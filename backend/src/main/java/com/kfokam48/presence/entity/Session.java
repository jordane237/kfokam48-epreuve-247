package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Entité Session — table `session`, conforme à la migration V1 (D2). */
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    /** Code de présence court, non ambigu (pas de 0/O/1/I) — EF1. */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private LocalDateTime ouvertureAt;

    /** RG1 : ouvertureAt + 15 minutes. */
    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationAt;

    /** Nullable : NULL = session ouverte au dépôt (RG12). Renseigné par T14. */
    @Column(name = "cloture_at")
    private LocalDateTime clotureAt;

    protected Session() {
    }

    public Session(String titre, Long promotionId, String code, LocalDateTime ouvertureAt, LocalDateTime expirationAt) {
        this.titre = titre;
        this.promotionId = promotionId;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getOuvertureAt() {
        return ouvertureAt;
    }

    public LocalDateTime getExpirationAt() {
        return expirationAt;
    }

    public LocalDateTime getClotureAt() {
        return clotureAt;
    }

    /** RG12 : renseigné par la clôture manuelle du formateur. */
    public void setClotureAt(LocalDateTime clotureAt) {
        this.clotureAt = clotureAt;
    }
}
