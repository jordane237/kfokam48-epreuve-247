package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Exercice — table `exercice` (V5).
 * Les constantes de statut sont volontairement en minuscules : elles sont
 * stockées telles quelles et exposées telles quelles, conformément au contrat
 * et au diagramme D4 (depose → en_attente_relecteur | assigne → relu_partiel → relu,
 * étape 3 : relu_partiel = une relecture rendue sur deux, note PROVISOIRE).
 */
@Entity
@Table(name = "exercice")
public class Exercice {

    public enum Statut {
        depose, en_attente_relecteur, assigne, relu_partiel, relu
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(nullable = false)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    @Column(name = "depose_at", nullable = false)
    private LocalDateTime deposeAt;

    @Column(name = "maj_at")
    private LocalDateTime majAt;

    protected Exercice() {
    }

    public Exercice(Long sessionId, Long etudiantId, String lien, Statut statut, LocalDateTime deposeAt) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.lien = lien;
        this.statut = statut;
        this.deposeAt = deposeAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDeposeAt() {
        return deposeAt;
    }

    public LocalDateTime getMajAt() {
        return majAt;
    }

    public void setMajAt(LocalDateTime majAt) {
        this.majAt = majAt;
    }
}
