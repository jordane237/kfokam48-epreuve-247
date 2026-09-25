package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Session;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    boolean existsByCode(String code);

    /** Retrouve une session par son code de présence (EF2). */
    Optional<Session> findByCode(String code);
}
