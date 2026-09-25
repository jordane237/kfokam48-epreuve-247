package com.kfokam48.presence.api.dto;

/** Sortie 201 de POST /api/exercices — exactement { id, statut } (contrat). */
public record ExerciceCreeDto(Long id, String statut) {
}
