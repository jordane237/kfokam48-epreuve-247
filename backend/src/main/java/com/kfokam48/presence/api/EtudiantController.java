package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.EtudiantDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/etudiants?promotionId= — opération supplémentaire (libre, contrat) :
 * l'étudiant choisit son nom dans une liste (Q1), le front doit donc pouvoir
 * charger cette liste. Aucun mot de passe, aucun secret — id + nom (Q1, Q8).
 */
@RestController
public class EtudiantController {

    private final EtudiantRepository etudiants;
    private final PromotionRepository promotions;

    public EtudiantController(EtudiantRepository etudiants, PromotionRepository promotions) {
        this.etudiants = etudiants;
        this.promotions = promotions;
    }

    @GetMapping("/api/etudiants")
    public List<EtudiantDto> lister(@RequestParam Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Cette promotion n'existe pas.");
        }
        return etudiants.findByPromotionIdOrderById(promotionId).stream()
                .map(e -> new EtudiantDto(e.getId(), e.getNom()))
                .toList();
    }
}
