package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Retour de relecture vu par l'auteur de l'exercice (EF11, Q8) — étape 3 :
 * la note est la MOYENNE des relectures rendues ; `provisoire` vaut true si
 * une seule est rendue (mention à afficher) ; les commentaires sont listés
 * sans jamais exposer l'identité des relecteurs (Q8).
 */
public record RetourDto(Long exerciceId, Double note, boolean provisoire,
        List<String> commentaires, LocalDateTime derniereRendueAt) {
}
