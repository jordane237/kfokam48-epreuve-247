package com.kfokam48.presence.api.dto;

import java.time.LocalDateTime;

/** Sortie 201 de POST /api/sessions — exactement { id, code, ouvertureAt, expirationAt } (contrat). */
public record SessionCreeeDto(Long id, String code, LocalDateTime ouvertureAt, LocalDateTime expirationAt) {
}
