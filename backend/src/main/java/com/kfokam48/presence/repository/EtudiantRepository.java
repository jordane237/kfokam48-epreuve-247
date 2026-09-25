package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Etudiant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByPromotionIdOrderById(Long promotionId);
}
