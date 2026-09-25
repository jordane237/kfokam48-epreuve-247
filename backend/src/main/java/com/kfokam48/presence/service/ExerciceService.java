package com.kfokam48.presence.service;

import com.kfokam48.presence.api.dto.DeposerExerciceRequest;
import com.kfokam48.presence.api.dto.ExerciceCreeDto;
import com.kfokam48.presence.api.error.BusinessException;
import com.kfokam48.presence.entity.Exercice;
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
        if (!sessions.existsById(requete.sessionId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                    "Cette session n'existe pas.");
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
