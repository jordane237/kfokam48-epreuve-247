package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Relecture — table `relecture` (V6).
 * Le relecteur est un étudiant (pas un acteur distinct) : relecteurId pointe
 * vers `etudiant`. Un seul relecteur par exercice (RG16) ; une relecture
 * rendue est définitive (RG10).
 */
@Entity
@Table(name = "relecture")
public class Relecture {

    public enum Statut {
        assignee, rendue
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice_id", nullable = false)
    private Long exerciceId;

    @Column(name = "relecteur_id", nullable = false)
    private Long relecteurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    @Column
    private Integer note;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "assignee_at", nullable = false)
    private LocalDateTime assigneeAt;

    @Column(name = "rendue_at")
    private LocalDateTime rendueAt;

    protected Relecture() {
    }

    public Relecture(Long exerciceId, Long relecteurId, LocalDateTime assigneeAt) {
        this.exerciceId = exerciceId;
        this.relecteurId = relecteurId;
        this.statut = Statut.assignee;
        this.assigneeAt = assigneeAt;
    }

    public Long getId() {
        return id;
    }

    public Long getExerciceId() {
        return exerciceId;
    }

    public Long getRelecteurId() {
        return relecteurId;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getAssigneeAt() {
        return assigneeAt;
    }

    public LocalDateTime getRendueAt() {
        return rendueAt;
    }

    public void setRendueAt(LocalDateTime rendueAt) {
        this.rendueAt = rendueAt;
    }
}
