package com.finances.main.service;

/**
 * Excepción de dominio para fallos al consumir el servicio RAG externo.
 */
public class RagUnavailableException extends RuntimeException {
    public RagUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
