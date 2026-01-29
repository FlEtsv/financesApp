package com.finances.main.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTOs para objetivos financieros.
 */
public final class FinancialGoalDtos {
    private FinancialGoalDtos() {
        // Utilidad estática.
    }

    /**
     * Request para crear un objetivo financiero.
     */
    public record FinancialGoalCreateRequest(
        String accountName,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate targetDate,
        String description
    ) {
    }

    /**
     * Request para sumar progreso a un objetivo.
     */
    public record FinancialGoalProgressRequest(BigDecimal amount) {
    }

    /**
     * Respuesta con el objetivo financiero.
     */
    public record FinancialGoalResponse(
        Long id,
        String accountName,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate targetDate,
        String description,
        Instant createdAt
    ) {
    }
}
