package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrée POST /api/relectures/{id} — conforme au contrat { note, commentaire }. */
public record RendreRelectureRequest(
        @NotNull(message = "Le champ « note » est obligatoire.") Integer note,
        @NotBlank(message = "Le champ « commentaire » est obligatoire.") String commentaire) {
}
