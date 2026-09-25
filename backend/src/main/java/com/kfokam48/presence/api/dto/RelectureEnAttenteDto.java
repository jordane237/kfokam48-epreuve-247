package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/** Une relecture à faire, pour l'écran relecteur — le lien de l'exercice sans identité d'auteur. */
public record RelectureEnAttenteDto(Long exerciceId, String lien, LocalDateTime assigneeAt) {
}
