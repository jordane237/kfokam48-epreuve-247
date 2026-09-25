package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.RelectureDto;
import com.kfokam48.presence.api.dto.RelectureEnAttenteDto;
import com.kfokam48.presence.api.dto.RendreRelectureRequest;
import com.kfokam48.presence.api.dto.RetourDto;
import com.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * GET /api/relectures/en-attente?relecteurId= — opération ajoutée (libre, contrat).
     * Aucune identité d'auteur n'est exposée (Q8).
     */
    @GetMapping("/api/relectures/en-attente")
    public List<RelectureEnAttenteDto> enAttente(@RequestParam Long relecteurId) {
        return service.listerEnAttente(relecteurId);
    }

    @PostMapping("/api/relectures/{id}")
    public ResponseEntity<RelectureDto> rendre(@PathVariable Long id,
            @Valid @RequestBody RendreRelectureRequest requete) {
        return ResponseEntity.ok(service.rendre(id, requete));
    }

    /**
     * GET /api/exercices/{id}/retour — opération ajoutée (EF11, Q8) : l'étudiant
     * relu consulte sa note et le commentaire, sans jamais voir le relecteur.
     */
    @GetMapping("/api/exercices/{id}/retour")
    public ResponseEntity<RetourDto> retour(@PathVariable Long id) {
        return ResponseEntity.ok(service.consulterRetour(id));
    }
}
