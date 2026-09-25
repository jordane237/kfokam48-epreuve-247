package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.MarquerPresenceRequest;
import com.kfokam48.presence.api.dto.PresenceDto;
import com.kfokam48.presence.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/presences — opération imposée du contrat.
 * Contrôleur sans requête base : délégation au service (B3).
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PresenceDto> marquer(@Valid @RequestBody MarquerPresenceRequest requete) {
        PresenceDto presence = service.marquer(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }
}
