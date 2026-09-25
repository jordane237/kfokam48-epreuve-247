package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.EtudiantDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * B3 : toute la logique du listing des étudiants vit ici — le contrôleur ne
 * fait que déléguer. L'étudiant choisit son nom dans une liste (Q1) ; aucune
 * donnée sensible n'est exposée : id + nom uniquement (Q1, Q8).
 */
@Service
public class EtudiantService {

    private final EtudiantRepository etudiants;
    private final PromotionRepository promotions;

    public EtudiantService(EtudiantRepository etudiants, PromotionRepository promotions) {
        this.etudiants = etudiants;
        this.promotions = promotions;
    }

    @Transactional(readOnly = true)
    public List<EtudiantDto> listerParPromotion(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Cette promotion n'existe pas.");
        }
        return etudiants.findByPromotionIdOrderById(promotionId).stream()
                .map(e -> new EtudiantDto(e.getId(), e.getNom()))
                .toList();
    }
}
