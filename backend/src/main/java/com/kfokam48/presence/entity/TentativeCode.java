package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Suivi des échecs consécutifs de saisie du code (RG3/RG4) — table `tentative_code`.
 * Après 5 échecs consécutifs (code inconnu ou expiré), l'étudiant est bloqué
 * 2 minutes, même avec un code correct. Le compteur repart à zéro après un
 * succès ou l'expiration du blocage.
 */
@Entity
@Table(name = "tentative_code")
public class TentativeCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etudiant_id", nullable = false, unique = true)
    private Long etudiantId;

    @Column(name = "echecs_consicutifs", nullable = false)
    private int echecsConsicutifs;

    @Column(name = "bloque_jusqua")
    private LocalDateTime bloqueJusqua;

    protected TentativeCode() {
    }

    public TentativeCode(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    /** Enregistre un échec ; au 5e consécutif, pose le blocage de 2 minutes (RG4). */
    public void enregistrerEchec(int maxEchecs, Duration dureeBlocage, LocalDateTime maintenant) {
        this.echecsConsicutifs++;
        if (this.echecsConsicutifs >= maxEchecs) {
            this.bloqueJusqua = maintenant.plus(dureeBlocage);
            this.echecsConsicutifs = 0; // le compteur repart après le blocage
        }
    }

    /** Remise à zéro : après un succès ou l'expiration du blocage (RG4). */
    public void reinitialiser() {
        this.echecsConsicutifs = 0;
        this.bloqueJusqua = null;
    }

    public boolean estBloque(LocalDateTime maintenant) {
        return bloqueJusqua != null && maintenant.isBefore(bloqueJusqua);
    }

    public Long getId() {
        return id;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public int getEchecsConsicutifs() {
        return echecsConsicutifs;
    }

    public LocalDateTime getBloqueJusqua() {
        return bloqueJusqua;
    }
}
