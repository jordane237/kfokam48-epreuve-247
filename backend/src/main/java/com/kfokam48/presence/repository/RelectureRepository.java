package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** RG16 : un seul relecteur par exercice. */
    boolean existsByExerciceId(Long exerciceId);
}
