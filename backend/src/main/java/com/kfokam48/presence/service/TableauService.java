package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.LigneTableauDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF13 : tableau de suivi du formateur (Q16, RG15).
 * La moyenne est calculée ICI, côté API (F3) — le front ne la recalcule jamais.
 * Aucune identité de relecteur n'est exposée (Q8).
 */
@Service
public class TableauService {

    private final EtudiantRepository etudiants;
    private final PromotionRepository promotions;
    private final SessionRepository sessions;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(EtudiantRepository etudiants, PromotionRepository promotions,
            SessionRepository sessions, PresenceRepository presences,
            ExerciceRepository exercices, RelectureRepository relectures) {
        this.etudiants = etudiants;
        this.promotions = promotions;
        this.sessions = sessions;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    @Transactional(readOnly = true)
    public List<LigneTableauDto> tableau(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Cette promotion n'existe pas.");
        }

        List<com.kfokam48.presence.entity.Etudiant> etudiantsPromo =
                etudiants.findByPromotionIdOrderById(promotionId);

        List<Long> sessionIds = sessions.findByPromotionId(promotionId).stream()
                .map(s -> s.getId()).toList();

        List<Exercice> tousExercices = sessionIds.isEmpty()
                ? List.of()
                : exercices.findBySessionIdIn(sessionIds);
        List<Long> idsExercices = tousExercices.stream().map(Exercice::getId).toList();

        // Exercices déposés par étudiant (RG15).
        Map<Long, List<Exercice>> exercicesParEtudiant = tousExercices.stream()
                .collect(Collectors.groupingBy(Exercice::getEtudiantId));

        // Auteur de chaque exercice : pour rattacher les notes reçues (Q16).
        Map<Long, Long> auteurParExercice = tousExercices.stream()
                .collect(Collectors.toMap(Exercice::getId, Exercice::getEtudiantId));

        List<Relecture> toutesRelectures = idsExercices.isEmpty()
                ? List.of()
                : relectures.findByExerciceIdIn(idsExercices);

        // Étape 3 : la moyenne est calculée PAR EXERCICE (moyenne des relectures
        // rendues de cet exercice, provisoire si une seule) puis agrégée par auteur.
        Map<Long, List<Relecture>> renduesParExercice = toutesRelectures.stream()
                .filter(r -> r.getStatut() == Relecture.Statut.rendue && r.getNote() != null)
                .collect(Collectors.groupingBy(Relecture::getExerciceId));

        Map<Long, List<Double>> notesRecuesParAuteur = new HashMap<>();
        renduesParExercice.forEach((exerciceId, rendues) -> {
            double moyenneExercice = rendues.stream().mapToInt(Relecture::getNote).average().orElse(0);
            Long auteur = auteurParExercice.get(exerciceId);
            if (auteur != null) {
                notesRecuesParAuteur.computeIfAbsent(auteur, k -> new ArrayList<>()).add(moyenneExercice);
            }
        });

        // Relectures en attente par relecteur (Q11, Q16).
        Map<Long, Long> enAttenteParRelecteur = toutesRelectures.stream()
                .filter(r -> r.getStatut() == Relecture.Statut.assignee)
                .collect(Collectors.groupingBy(Relecture::getRelecteurId, Collectors.counting()));

        // Q14/RG11 : les présences « ajoutées par le formateur » doivent se voir
        // distinctement dans le tableau — comptées à part des marquages étudiants.
        List<Presence> toutesPresences = sessionIds.isEmpty()
                ? List.of()
                : presences.findBySessionIdIn(sessionIds);
        Map<Long, Long> presencesFormateurParEtudiant = toutesPresences.stream()
                .filter(p -> p.getSource() == Presence.Source.FORMATEUR)
                .collect(Collectors.groupingBy(Presence::getEtudiantId, Collectors.counting()));

        return etudiantsPromo.stream()
                .map(etudiant -> {
                    Long id = etudiant.getId();

                    long nbPresences = sessionIds.isEmpty() ? 0
                            : presences.findBySessionIdIn(sessionIds).stream()
                                    .filter(p -> p.getEtudiantId().equals(id)).count();

                    long nbExercices = exercicesParEtudiant
                            .getOrDefault(id, List.of()).size();

                    List<Double> notes = notesRecuesParAuteur.get(id);
                    Double moyenne = (notes == null || notes.isEmpty())
                            ? null // F3 : null si aucune note, calculée côté API
                            : notes.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                    return new LigneTableauDto(id, etudiant.getNom(), nbPresences,
                            presencesFormateurParEtudiant.getOrDefault(id, 0L),
                            nbExercices, moyenne,
                            enAttenteParRelecteur.getOrDefault(id, 0L));
                })
                .toList();
    }
}
