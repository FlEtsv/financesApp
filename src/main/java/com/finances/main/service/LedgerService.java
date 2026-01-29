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
     * Calcula el balance (ingresos - gastos) de una cuenta por nombre.
     */
    BigDecimal calculateBalance(String accountName);

    /**
     * Resume montos por categoría en una cuenta.
     */
    Map<String, BigDecimal> totalsByCategory(Long accountId, CategoryType type);

    /**
     * Resume montos por categoría para una cuenta por nombre.
     */
    Map<String, BigDecimal> totalsByCategory(String accountName, CategoryType type);

    /**
     * Obtiene transacciones para una cuenta en un rango de fechas.
     */
    List<Transaction> listTransactions(Long accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene transacciones para una cuenta por nombre en un rango de fechas.
     */
    List<Transaction> listTransactions(String accountName, LocalDate startDate, LocalDate endDate);
}
