package com.kfokam48.presence.api.error;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : porte le statut HTTP et le code d'erreur du contrat.
 * Chaque règle de gestion (RGx) qui refuse une opération lève une sous-classe
 * ou une instance de cette exception ; GlobalExceptionHandler la traduit
 * en réponse { code, message } sans stack trace (B4).
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
