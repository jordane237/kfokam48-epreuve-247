package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Entrée PUT /api/exercices/{id} — conforme au contrat { lien }. */
public record RemplacerLienRequest(
        @NotBlank(message = "Le champ « lien » est obligatoire.") String lien) {
}
