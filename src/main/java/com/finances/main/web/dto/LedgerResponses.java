package com.finances.main.web.dto;

import com.finances.main.model.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs compartidos para las respuestas del libro mayor.
 */
public final class LedgerResponses {
    private LedgerResponses() {
        // Constructor privado para evitar instanciación.
    }

    /**
     * Respuesta con balance de cuenta.
     */
    public record BalanceResponse(Long accountId, BigDecimal balance) {
    }

    /**
     * Respuesta con totales por categoría.
     */
    public record CategoryTotalsResponse(Long accountId, CategoryType type, Map<String, BigDecimal> totals) {
    }

    /**
     * Respuesta con lista de transacciones resumidas.
     */
    public record TransactionsResponse(
        Long accountId,
        LocalDate startDate,
        LocalDate endDate,
        List<TransactionSummary> transactions
    ) {
    }

    /**
     * DTO de transacción resumida para consumo web.
     */
    public record TransactionSummary(
        Long id,
        BigDecimal amount,
        LocalDate transactionDate,
        String description,
        String categoryName,
        CategoryType categoryType
    ) {
    }
}
