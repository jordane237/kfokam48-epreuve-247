package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    /** RG2 : unicité d'une présence par session et par étudiant. */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
