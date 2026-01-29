package com.finances.main.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs para presupuesto y resúmenes mensuales.
 */
public final class BudgetDtos {
    private BudgetDtos() {
        // Utilidad estática.
    }

    /**
     * Resumen presupuestario para un rango.
     */
    public record BudgetSummaryResponse(
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal initialBalance,
        BigDecimal fixedIncome,
        BigDecimal fixedExpense,
        BigDecimal actualIncome,
        BigDecimal actualExpense,
        BigDecimal expectedBalance,
        BigDecimal actualBalance
    ) {
    }

    /**
     * Resumen mensual para un año.
     */
    public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
    ) {
    }
}
