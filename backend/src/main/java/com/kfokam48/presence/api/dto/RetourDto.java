package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/**
 * Retour de relecture vu par l'auteur de l'exercice (EF11, Q8) :
 * note + commentaire uniquement, jamais l'identité du relecteur.
 */
public record RetourDto(Long exerciceId, Integer note, String commentaire, LocalDateTime rendueAt) {
}
