package com.finances.main.web.dto;

import java.time.Instant;

/**
 * DTOs para operaciones de cuentas.
 */
public final class AccountDtos {
    private AccountDtos() {
        // Utilidad estática.
    }

    /**
     * Request para crear una cuenta.
     */
    public record AccountCreateRequest(String name, String currency) {
    }

    /**
     * Resumen de cuenta para respuesta.
     */
    public record AccountSummary(Long id, String name, String currency, Instant createdAt) {
    }
}
