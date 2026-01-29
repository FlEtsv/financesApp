package com.finances.main.web.dto;

import java.math.BigDecimal;
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
    public record AccountCreateRequest(String name, String currency, BigDecimal initialBalance) {
    }

    /**
     * Resumen de cuenta para respuesta.
     */
    public record AccountSummary(
        Long id,
        String name,
        String currency,
        BigDecimal initialBalance,
        Instant createdAt
    ) {
    }

    /**
     * Request para actualizar el saldo inicial de una cuenta.
     */
    public record AccountBalanceUpdateRequest(BigDecimal initialBalance) {
    }
}
