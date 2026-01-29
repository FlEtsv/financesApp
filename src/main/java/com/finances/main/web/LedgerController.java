package com.finances.main.web;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import com.finances.main.service.TransactionService;
import com.finances.main.web.dto.LedgerResponses.BalanceByNameResponse;
import com.finances.main.web.dto.LedgerResponses.BalanceResponse;
import com.finances.main.web.dto.LedgerResponses.CategoryTotalsByNameResponse;
import com.finances.main.web.dto.LedgerResponses.CategoryTotalsResponse;
import com.finances.main.web.dto.LedgerResponses.TransactionSummary;
import com.finances.main.web.dto.LedgerResponses.TransactionsByNameResponse;
import com.finances.main.web.dto.LedgerResponses.TransactionsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST para exponer datos del libro mayor a clientes web.
 */
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
    private final LedgerService ledgerService;
    private final TransactionService transactionService;

    public LedgerController(LedgerService ledgerService, TransactionService transactionService) {
        this.ledgerService = ledgerService;
        this.transactionService = transactionService;
    }

    /**
     * Devuelve el balance de una cuenta por id.
     */
    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        BigDecimal balance = ledgerService.calculateBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }

    /**
     * Devuelve el balance de una cuenta por nombre.
     */
    @GetMapping("/accounts/by-name/{accountName}/balance")
    public BalanceByNameResponse getBalanceByName(@PathVariable String accountName) {
        BigDecimal balance = ledgerService.calculateBalance(accountName);
        return new BalanceByNameResponse(accountName, balance);
    }

    /**
     * Devuelve totales agrupados por categoría usando id de cuenta.
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
     * Devuelve totales agrupados por categoría usando nombre de cuenta.
     */
    @GetMapping("/accounts/by-name/{accountName}/totals")
    public CategoryTotalsByNameResponse getTotalsByCategoryByName(
        @PathVariable String accountName,
        @RequestParam CategoryType type
    ) {
        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(accountName, type);
        return new CategoryTotalsByNameResponse(accountName, type, totals);
    }

    /**
     * Devuelve transacciones en un rango de fechas para una cuenta por id.
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

    /**
     * Devuelve transacciones en un rango de fechas para una cuenta por nombre.
     */
    @GetMapping("/accounts/by-name/{accountName}/transactions")
    public TransactionsByNameResponse getTransactionsByName(
        @PathVariable String accountName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Transaction> transactions = ledgerService.listTransactions(accountName, startDate, endDate);
        List<TransactionSummary> summaries = transactions.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return new TransactionsByNameResponse(accountName, startDate, endDate, summaries);
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

    @DeleteMapping("/delete/transaction/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        transactionService.deleteTransactionById(id);
        return ResponseEntity.noContent().build();
    }
}

