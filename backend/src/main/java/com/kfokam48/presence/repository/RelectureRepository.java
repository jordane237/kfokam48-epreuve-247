package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Relecture;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** RG16 : un seul relecteur par exercice. */
    boolean existsByExerciceId(Long exerciceId);

    /** Retrouve la relecture assignée pour un exercice donné. */
    Optional<Relecture> findByExerciceId(Long exerciceId);
}
