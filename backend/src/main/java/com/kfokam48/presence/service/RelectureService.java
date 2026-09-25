package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.RelectureDto;
import com.kfokam48.presence.api.dto.RelectureEnAttenteDto;
import com.kfokam48.presence.api.dto.RendreRelectureRequest;
import com.kfokam48.presence.api.dto.RetourDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF9 : rendre une relecture (POST /api/relectures/{id}, id = id de l'exercice).
 * RG7 : relecteur == auteur → 403. RG8 : note entière 0–20 → sinon 400.
 * RG10 : une relecture rendue est définitive → 409 RELECTURE_DEJA_RENDUE,
 * aucune correction possible (Q15 tranché contre Q10 — cahier des charges §7).
 */
@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final ExerciceRepository exercices;

    public RelectureService(RelectureRepository relectures, ExerciceRepository exercices) {
        this.relectures = relectures;
        this.exercices = exercices;
    }

    /**
     * GET /api/relectures/en-attente?relecteurId= — opération ajoutée (libre, contrat) :
     * les relectures assignées et pas encore rendues d'un étudiant (écran relecteur).
     * Aucune identité d'auteur n'est exposée (Q8).
     */
    @Transactional(readOnly = true)
    public List<RelectureEnAttenteDto> listerEnAttente(Long relecteurId) {
        return relectures.findByRelecteurIdAndStatut(relecteurId, Relecture.Statut.assignee).stream()
                .map(r -> exercices.findById(r.getExerciceId())
                        .map(e -> new RelectureEnAttenteDto(e.getId(), e.getLien(), r.getAssigneeAt()))
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
    }

    /**
     * EF11/Q8 : la note et le commentaire sans le nom du relecteur — le retour
     * est consultable dès qu'une relecture a été rendue.
     */
    @Transactional(readOnly = true)
    public RetourDto consulterRetour(Long exerciceId) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU",
                        "Cet exercice n'existe pas."));
        List<Relecture> affectations = relectures.findByExerciceId(exerciceId);
        if (affectations.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                    "Aucune relecture n'est assignée pour cet exercice.");
        }
        List<Relecture> rendues = affectations.stream()
                .filter(r -> r.getStatut() == Relecture.Statut.rendue && r.getNote() != null)
                .toList();
        if (rendues.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "RELECTURE_PAS_ENCORE_RENDUE",
                    "Aucune relecture n'a encore été rendue.");
        }
        // Étape 3 : moyenne des relectures rendues ; PROVISOIRE si une seule rendue
        // (moins de 2). Q8 : aucun champ ne mentionne les relecteurs.
        double moyenne = rendues.stream().mapToInt(Relecture::getNote).average().orElse(0);
        boolean provisoire = rendues.size() < 2;
        List<String> commentaires = rendues.stream()
                .map(Relecture::getCommentaire)
                .filter(c -> c != null && !c.isBlank())
                .toList();
        LocalDateTime derniere = rendues.stream()
                .map(Relecture::getRendueAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        return new RetourDto(exercice.getId(), moyenne, provisoire, commentaires, derniere);
    }

    @Transactional
    public RelectureDto rendre(Long exerciceId, RendreRelectureRequest requete) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU",
                        "Cet exercice n'existe pas."));

        List<Relecture> affectations = relectures.findByExerciceId(exerciceId);
        if (affectations.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                    "Aucune relecture n'est assignée pour cet exercice.");
        }

        // Étape 3 : chaque relecteur soumet SA propre affectation. Le champ
        // relecteurId du corps est requis dès que deux affectations coexistent ;
        // absent avec une seule affectation, le comportement historique est conservé
        // (rétrocompatibilité — B2 : aucun chemin/verbe/code imposé ne change).
        Relecture relecture;
        if (affectations.size() > 1 && requete.relecteurId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "RELECTEUR_REQUIS",
                    "Deux relecteurs sont assignés : précisez relecteurId dans la requête.");
        }
        if (requete.relecteurId() != null) {
            final Long demande = requete.relecteurId();
            relecture = affectations.stream()
                    .filter(r -> r.getRelecteurId().equals(demande))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                            "Aucune affectation pour ce relecteur sur cet exercice."));
        } else {
            relecture = affectations.get(0);
        }

        // RG7 : on ne relit jamais son propre exercice (défensif — l'assignation l'exclut déjà).
        if (relecture.getRelecteurId().equals(exercice.getEtudiantId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "RELECTURE_PROPRE_EXERCICE",
                    "On ne peut pas relire son propre exercice.");
        }

        // RG10 : définitive dès l'envoi — le 409 n'a aucune exception, par affectation.
        if (relecture.getStatut() == Relecture.Statut.rendue) {
            throw new BusinessException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE",
                    "Cette relecture a déjà été rendue et est définitive.");
        }

        // RG8 : note entière entre 0 et 20 (le type Integer du DTO garantit l'entier ;
        // une note décimale est rejetée dès le parsing JSON avec NOTE_INVALIDE).
        if (requete.note() == null || requete.note() < 0 || requete.note() > 20) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE",
                    "La note doit être un entier entre 0 et 20.");
        }

        relecture.setNote(requete.note());
        relecture.setCommentaire(requete.commentaire());
        relecture.setStatut(Relecture.Statut.rendue);
        relecture.setRendueAt(LocalDateTime.now());

        // D4 étape 3 : première relecture rendue → relu_partiel (note PROVISOIRE) ;
        // seconde (ou exercice à un seul relecteur) → relu (note finale).
        long renduesApres = relectures.findByExerciceId(exerciceId).stream()
                .filter(r -> r.getStatut() == Relecture.Statut.rendue)
                .count();
        boolean deuxiemeRendue = renduesApres >= 2;
        exercice.setStatut(deuxiemeRendue ? Exercice.Statut.relu : Exercice.Statut.relu_partiel);
        exercice.setMajAt(LocalDateTime.now());
        exercices.save(exercice);

        Relecture rendue = relectures.save(relecture);

        // Champ additionnel (forme imposée intacte) : l'état des affectations
        // de l'exercice après cette soumission — sans identité de l'auteur (Q8).
        List<RelectureDto.AffectationDto> vueAffectations = relectures.findByExerciceId(exerciceId).stream()
                .map(r -> new RelectureDto.AffectationDto(r.getId(), r.getRelecteurId(),
                        r.getStatut().name(), r.getNote(), r.getRendueAt()))
                .toList();
        return new RelectureDto(rendue.getId(), rendue.getExerciceId(), rendue.getNote(),
                rendue.getCommentaire(), rendue.getRendueAt(), rendue.getStatut().name(), vueAffectations);
    }
}
