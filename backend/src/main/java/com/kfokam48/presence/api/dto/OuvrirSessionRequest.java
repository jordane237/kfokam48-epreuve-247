package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrée POST /api/sessions — conforme au contrat { titre, promotionId }. */
public record OuvrirSessionRequest(
        @NotBlank(message = "Le champ « titre » est obligatoire.") String titre,
        @NotNull(message = "Le champ « promotionId » est obligatoire.") Long promotionId) {
}
