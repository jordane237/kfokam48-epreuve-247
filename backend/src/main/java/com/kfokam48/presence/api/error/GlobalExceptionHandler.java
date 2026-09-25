package com.kfokam48.presence.api.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestion centralisée des erreurs (B4) : toute erreur renvoie exactement le
 * format imposé par le contrat — { "code": "...", "message": "..." } —
 * jamais de stack trace, jamais la page d'erreur par défaut de Spring.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> business(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> "Le champ « " + err.getField() + " » est obligatoire.")
                .orElse("Requête invalide.");
        return ResponseEntity.badRequest().body(new ApiError("CHAMP_MANQUANT", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> corpsInvalide(HttpMessageNotReadableException ex) {
        // Une note décimale (ex. 12.5) ou un corps illisible arrive ici : RG8 → NOTE_INVALIDE.
        return ResponseEntity.badRequest().body(new ApiError("NOTE_INVALIDE",
                "La note doit être un entier entre 0 et 20."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> ressourceInconnue(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("RESSOURCE_INCONNUE", "Cette adresse n'existe pas."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inattendue(Exception ex) {
        // Une 500 est par définition imprévue : sans log, elle est indémontrable.
        logger.error("Erreur interne non gérée", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("ERREUR_INTERNE", "Une erreur interne est survenue."));
    }
}
