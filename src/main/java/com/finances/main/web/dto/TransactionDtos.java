package com.finances.main.web.dto;

import com.finances.main.model.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs para operaciones de transacciones.
 */
public final class TransactionDtos {
    private TransactionDtos() {
        // Utilidad estática.
    }

    /**
     * Request para registrar un movimiento.
     */
    public record TransactionCreateRequest(
        String accountName,
        String categoryName,
        CategoryType categoryType,
        BigDecimal amount,
        LocalDate transactionDate,
        String description
    ) {
    }

    /**
     * Respuesta para movimientos registrados.
     */
    public record TransactionResponse(
        Long id,
        String accountName,
        String categoryName,
        CategoryType categoryType,
        BigDecimal amount,
        LocalDate transactionDate,
        String description
    ) {
    }
}
