package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sortie 200 de POST /api/relectures/{id} — conforme au schéma Relecture du contrat.
 * N'expose jamais le nom des relecteurs (Q8). Étape 3 : `affectations` est un
 * champ additionnel qui reflète l'état de l'exercice après la soumission
 * (une ou deux relectures, rendues ou non) — la forme imposée est intacte.
 */
public record RelectureDto(Long id, Long exerciceId, Integer note, String commentaire,
        LocalDateTime rendueAt, String statut, List<AffectationDto> affectations) {

    /** Vue d'une affectation de relecture (sans identité de l'auteur, Q8). */
    public record AffectationDto(Long relectureId, Long relecteurId, String statut,
            Integer note, LocalDateTime rendueAt) {
    }
}
