package com.finances.main.service;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Orquesta operaciones de negocio para el libro mayor.
 */
public interface LedgerService {
    /**
     * Calcula el balance (ingresos - gastos) de una cuenta.
     */
    BigDecimal calculateBalance(Long accountId);

    /**
     * Resume montos por categoría en una cuenta.
     */
    Map<String, BigDecimal> totalsByCategory(Long accountId, CategoryType type);

    /**
     * Obtiene transacciones para una cuenta en un rango de fechas.
     */
    List<Transaction> listTransactions(Long accountId, LocalDate startDate, LocalDate endDate);
}
