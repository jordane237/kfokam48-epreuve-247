package com.kfokam48.presence.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestion centralisée des erreurs (B4) : toute erreur renvoie exactement le
 * format imposé par le contrat — { "code": "...", "message": "..." } —
 * jamais de stack trace, jamais la page d'erreur par défaut de Spring.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inattendue(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("ERREUR_INTERNE", "Une erreur interne est survenue."));
    }
}
