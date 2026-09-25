package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.DeposerExerciceRequest;
import com.kfokam48.presence.api.dto.ExerciceCreeDto;
import com.kfokam48.presence.api.dto.ExerciceDto;
import com.kfokam48.presence.api.dto.RemplacerLienRequest;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF5 : déposer le lien de son exercice.
 * Hypothèse documentée (cahier des charges §7) : le dépôt n'exige pas d'avoir
 * marqué sa présence — aucun contrôle de présence ici, volontairement.
 * Juste après un dépôt réussi, l'assignation d'un relecteur est tentée (EF8) ;
 * son éventuel échec silencieux (RG14) ne fait jamais échouer le dépôt.
 */
@Service
public class ExerciceService {

    private final ExerciceRepository exercices;
    private final SessionRepository sessions;
    private final AssignationService assignation;

    public ExerciceService(ExerciceRepository exercices, SessionRepository sessions,
            AssignationService assignation) {
        this.exercices = exercices;
        this.sessions = sessions;
        this.assignation = assignation;
    }

    @Transactional
    public ExerciceCreeDto deposer(DeposerExerciceRequest requete) {
        Session session = sessions.findById(requete.sessionId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                        "Cette session n'existe pas."));

        // RG12 : la clôture ferme le dépôt d'exercices (Q3, Q12).
        if (session.getClotureAt() != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                    "La session est clôturée, le dépôt d'exercice est fermé.");
        }

        validerLien(requete.lien());

        // RG5 : un seul exercice par étudiant et par session.
        if (exercices.existsBySessionIdAndEtudiantId(requete.sessionId(), requete.etudiantId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
                    "Un exercice a déjà été déposé pour cette session.");
        }

        Exercice exercice = exercices.save(new Exercice(
                requete.sessionId(), requete.etudiantId(), requete.lien(),
                Exercice.Statut.depose, LocalDateTime.now()));

        // EF8 : le statut renvoyé reflète l'assignation (depose → assigne | en_attente_relecteur).
        Exercice.Statut statutFinal = assignation.assigner(exercice);
        return new ExerciceCreeDto(exercice.getId(), statutFinal.name());
    }

    /**
     * EF6/RG6 : remplacer le lien tant que personne n'a commencé la relecture
     * (Q13) — refusé dès que l'exercice est assigne ou relu, et après clôture (RG12).
     */
    @Transactional
    public ExerciceDto remplacerLien(Long id, RemplacerLienRequest requete) {
        Exercice exercice = exercices.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU",
                        "Cet exercice n'existe pas."));

        Session session = sessions.findById(exercice.getSessionId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                        "Cette session n'existe pas."));
        if (session.getClotureAt() != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                    "La session est clôturée.");
        }

        // RG6/Q13 : le remplacement est interdit dès qu'un relecteur a été assigné.
        if (exercice.getStatut() == Exercice.Statut.assigne
                || exercice.getStatut() == Exercice.Statut.relu) {
            throw new BusinessException(HttpStatus.CONFLICT, "RELECTURE_DEJA_COMMENCEE",
                    "La relecture a commencé, le lien ne peut plus être remplacé.");
        }

        validerLien(requete.lien());
        exercice.setLien(requete.lien());
        exercice.setMajAt(LocalDateTime.now());
        Exercice maj = exercices.save(exercice);
        return new ExerciceDto(maj.getId(), maj.getSessionId(), maj.getEtudiantId(),
                maj.getLien(), maj.getStatut().name(), maj.getDeposeAt(), maj.getMajAt());
    }

    /** 400 LIEN_INVALIDE si l'URL n'est pas exploitable (http/https avec hôte). */
    private void validerLien(String lien) {
        try {
            URI uri = URI.create(lien);
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
                        "Le lien fourni n'est pas une URL valide.");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
                    "Le lien fourni n'est pas une URL valide.");
        }
    }
}
