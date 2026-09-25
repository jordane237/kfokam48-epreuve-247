package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.RelectureDto;
import com.kfokam48.presence.api.dto.RendreRelectureRequest;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import java.time.LocalDateTime;
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

    @Transactional
    public RelectureDto rendre(Long exerciceId, RendreRelectureRequest requete) {
        Relecture relecture = relectures.findByExerciceId(exerciceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                        "Aucune relecture n'est assignée pour cet exercice."));

        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU",
                        "Cet exercice n'existe pas."));

        // RG7 : on ne relit jamais son propre exercice (défensif — l'assignation l'exclut déjà).
        if (relecture.getRelecteurId().equals(exercice.getEtudiantId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "RELECTURE_PROPRE_EXERCICE",
                    "On ne peut pas relire son propre exercice.");
        }

        // RG10 : définitive dès l'envoi — le 409 n'a aucune exception.
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

        exercice.setStatut(Exercice.Statut.relu); // D4 : assigne → relu
        exercice.setMajAt(LocalDateTime.now());
        exercices.save(exercice);

        Relecture rendue = relectures.save(relecture);
        return new RelectureDto(rendue.getId(), rendue.getExerciceId(), rendue.getNote(),
                rendue.getCommentaire(), rendue.getRendueAt(), rendue.getStatut().name());
    }
}
