package com.kfokam48.presence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entrée POST /api/relectures/{id} — conforme au contrat { note, commentaire }.
 * Étape 3 (double relecture) : `relecteurId` est OPTIONNEL pour rester
 * rétrocompatible — requis dès que deux relecteurs sont assignés à l'exercice
 * (chaque relecteur soumet SA propre affectation) ; absent avec une seule
 * affectation, le comportement historique est conservé. Aucun chemin, verbe
 * ni code de statut des opérations imposées ne change (B2).
 */
public record RendreRelectureRequest(
        @NotNull(message = "Le champ « note » est obligatoire.") Integer note,
        @NotBlank(message = "Le champ « commentaire » est obligatoire.") String commentaire,
        Long relecteurId) {
}
