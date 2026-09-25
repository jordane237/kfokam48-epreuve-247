package com.kfokam48.presence.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Issue #33 — reproduction du bug client :
 * « Deux étudiants différents ont tapé leur code presque en même temps sur la
 * même session. Un seul apparaît dans le tableau. Au réessai, les deux sont passés. »
 *
 * PAS de @Transactional : chaque marquage suit le chemin réel (service
 * @Transactional → commit propre), avec de vraies transactions concurrentes.
 * Une barrière force les deux requêtes à démarrer ensemble.
 * @DirtiesContext : base H2 fraîche pour chaque méthode — les commits réels
 * d'un test ne polluent pas le suivant.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PresenceConcurrenteIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SessionRepository sessions;
    @Autowired
    private PresenceRepository presences;
    @Autowired
    private EtudiantRepository etudiants;

    private ExecutorService pool;
    private String code;

    @BeforeEach
    void amenerLEtatInitial() {
        pool = Executors.newFixedThreadPool(2);
        // Code unique par méthode (colonne code VARCHAR(10) : 10 caractères max) :
        // la base H2 nommée survit entre les contextes de test, un code fixe
        // entrerait en collision avec une session précédente.
        code = String.format("C%09d", System.nanoTime() % 1_000_000_000L);
        sessions.save(new Session("Cours concurrence", 1L, code,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
    }

    @AfterEach
    void eteindreLePool() {
        pool.shutdownNow();
    }

    private Etudiant nouvelEtudiant(String nom) {
        return etudiants.save(new Etudiant(1L, nom));
    }

    /** Requête POST /api/presences, déclenchée dès que la barrière est franchie. */
    private Callable<Integer> marquer(String code, Long etudiantId, CyclicBarrier barriere) {
        return () -> {
            barriere.await(5, TimeUnit.SECONDS);
            return mvc.perform(post("/api/presences")
                            .contentType("application/json")
                            .content("{\"code\": \"" + code + "\", \"etudiantId\": " + etudiantId + "}"))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        };
    }

    private long presencesDeLaSession(Long sessionId) {
        return presences.findAll().stream()
                .filter(p -> sessionId.equals(p.getSessionId()))
                .count();
    }

    @Test
    @DisplayName("Issue #33 : deux étudiants différents en parallèle — les DEUX présences doivent exister en base")
    void deuxEtudiantsDifferentsEnParallele() throws Exception {
        Long sessionId = sessions.findByCode(code).orElseThrow().getId();
        Etudiant a = nouvelEtudiant("Conc A");
        Etudiant b = nouvelEtudiant("Conc B");
        CyclicBarrier barriere = new CyclicBarrier(2);

        var futureA = pool.submit(marquer(code, a.getId(), barriere));
        var futureB = pool.submit(marquer(code, b.getId(), barriere));

        int statutA = futureA.get(10, TimeUnit.SECONDS);
        int statutB = futureB.get(10, TimeUnit.SECONDS);

        // Aucune requête ne doit échouer : chaque étudiant distinct a le droit à sa présence.
        assertTrue(statutA < 500 && statutB < 500,
                "Aucun marquage ne doit planter (5xx) — reçu A=" + statutA + " B=" + statutB);

        // LE symptôme client : les deux présences doivent exister en base.
        assertEquals(2, presencesDeLaSession(sessionId),
                "Deux présences distinctes doivent être persistées");
    }

    @Test
    @DisplayName("Issue #33 : double-soumission simultanée du même étudiant — une seule présence, aucune erreur serveur")
    void memeEtudiantDeuxSoumissionsSimultanees() throws Exception {
        Long sessionId = sessions.findByCode(code).orElseThrow().getId();
        Etudiant a = nouvelEtudiant("Conc Double");
        CyclicBarrier barriere = new CyclicBarrier(2);

        var futureA = pool.submit(marquer(code, a.getId(), barriere));
        var futureB = pool.submit(marquer(code, a.getId(), barriere));

        int statutA = futureA.get(10, TimeUnit.SECONDS);
        int statutB = futureB.get(10, TimeUnit.SECONDS);

        // RG2 : une seule présence. Mais aucune des deux soumissions ne doit
        // produire d'erreur serveur (5xx) : le doublon doit se régler en 409 DEJA_PRESENT.
        assertTrue(statutA < 500, "Aucune soumission ne doit planter (5xx), reçu " + statutA);
        assertTrue(statutB < 500, "Aucune soumission ne doit planter (5xx), reçu " + statutB);
        assertEquals(1, presences.findAll().stream()
                        .filter(p -> sessionId.equals(p.getSessionId())
                                && a.getId().equals(p.getEtudiantId()))
                        .count(),
                "RG2 : exactement une présence pour cet étudiant sur cette session");
    }
}
