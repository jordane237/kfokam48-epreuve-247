package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Relecture;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** RG16 : un seul relecteur par exercice. */
    boolean existsByExerciceId(Long exerciceId);

    /** Retrouve la relecture assignée pour un exercice donné. */
    Optional<Relecture> findByExerciceId(Long exerciceId);

    /** Tableau RG15 : relectures des exercices d'une promotion (notes reçues, en attente). */
    List<Relecture> findByExerciceIdIn(Collection<Long> exerciceIds);

    /** Écran relecteur : les relectures assignées et pas encore rendues d'un étudiant. */
    List<Relecture> findByRelecteurIdAndStatut(Long relecteurId, Relecture.Statut statut);
}
