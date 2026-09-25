package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/** Réponse complète d'une session — inclut clotureAt (EF7, RG12). */
public record SessionDto(Long id, String titre, Long promotionId, String code,
        LocalDateTime ouvertureAt, LocalDateTime expirationAt, LocalDateTime clotureAt) {
}
