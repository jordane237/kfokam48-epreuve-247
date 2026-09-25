package com.kfokam48.presence.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
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
 * Changement de besoin étape 3 — double relecture (issues #35/#37) :
 * chaque exercice est relu par deux pairs distincts ; la note affichée est la
 * moyenne des relectures rendues ; une seule rendue → note PROVISOIRE.
 * Chaque relecture individuelle reste définitive (RG10 inchangé).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoubleRelectureIntegrationTest {

    private static final String CODE = "DBL202";

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
    @Autowired
    private EtudiantRepository etudiants;

    private Long exerciceId;
    private Long relecteur1Id;
    private Long relecteur2Id;

    @BeforeEach
    void amenerLEtatInitial() {
        // Session ouverte : auteur (1) + deux relecteurs présents (11, 12).
        Session session = sessions.save(new Session("Cours double", 1L, CODE,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
        presences.save(new Presence(session.getId(), 1L, Presence.Source.ETUDIANT, LocalDateTime.now()));
        Etudiant r1 = etudiants.save(new Etudiant(1L, "Relecteur Un"));
        Etudiant r2 = etudiants.save(new Etudiant(1L, "Relecteur Deux"));
        relecteur1Id = r1.getId();
        relecteur2Id = r2.getId();
        presences.save(new Presence(session.getId(), relecteur1Id, Presence.Source.ETUDIANT, LocalDateTime.now()));
        presences.save(new Presence(session.getId(), relecteur2Id, Presence.Source.ETUDIANT, LocalDateTime.now()));

        Exercice exercice = exercices.save(new Exercice(session.getId(), 1L, "https://exemple.com/dbl",
                Exercice.Statut.assigne, LocalDateTime.now()));
        exerciceId = exercice.getId();
        relectures.save(new Relecture(exerciceId, relecteur1Id, LocalDateTime.now()));
        relectures.save(new Relecture(exerciceId, relecteur2Id, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Étape 3 : une seule relecture rendue → note = sa note, provisoire=true, statut relu_partiel")
    void uneSeuleRendueNoteProvisoire() throws Exception {
        // Le relecteur 1 rend 14 — le relecteur 2 n'a pas encore rendu.
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 14, \"commentaire\": \"Premier avis.\", \"relecteurId\": " + relecteur1Id + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.affectations.length()").value(2));

        mvc.perform(get("/api/exercices/" + exerciceId + "/retour"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(14.0))
                .andExpect(jsonPath("$.provisoire").value(true))
                .andExpect(jsonPath("$.commentaires.length()").value(1));

        // D4 étape 3 : l'exercice passe à relu_partiel (une relecture sur deux).
        org.junit.jupiter.api.Assertions.assertEquals(Exercice.Statut.relu_partiel,
                exercices.findById(exerciceId).orElseThrow().getStatut());
    }

    @Test
    @DisplayName("Étape 3 : deux relectures rendues → note = moyenne exacte, provisoire=false, statut relu")
    void deuxRenduesMoyenneExacte() throws Exception {
        rendre(relecteur1Id, 10, "Premier avis.");
        rendre(relecteur2Id, 16, "Second avis.");

        mvc.perform(get("/api/exercices/" + exerciceId + "/retour"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(13.0)) // (10 + 16) / 2
                .andExpect(jsonPath("$.provisoire").value(false))
                .andExpect(jsonPath("$.commentaires.length()").value(2));

        // D4 étape 3 : les deux relectures rendues → relu (note définitive).
        org.junit.jupiter.api.Assertions.assertEquals(Exercice.Statut.relu,
                exercices.findById(exerciceId).orElseThrow().getStatut());
    }

    @Test
    @DisplayName("RG10 par affectation : chaque relecteur ne peut rendre qu'une fois (409), l'autre peut encore rendre")
    void rg10ParAffectation() throws Exception {
        rendre(relecteur1Id, 12, "Une fois suffit.");

        // Le relecteur 1 retente → 409 RELECTURE_DEJA_RENDUE (sa propre affectation).
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 18, \"commentaire\": \"Tentative.\", \"relecteurId\": " + relecteur1Id + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));

        // Le relecteur 2, lui, peut encore rendre : la note passe de provisoire à définitive.
        rendre(relecteur2Id, 14, "Second avis.");
        mvc.perform(get("/api/exercices/" + exerciceId + "/retour"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(13.0))
                .andExpect(jsonPath("$.provisoire").value(false));
    }

    @Test
    @DisplayName("Deux affectations sans relecteurId → 400 RELECTEUR_REQUIS (ambiguïté, B2 : chemin inchangé)")
    void deuxAffectationsSansRelecteurId() throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                        .contentType("application/json")
                        .content("{\"note\": 12, \"commentaire\": \"Qui parle ?\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RELECTEUR_REQUIS"));
    }

    private void rendre(Long relecteurId, int note, String commentaire) throws Exception {
        mvc.perform(post("/api/relectures/" + exerciceId)
                .contentType("application/json")
                .content("{\"note\": " + note + ", \"commentaire\": \"" + commentaire
                        + "\", \"relecteurId\": " + relecteurId + "}"))
                .andExpect(status().isOk());
    }
}
