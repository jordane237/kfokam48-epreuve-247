package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/**
 * Sortie 200 de POST /api/relectures/{id} — conforme au schéma Relecture du contrat.
 * N'expose jamais le nom du relecteur (Q8) : l'étudiant relu ne voit que note et commentaire.
 */
public record RelectureDto(Long id, Long exerciceId, Integer note, String commentaire,
        LocalDateTime rendueAt, String statut) {
}
