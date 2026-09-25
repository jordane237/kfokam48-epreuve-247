package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.RelectureDto;
import com.kfokam48.presence.api.dto.RendreRelectureRequest;
import com.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/relectures/{id} — opération imposée du contrat (id = id de l'exercice).
 * Contrôleur sans requête base : délégation au service (B3).
 */
@RestController
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    @PostMapping("/api/relectures/{id}")
    public ResponseEntity<RelectureDto> rendre(@PathVariable Long id,
            @Valid @RequestBody RendreRelectureRequest requete) {
        return ResponseEntity.ok(service.rendre(id, requete));
    }
}
