package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Exercice;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    /** RG5 : un seul exercice par couple (session, étudiant). */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** Tableau RG15 : exercices déposés sur les sessions d'une promotion. */
    List<Exercice> findBySessionIdIn(Collection<Long> sessionIds);
}
