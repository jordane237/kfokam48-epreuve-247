package com.kfokam48.presence.entity;

import jakarta.persistence.*;

/** Entité Etudiant — table `etudiant` (V1). L'étudiant choisit son nom dans une liste (Q1). */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(nullable = false)
    private String nom;

    protected Etudiant() {
    }

    public Etudiant(Long promotionId, String nom) {
        this.promotionId = promotionId;
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public String getNom() {
        return nom;
    }
}
