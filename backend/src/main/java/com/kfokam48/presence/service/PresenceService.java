package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.AjouterPresenceManuelleRequest;
import com.kfokam48.presence.api.dto.MarquerPresenceRequest;
import com.kfokam48.presence.api.dto.PresenceDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.entity.TentativeCode;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.repository.TentativeCodeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF2/EF3 : marquer sa présence. Ordre de vérification imposé (D3) :
 * blocage RG4 actif → code inconnu 400 → code expiré 410 (RG1) →
 * déjà présent 409 (RG2) → enregistrement 201.
 */
@Service
public class PresenceService {

    /** RG3/Q4 : au bout de 5 erreurs, blocage de 2 minutes. */
    static final int MAX_ECHECS = 5;
    static final Duration DUREE_BLOCAGE = Duration.ofMinutes(2);

    private final PresenceRepository presences;
    private final SessionRepository sessions;
    private final TentativeCodeRepository tentatives;
    private final EtudiantRepository etudiants;

    public PresenceService(PresenceRepository presences, SessionRepository sessions,
            TentativeCodeRepository tentatives, EtudiantRepository etudiants) {
        this.presences = presences;
        this.sessions = sessions;
        this.tentatives = tentatives;
        this.etudiants = etudiants;
    }

    @Transactional
    public PresenceDto marquer(MarquerPresenceRequest requete) {
        LocalDateTime maintenant = LocalDateTime.now();
        TentativeCode tentative = tentatives.findByEtudiantId(requete.etudiantId()).orElse(null);

        // RG4 : blocage actif → 429, même si le code saisi est correct.
        if (tentative != null && tentative.getBloqueJusqua() != null) {
            if (maintenant.isBefore(tentative.getBloqueJusqua())) {
                throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "TROP_TENTATIVES",
                        "Trop de tentatives. Réessayez dans 2 minutes.");
            }
            tentative.reinitialiser(); // blocage expiré → compteur remis à zéro
        }

        Session session = sessions.findByCode(requete.code()).orElse(null);
        if (session == null) {
            echouer(tentative, requete.etudiantId(), maintenant);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CODE_INCONNU",
                    "Ce code de présence n'existe pas.");
        }

        // RG1 : au-delà de expirationAt, le code ne marche plus.
        if (maintenant.isAfter(session.getExpirationAt())) {
            echouer(tentative, requete.etudiantId(), maintenant);
            throw new BusinessException(HttpStatus.GONE, "CODE_EXPIRE",
                    "Le code de présence a expiré.");
        }

        // RG2 : un seul marquage par étudiant et par session.
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), requete.etudiantId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                    "Cet étudiant a déjà marqué sa présence.");
        }

        // Succès : le compteur d'échecs est remis à zéro (RG4).
        if (tentative != null) {
            tentative.reinitialiser();
        }

        Presence presence = insererDeManiereAtomique(
                session.getId(), requete.etudiantId(), Presence.Source.ETUDIANT);
        return new PresenceDto(presence.getId(), presence.getSessionId(),
                presence.getEtudiantId(), presence.getSource().name());
    }

    /**
     * Issue #33 : chaque présence est un INSERT atomique et indépendant — aucune
     * écriture ne dépend d'un état parent chargé en mémoire (pas de @OneToMany,
     * pas de cascade). La vérification RG2 ci-dessus reste la protection normale ;
     * la contrainte unique (session_id, etudiant_id) est le filet de sécurité : si
     * deux transactions la franchissent en même temps (TOCTOU), l'INSERT du perdant
     * est rejeté par la base et traduit en 409 DEJA_PRESENT — jamais en 500.
     */
    private Presence insererDeManiereAtomique(Long sessionId, Long etudiantId, Presence.Source source) {
        try {
            return presences.save(new Presence(sessionId, etudiantId, source, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            // Un concurrent a commité la même (session, étudiant) entre le check et l'INSERT.
            throw new BusinessException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                    "Cet étudiant a déjà marqué sa présence.");
        }
    }

    /**
     * EF12/RG11 : présence ajoutée à la main par le formateur — source=FORMATEUR
     * pour que « ça se voie » dans le tableau (Q14). Impossible après clôture (RG12) ;
     * en revanche possible après expiration du code (Q12 : clôture ≠ expiration).
     */
    @Transactional
    public PresenceDto presenceManuelle(Long sessionId, AjouterPresenceManuelleRequest requete) {
        Session session = sessions.findById(sessionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                        "Cette session n'existe pas."));
        if (session.getClotureAt() != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                    "La session est clôturée, la présence ne peut plus être ajoutée.");
        }
        if (!etudiants.existsById(requete.etudiantId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU",
                    "Cet étudiant n'existe pas.");
        }
        // RG2 : un seul marquage par étudiant et par session, quelle que soit la source.
        if (presences.existsBySessionIdAndEtudiantId(sessionId, requete.etudiantId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                    "Cet étudiant a déjà marqué sa présence.");
        }

        // Même filet de sécurité que le marquage étudiant (issue #33).
        Presence presence = insererDeManiereAtomique(sessionId, requete.etudiantId(), Presence.Source.FORMATEUR);
        return new PresenceDto(presence.getId(), presence.getSessionId(),
                presence.getEtudiantId(), presence.getSource().name());
    }

    private void echouer(TentativeCode tentative, Long etudiantId, LocalDateTime maintenant) {
        if (tentative == null) {
            tentative = new TentativeCode(etudiantId);
        }
        tentative.enregistrerEchec(MAX_ECHECS, DUREE_BLOCAGE, maintenant);
        tentatives.save(tentative);
    }
}
