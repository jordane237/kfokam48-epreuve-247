package com.kfokam48.presence.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test d'intégration B6 (T6) : l'endpoint POST /api/relectures/{id} est testé
 * de bout en bout (contrôleur → service → base H2, migrations Flyway exécutées).
 * Tourne sur un poste vierge, sans base locale.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelectureIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SessionRepository sessions;
    @Autowired
    private PresenceRepository presences;
    @Autowired
    private ExerciceRepository exercices;
    @Autowired
    private RelectureRepository relectures;

    private Long exerciceId;

    @BeforeEach
    void amenerLEtatInitial() {
        // Un exercice déposé par l'étudiant 2, relecteur assigné : l'étudiant 3 (présent).
        Session session = sessions.save(new Session("Cours intégration", 1L, "TST234",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
        presences.save(new Presence(session.getId(), 2L, Presence.Source.ETUDIANT, LocalDateTime.now()));
        presences.save(new Presence(session.getId(), 3L, Presence.Source.ETUDIANT, LocalDateTime.now()));
        Exercice exercice = exercices.save(new Exercice(session.getId(), 2L, "https://exemple.com/exo",
                Exercice.Statut.assigne, LocalDateTime.now()));
        relectures.save(new Relecture(exercice.getId(), 3L, LocalDateTime.now()));
        exerciceId = exercice.getId();
    }

    @Test
    @DisplayName("EF9 : cas nominal — 200, la relecture est rendue et l'exercice passe à relu")
    void casNominal() throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 15, \"commentaire\": \"Bon travail, justifications claires.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciceId").value(exerciceId))
                .andExpect(jsonPath("$.note").value(15))
                .andExpect(jsonPath("$.statut").value("rendue"));
    }

    @Test
    @DisplayName("RG10 : une relecture déjà rendue renvoie 409 RELECTURE_DEJA_RENDUE — définitive")
    void dejaRendue() throws Exception {
        rendre("16", "Premier rendu.");
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 10, \"commentaire\": \"Tentative de correction.\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    @DisplayName("RG8 : une note hors 0–20 renvoie 400 NOTE_INVALIDE")
    void noteInvalide() throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 25, \"commentaire\": \"Trop généreux.\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    @DisplayName("RG8 : une note non entière est rejetée 400 NOTE_INVALIDE")
    void noteNonEntiere() throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 12.5, \"commentaire\": \"Note décimale.\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    private void rendre(String note, String commentaire) throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                .contentType("application/json")
                .content("{\"note\": " + note + ", \"commentaire\": \"" + commentaire + "\"}"))
                .andExpect(status().isOk());
    }
}
