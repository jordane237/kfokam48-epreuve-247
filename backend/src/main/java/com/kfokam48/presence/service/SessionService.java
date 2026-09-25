package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.OuvrirSessionRequest;
import com.kfokam48.presence.api.dto.SessionCreeeDto;
import com.kfokam48.presence.api.dto.SessionDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    /** Alphabet sans 0/O/1/I : codes courts non ambigus, lisibles à l'écran (EF1). */
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 6;

    /** RG1 : le code expire 15 minutes après l'ouverture (Q2). */
    static final Duration DUREE_VIE_CODE = Duration.ofMinutes(15);

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final SecureRandom aleatoire = new SecureRandom();

    public SessionService(SessionRepository sessions, PromotionRepository promotions) {
        this.sessions = sessions;
        this.promotions = promotions;
    }

    @Transactional
    public SessionCreeeDto ouvrir(OuvrirSessionRequest requete) {
        if (!promotions.existsById(requete.promotionId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Cette promotion n'existe pas.");
        }

        LocalDateTime ouverture = LocalDateTime.now();
        String code = genererCode();
        while (sessions.existsByCode(code)) {
            code = genererCode();
        }

        Session session = new Session(
                requete.titre(),
                requete.promotionId(),
                code,
                ouverture,
                ouverture.plus(DUREE_VIE_CODE)); // RG1
        session = sessions.save(session);

        return new SessionCreeeDto(session.getId(), session.getCode(),
                session.getOuvertureAt(), session.getExpirationAt());
    }

    /**
     * EF7/RG12 : clôture manuelle du formateur — ferme le dépôt d'exercices et
     * la présence manuelle. Action distincte de l'expiration du code (RG1, Q12).
     */
    @Transactional
    public SessionDto cloturer(Long id) {
        Session session = sessions.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                        "Cette session n'existe pas."));
        if (session.getClotureAt() != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "SESSION_DEJA_CLOTUREE",
                    "La session est déjà clôturée.");
        }
        session.setClotureAt(LocalDateTime.now());
        return versDto(sessions.save(session));
    }

    /** Vue complète d'une session (contrat — schéma Session, inclut clotureAt). */
    @Transactional(readOnly = true)
    public SessionDto consulter(Long id) {
        return versDto(sessions.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                        "Cette session n'existe pas.")));
    }

    private SessionDto versDto(Session s) {
        return new SessionDto(s.getId(), s.getTitre(), s.getPromotionId(), s.getCode(),
                s.getOuvertureAt(), s.getExpirationAt(), s.getClotureAt());
    }

    private String genererCode() {
        StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            sb.append(ALPHABET.charAt(aleatoire.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
