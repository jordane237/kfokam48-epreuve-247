package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.AjouterPresenceManuelleRequest;
import com.kfokam48.presence.api.dto.OuvrirSessionRequest;
import com.kfokam48.presence.api.dto.PresenceDto;
import com.kfokam48.presence.api.dto.SessionCreeeDto;
import com.kfokam48.presence.api.dto.SessionDto;
import com.kfokam48.presence.service.PresenceService;
import com.kfokam48.presence.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/sessions — opération imposée du contrat, complétée par les
 * opérations ajoutées : clôture (EF7, RG12) et présence manuelle (EF12, RG11).
 * Le contrôleur ne fait aucune requête base : délégation aux services (B3).
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;
    private final PresenceService presences;

    public SessionController(SessionService service, PresenceService presences) {
        this.service = service;
        this.presences = presences;
    }

    @PostMapping
    public ResponseEntity<SessionCreeeDto> ouvrir(@Valid @RequestBody OuvrirSessionRequest requete) {
        SessionCreeeDto creee = service.ouvrir(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    /** Vue complète d'une session (contrat — schéma Session, inclut clotureAt). */
    @GetMapping("/{id}")
    public SessionDto consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    /** EF7/RG12 : clôture manuelle, distincte de l'expiration du code (Q12). */
    @PostMapping("/{id}/cloture")
    public ResponseEntity<SessionDto> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(service.cloturer(id));
    }

    /**
     * EF12/RG11 : présence ajoutée à la main par le formateur — la logique vit
     * dans PresenceService ; la route est ici car elle est rattachée à la session.
     */
    @PostMapping("/{id}/presences-manuelles")
    public ResponseEntity<PresenceDto> presenceManuelle(@PathVariable Long id,
            @Valid @RequestBody AjouterPresenceManuelleRequest requete) {
        PresenceDto presence = presences.presenceManuelle(id, requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }
}
