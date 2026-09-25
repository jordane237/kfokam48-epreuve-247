package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/** Réponse complète d'un exercice — PUT /api/exercices/{id} (EF6, RG6). */
public record ExerciceDto(Long id, Long sessionId, Long etudiantId, String lien,
        String statut, LocalDateTime deposeAt, LocalDateTime majAt) {
}
