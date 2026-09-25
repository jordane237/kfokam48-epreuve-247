package com.kfokam48.presence.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de vérification du squelette — sera enrichi par les tickets T2 à T7. */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "application", "kfokam48-presence");
    }
}
