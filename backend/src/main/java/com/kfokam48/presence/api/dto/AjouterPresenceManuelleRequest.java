package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotNull;

/** Entrée POST /api/sessions/{id}/presences-manuelles — conforme au contrat { etudiantId }. */
public record AjouterPresenceManuelleRequest(
        @NotNull(message = "Le champ « etudiantId » est obligatoire.") Long etudiantId) {
}
