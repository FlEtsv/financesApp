package com.finances.main.web;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST para exponer datos del libro mayor a clientes web.
 */
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Devuelve el balance de una cuenta.
     */
    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        BigDecimal balance = ledgerService.calculateBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }

    /**
     * Devuelve totales agrupados por categoría.
     */
    @GetMapping("/accounts/{accountId}/totals")
    public CategoryTotalsResponse getTotalsByCategory(
        @PathVariable Long accountId,
        @RequestParam CategoryType type
    ) {
        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(accountId, type);
        return new CategoryTotalsResponse(accountId, type, totals);
    }

    /**
     * Devuelve transacciones en un rango de fechas.
     */
    @GetMapping("/accounts/{accountId}/transactions")
    public TransactionsResponse getTransactions(
        @PathVariable Long accountId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Transaction> transactions = ledgerService.listTransactions(accountId, startDate, endDate);
        List<TransactionSummary> summaries = transactions.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return new TransactionsResponse(accountId, startDate, endDate, summaries);
    }

    private TransactionSummary toSummary(Transaction transaction) {
        return new TransactionSummary(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getDescription(),
            transaction.getCategory().getName(),
            transaction.getCategory().getType()
        );
    }
}

/**
 * Respuesta con balance de cuenta.
 */
record BalanceResponse(Long accountId, BigDecimal balance) {
}

/**
 * Respuesta con totales por categoría.
 */
record CategoryTotalsResponse(Long accountId, CategoryType type, Map<String, BigDecimal> totals) {
}

/**
 * Respuesta con lista de transacciones resumidas.
 */
record TransactionsResponse(Long accountId, LocalDate startDate, LocalDate endDate, List<TransactionSummary> transactions) {
}

/**
 * DTO de transacción resumida para consumo web.
 */
record TransactionSummary(
    Long id,
    BigDecimal amount,
    LocalDate transactionDate,
    String description,
    String categoryName,
    CategoryType categoryType
) {
}
