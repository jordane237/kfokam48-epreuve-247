package com.kfokam48.presence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Test unitaire B6 (T5) : le cas « aucun relecteur disponible » (RG14) —
 * l'exercice passe à en_attente_relecteur SANS lever d'erreur, et le tirage
 * RG13 exclut l'auteur. Sans base : repositories simulés.
 */
class AssignationServiceTest {

    private PresenceRepository presences;
    private RelectureRepository relectures;
    private ExerciceRepository exercices;
    private AssignationService service;

    private final Exercice exercice = new Exercice(1L, 10L, "https://exemple.com/exo",
            Exercice.Statut.depose, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        presences = mock(PresenceRepository.class);
        relectures = mock(RelectureRepository.class);
        exercices = mock(ExerciceRepository.class);
        when(exercices.save(any(Exercice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(relectures.save(any(Relecture.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new AssignationService(presences, relectures, exercices, new Random(42));
    }

    @Test
    @DisplayName("RG14 : aucun candidat → en_attente_relecteur, aucune erreur, aucune relecture créée")
    void aucunRelecteurDisponible() {
        // Seul l'auteur (10) est présent : personne d'éligible
        when(presences.findBySessionId(1L))
                .thenReturn(List.of(new Presence(1L, 10L, Presence.Source.ETUDIANT, LocalDateTime.now())));

        Exercice.Statut statut = service.assigner(exercice);

        assertEquals(Exercice.Statut.en_attente_relecteur, statut);
        verify(relectures, never()).save(any(Relecture.class)); // aucune erreur n'a interrompu le flux
    }

    @Test
    @DisplayName("RG13/RG7 : les relecteurs sont tirés parmi les présents, jamais l'auteur (RG16 étape 3 : deux distincts)")
    void tirageExclutLAuteur() {
        when(presences.findBySessionId(1L)).thenReturn(List.of(
                new Presence(1L, 10L, Presence.Source.ETUDIANT, LocalDateTime.now()), // l'auteur
                new Presence(1L, 11L, Presence.Source.ETUDIANT, LocalDateTime.now()),
                new Presence(1L, 12L, Presence.Source.FORMATEUR, LocalDateTime.now()))); // présence manuelle Q14

        Exercice.Statut statut = service.assigner(exercice);

        assertEquals(Exercice.Statut.assigne, statut);
        ArgumentCaptor<Relecture> captor = ArgumentCaptor.forClass(Relecture.class);
        verify(relectures, times(2)).save(captor.capture());
        List<Long> elus = captor.getAllValues().stream().map(Relecture::getRelecteurId).toList();
        assertEquals(2, elus.size(), "RG16 étape 3 : deux affectations créées");
        assertTrue(elus.stream().allMatch(r -> r == 11L || r == 12L), "relecteurs élus : " + elus);
        assertTrue(!elus.get(0).equals(elus.get(1)), "les deux relecteurs sont distincts : " + elus);
    }
}
