package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.RelectureDto;
import com.kfokam48.presence.api.dto.RelectureEnAttenteDto;
import com.kfokam48.presence.api.dto.RendreRelectureRequest;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
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
    private final RelectureRepository relectures;
    private final ExerciceRepository exercices;

    public RelectureController(RelectureService service, RelectureRepository relectures,
            ExerciceRepository exercices) {
        this.service = service;
        this.relectures = relectures;
        this.exercices = exercices;
    }

    /**
     * GET /api/relectures/en-attente?relecteurId= — opération ajoutée (libre, contrat) :
     * les relectures assignées et pas encore rendues d'un étudiant (écran relecteur).
     * Aucune identité d'auteur n'est exposée (Q8).
     */
    @GetMapping("/api/relectures/en-attente")
    public List<RelectureEnAttenteDto> enAttente(@RequestParam Long relecteurId) {
        return relectures.findByRelecteurIdAndStatut(relecteurId, Relecture.Statut.assignee).stream()
                .map(r -> exercices.findById(r.getExerciceId())
                        .map(e -> new RelectureEnAttenteDto(e.getId(), e.getLien(), r.getAssigneeAt()))
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
    }

    @PostMapping("/api/relectures/{id}")
    public ResponseEntity<RelectureDto> rendre(@PathVariable Long id,
            @Valid @RequestBody RendreRelectureRequest requete) {
        return ResponseEntity.ok(service.rendre(id, requete));
    }
}
