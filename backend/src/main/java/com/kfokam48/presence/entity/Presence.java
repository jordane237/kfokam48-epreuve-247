package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Entité Presence — table `presence` (V3). Source : ETUDIANT ou FORMATEUR (RG11). */
@Entity
@Table(name = "presence")
public class Presence {

    public enum Source {
        ETUDIANT, FORMATEUR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(name = "marque_at", nullable = false)
    private LocalDateTime marqueAt;

    protected Presence() {
    }

    public Presence(Long sessionId, Long etudiantId, Source source, LocalDateTime marqueAt) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.source = source;
        this.marqueAt = marqueAt;
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

    public Source getSource() {
        return source;
    }

    public LocalDateTime getMarqueAt() {
        return marqueAt;
    }
}
