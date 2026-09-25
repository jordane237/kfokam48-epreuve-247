package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.TentativeCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {

    Optional<TentativeCode> findByEtudiantId(Long etudiantId);
}
