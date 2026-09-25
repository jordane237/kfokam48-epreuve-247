package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrée POST /api/presences — conforme au contrat { code, etudiantId }. */
public record MarquerPresenceRequest(
        @NotBlank(message = "Le champ « code » est obligatoire.") String code,
        @NotNull(message = "Le champ « etudiantId » est obligatoire.") Long etudiantId) {
}
