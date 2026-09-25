package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    boolean existsByCode(String code);
}
