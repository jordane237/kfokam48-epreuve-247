package com.kfokam48.presence.api.dto;

/**
 * Une ligne du tableau du formateur — exactement le format du contrat (Q16, RG15).
 * `moyenne` est calculée côté API (F3) et vaut null si l'étudiant n'a reçu aucune note.
 * N'expose jamais l'identité des relecteurs (Q8).
 */
public record LigneTableauDto(Long etudiantId, String nom, long presences,
        long presencesFormateur, long exercicesDeposes, Double moyenne,
        long relecturesEnAttente) {
}
