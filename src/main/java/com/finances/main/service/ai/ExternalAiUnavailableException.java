package com.finances.main.service.ai;

/**
 * Excepción para indicar que el proveedor de IA no respondió correctamente.
 */
public class ExternalAiUnavailableException extends RuntimeException {
    public ExternalAiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
