package com.kfokam48.presence.api.error;

/** Format d'erreur imposé par le contrat : { "code": "...", "message": "..." } — B4. */
public record ApiError(String code, String message) {
}
