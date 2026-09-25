package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrée POST /api/exercices — conforme au contrat { sessionId, etudiantId, lien }. */
public record DeposerExerciceRequest(
        @NotNull(message = "Le champ « sessionId » est obligatoire.") Long sessionId,
        @NotNull(message = "Le champ « etudiantId » est obligatoire.") Long etudiantId,
        @NotBlank(message = "Le champ « lien » est obligatoire.") String lien) {
}
