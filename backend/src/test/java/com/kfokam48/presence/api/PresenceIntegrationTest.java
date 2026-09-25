package com.kfokam48.presence.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test d'intégration B6 (T5) : l'endpoint POST /api/presences est testé de bout
 * en bout (contrôleur → service → base H2, migrations Flyway exécutées).
 * Couvre le nominal 201 et les erreurs imposées : 400 CODE_INCONNU,
 * 409 DEJA_PRESENT (RG2), 410 CODE_EXPIRE (RG1).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SessionRepository sessions;
    @Autowired
    private PresenceRepository presences;
    @Autowired
    private EtudiantRepository etudiants;

    private static final String CODE = "PRST01";

    @BeforeEach
    void amenerLEtatInitial() {
        // Une session ouverte de 15 minutes (RG1) avec code fixe pour les tests.
        sessions.save(new Session("Cours presence", 1L, CODE,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
    }

    @Test
    @DisplayName("EF3 : cas nominal — 201, la présence est enregistrée en source ETUDIANT")
    void casNominal() throws Exception {
        Etudiant etudiant = etudiants.save(new Etudiant(1L, "Manga Test"));

        mvc.perform(post("/api/presences")
                        .contentType("application/json")
                        .content("{\"code\": \"" + CODE + "\", \"etudiantId\": " + etudiant.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(etudiant.getId().intValue()))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    @DisplayName("Code inexistant — 400 CODE_INCONNU")
    void codeInconnu() throws Exception {
        mvc.perform(post("/api/presences")
                        .contentType("application/json")
                        .content("{\"code\": \"FAUX9\", \"etudiantId\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
    }

    @Test
    @DisplayName("RG1 : session au-delà de expirationAt — 410 CODE_EXPIRE")
    void codeExpire() throws Exception {
        sessions.save(new Session("Cours ancien", 1L, "VIEUX",
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(5)));

        mvc.perform(post("/api/presences")
                        .contentType("application/json")
                        .content("{\"code\": \"VIEUX\", \"etudiantId\": 6}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    @DisplayName("RG2 : deuxième marquage du même étudiant — 409 DEJA_PRESENT")
    void dejaPresent() throws Exception {
        Session session = sessions.findByCode(CODE).orElseThrow();
        Etudiant etudiant = etudiants.save(new Etudiant(1L, "Nguema Test"));
        presences.save(new Presence(session.getId(), etudiant.getId(), Presence.Source.ETUDIANT, LocalDateTime.now()));

        mvc.perform(post("/api/presences")
                        .contentType("application/json")
                        .content("{\"code\": \"" + CODE + "\", \"etudiantId\": " + etudiant.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }
}
