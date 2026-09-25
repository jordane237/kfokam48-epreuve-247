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
 * EF8 : assignation automatique des relecteurs, juste après un dépôt réussi.
 * RG13 : tirage au hasard parmi les étudiants présents à cette session.
 * RG7 : l'auteur est exclu du tirage.
 * RG14 : si aucun candidat, l'exercice passe à `en_attente_relecteur` et
 * AUCUNE erreur n'est renvoyée à l'appelant — le dépôt réussit quand même (Q11).
 * RG16 (changement de besoin étape 3) : DEUX relecteurs distincts par exercice ;
 * un seul candidat → il est assigné seul (la note sera provisoire) ; aucun → RG14.
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

    /** Assigne les relecteurs (deux si possible) et renvoie le statut final de l'exercice. */
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

        // RG16 étape 3 : premier relecteur, puis un second distinct si possible.
        Long premier = candidats.get(aleatoire.nextInt(candidats.size()));
        relectures.save(new Relecture(exercice.getId(), premier, LocalDateTime.now()));

        if (candidats.size() > 1) {
            Long second;
            do {
                second = candidats.get(aleatoire.nextInt(candidats.size()));
            } while (second.equals(premier));
            relectures.save(new Relecture(exercice.getId(), second, LocalDateTime.now()));
        }
        // Un seul candidat : il est assigné seul — la note de l'exercice sera
        // PROVISOIRE jusqu'à (éventuelle) seconde assignation, choix client étape 3.

        exercice.setStatut(Exercice.Statut.assigne);
        exercices.save(exercice);
        return exercice.getStatut();
    }
}
