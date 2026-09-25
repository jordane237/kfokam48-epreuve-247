package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.EtudiantDto;
import com.kfokam48.presence.service.EtudiantService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/etudiants?promotionId= — opération supplémentaire (libre, contrat) :
 * l'étudiant choisit son nom dans une liste (Q1), le front doit donc pouvoir
 * charger cette liste. Contrôleur sans requête base : délégation au service (B3).
 */
@RestController
public class EtudiantController {

    private final EtudiantService service;

    public EtudiantController(EtudiantService service) {
        this.service = service;
    }

    @GetMapping("/api/etudiants")
    public List<EtudiantDto> lister(@RequestParam Long promotionId) {
        return service.listerParPromotion(promotionId);
    }
}
