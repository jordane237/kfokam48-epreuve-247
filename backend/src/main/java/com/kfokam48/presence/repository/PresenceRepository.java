package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Presence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    /** RG2 : unicité d'une présence par session et par étudiant. */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** RG13 : les étudiants présents à une session, candidats relecteurs. */
    List<Presence> findBySessionId(Long sessionId);
}
