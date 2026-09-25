package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.OuvrirSessionRequest;
import com.kfokam48.presence.api.dto.SessionCreeeDto;
import com.kfokam48.presence.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/sessions — opération imposée du contrat.
 * Le contrôleur ne fait aucune requête base : il délègue au service (B3).
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SessionCreeeDto> ouvrir(@Valid @RequestBody OuvrirSessionRequest requete) {
        SessionCreeeDto creee = service.ouvrir(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }
}
