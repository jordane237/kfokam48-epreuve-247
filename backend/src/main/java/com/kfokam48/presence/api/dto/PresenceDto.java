package com.kfokam48.presence.api.dto;

/** Sortie 201 de POST /api/presences — exactement { id, sessionId, etudiantId, source } (contrat). */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, String source) {
}
