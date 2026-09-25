package com.kfokam48.presence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kfokam48.presence.api.dto.MarquerPresenceRequest;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.entity.TentativeCode;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.repository.TentativeCodeRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire B6 sur des règles métier réelles :
 * RG1 (expiration du code à 15 min) et RG4 (blocage après 5 échecs).
 * Tourne sur un poste vierge : aucune base, repositories simulés en mémoire.
 */
class PresenceServiceTest {

    private PresenceService service;
    private SessionRepository sessions;
    private PresenceRepository presences;
    private Map<Long, TentativeCode> tentativesEnMemoire;

    private final Session sessionValide = new Session("Cours", 1L, "ABC234",
            LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(10));
    private final Session sessionExpiree = new Session("Cours", 1L, "XYZ789",
            LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(15));

    @BeforeEach
    void setUp() {
        sessions = mock(SessionRepository.class);
        presences = mock(PresenceRepository.class);
        TentativeCodeRepository tentatives = mock(TentativeCodeRepository.class);
        tentativesEnMemoire = new HashMap<>();

        // Simulations en mémoire : le comportement des règles RG3/RG4 est réel,
        // porté par l'entité TentativeCode, pas par les mocks.
        when(tentatives.findByEtudiantId(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(tentativesEnMemoire.get(inv.getArgument(0))));
        when(tentatives.save(any(TentativeCode.class))).thenAnswer(inv -> {
            TentativeCode t = inv.getArgument(0);
            tentativesEnMemoire.put(t.getEtudiantId(), t);
            return t;
        });
        when(presences.save(any(Presence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(presences.existsBySessionIdAndEtudiantId(anyLong(), anyLong())).thenReturn(false);
        when(sessions.findByCode(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            if (sessionValide.getCode().equals(code)) {
                return Optional.of(sessionValide);
            }
            if (sessionExpiree.getCode().equals(code)) {
                return Optional.of(sessionExpiree);
            }
            return Optional.empty();
        });

        service = new PresenceService(presences, sessions, tentatives);
    }

    private BusinessException marquer(String code, long etudiantId) {
        return assertThrows(BusinessException.class, () -> service.marquer(new MarquerPresenceRequest(code, etudiantId)));
    }

    @Test
    @DisplayName("RG1 : un code expiré renvoie 410 CODE_EXPIRE, même correctement saisi")
    void rg1_codeExpire() {
        BusinessException e = marquer("XYZ789", 1L); // expirationAt il y a 15 min
        assertEquals(410, e.getStatus().value());
        assertEquals("CODE_EXPIRE", e.getCode());
    }

    @Test
    @DisplayName("RG4 : après 5 échecs consécutifs, même un code valide renvoie 429 TROP_TENTATIVES")
    void rg4_blocageApresCinqEchecs() {
        // 4 échecs avec un code inconnu → 4 x 400, pas encore bloqué
        for (int i = 0; i < 4; i++) {
            BusinessException e = marquer("INCONNU", 1L);
            assertEquals(400, e.getStatus().value(), "échec " + (i + 1));
            assertEquals("CODE_INCONNU", e.getCode());
        }

        // 5e échec : le blocage de 2 minutes est posé (la tentative elle-même renvoie 400)
        BusinessException cinquieme = marquer("INCONNU", 1L);
        assertEquals(400, cinquieme.getStatus().value());
        assertEquals("CODE_INCONNU", cinquieme.getCode());

        // 6e tentative, cette fois avec un code VALIDE et non expiré → 429 (RG4)
        BusinessException e = marquer("ABC234", 1L);
        assertEquals(429, e.getStatus().value());
        assertEquals("TROP_TENTATIVES", e.getCode());
    }

    @Test
    @DisplayName("RG4 : un succès remet le compteur d'échecs à zéro")
    void rg4_succesRemetCompteur() {
        marquer("INCONNU", 2L); // 1 échec
        marquer("INCONNU", 2L); // 2e échec

        // Succès avec un code valide : aucune exception, présence enregistrée
        var presence = service.marquer(new MarquerPresenceRequest("ABC234", 2L));
        assertEquals("ETUDIANT", presence.source());

        // Les échecs précédents ont été oubliés : 4 nouveaux échecs ne suffisent pas à bloquer
        for (int i = 0; i < 4; i++) {
            BusinessException e = marquer("INCONNU", 2L);
            assertEquals(400, e.getStatus().value());
        }
    }

    @Test
    @DisplayName("RG2 : un étudiant déjà présent renvoie 409 DEJA_PRESENT")
    void rg2_dejaPresent() {
        // any() et non anyLong() : l'id de session d'une entité non persistée est null
        when(presences.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(true);
        BusinessException e = marquer("ABC234", 3L);
        assertEquals(409, e.getStatus().value());
        assertEquals("DEJA_PRESENT", e.getCode());
    }
}
