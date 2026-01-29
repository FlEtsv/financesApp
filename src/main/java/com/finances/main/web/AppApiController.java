package com.finances.main.web;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import com.finances.main.web.dto.LedgerResponses.BalanceResponse;
import com.finances.main.web.dto.LedgerResponses.CategoryTotalsResponse;
import com.finances.main.web.dto.LedgerResponses.TransactionSummary;
import com.finances.main.web.dto.LedgerResponses.TransactionsResponse;
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
 * Endpoints /app/api para consumo de la interfaz web básica.
 */
@RestController
@RequestMapping("/app/api")
public class AppApiController {
    private final LedgerService ledgerService;

    public AppApiController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Devuelve el balance de una cuenta para la interfaz web.
     */
    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        BigDecimal balance = ledgerService.calculateBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }

    /**
     * Devuelve totales agrupados por categoría para la interfaz web.
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
     * Devuelve transacciones en un rango de fechas para la interfaz web.
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
