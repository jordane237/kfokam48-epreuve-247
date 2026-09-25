package com.kfokam48.presence.service;

import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF8 : assignation automatique d'un relecteur, juste après un dépôt réussi.
 * RG13 : tirage au hasard parmi les étudiants présents à cette session.
 * RG7 : l'auteur est exclu du tirage.
 * RG14 : si aucun candidat, l'exercice passe à `en_attente_relecteur` et
 * AUCUNE erreur n'est renvoyée à l'appelant — le dépôt réussit quand même (Q11).
 * RG16 : un seul relecteur par exercice (unicité en base, V6).
 */
@Service
public class AssignationService {

    private final PresenceRepository presences;
    private final RelectureRepository relectures;
    private final ExerciceRepository exercices;
    private final Random aleatoire;

    public AssignationService(PresenceRepository presences, RelectureRepository relectures,
            ExerciceRepository exercices, Random aleatoire) {
        this.presences = presences;
        this.relectures = relectures;
        this.exercices = exercices;
        this.aleatoire = aleatoire;
    }

    /** Assigne un relecteur si possible et renvoie le statut final de l'exercice. */
    @Transactional
    public Exercice.Statut assigner(Exercice exercice) {
        List<Long> candidats = presences.findBySessionId(exercice.getSessionId()).stream()
                .map(Presence::getEtudiantId)
                .filter(id -> !id.equals(exercice.getEtudiantId())) // RG7
                .distinct()
                .toList();

        if (candidats.isEmpty()) {
            // RG14 : personne d'éligible — l'exercice reste en attente, sans erreur.
            exercice.setStatut(Exercice.Statut.en_attente_relecteur);
            exercices.save(exercice);
            return exercice.getStatut();
        }

        Long elu = candidats.get(aleatoire.nextInt(candidats.size())); // RG13 : au hasard
        relectures.save(new Relecture(exercice.getId(), elu, LocalDateTime.now()));
        exercice.setStatut(Exercice.Statut.assigne);
        exercices.save(exercice);
        return exercice.getStatut();
    }
}
