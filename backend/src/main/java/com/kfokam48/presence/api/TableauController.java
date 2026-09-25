package com.kfokam48.presence.api;

import com.kfokam48.presence.api.dto.LigneTableauDto;
import com.kfokam48.presence.service.TableauService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/tableau?promotionId= — opération imposée du contrat.
 * Contrôleur sans requête base : délégation au service (B3).
 */
@RestController
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    @GetMapping("/api/tableau")
    public List<LigneTableauDto> tableau(@RequestParam Long promotionId) {
        return service.tableau(promotionId);
    }
}
