package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.DeposerExerciceRequest;
import com.kfokam48.presence.api.dto.ExerciceCreeDto;
import com.kfokam48.presence.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/exercices — opération imposée du contrat.
 * Contrôleur sans requête base : délégation au service (B3).
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExerciceCreeDto> deposer(@Valid @RequestBody DeposerExerciceRequest requete) {
        ExerciceCreeDto cree = service.deposer(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }
}
