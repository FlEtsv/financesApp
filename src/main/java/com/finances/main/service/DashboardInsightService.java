package com.finances.main.service;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.repository.TransactionRepository;
import com.finances.main.service.BudgetService.BudgetSummary;
import com.finances.main.web.dto.DashboardDtos.DashboardAlert;
import com.finances.main.web.dto.DashboardDtos.DashboardInsightsResponse;
import com.finances.main.web.dto.DashboardDtos.DashboardTopCategory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Construye indicadores y alertas para el dashboard principal.
 */
@Service
@Transactional(readOnly = true)
public class DashboardInsightService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MIN_VARIANCE_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal EXPENSE_OVERAGE_MULTIPLIER = new BigDecimal("1.10");
    private static final BigDecimal CONCENTRATION_THRESHOLD = new BigDecimal("0.50");

    private final BudgetService budgetService;
    private final LedgerService ledgerService;
    private final TransactionRepository transactionRepository;

    public DashboardInsightService(
        BudgetService budgetService,
        LedgerService ledgerService,
        TransactionRepository transactionRepository
    ) {
        this.budgetService = budgetService;
        this.ledgerService = ledgerService;
        this.transactionRepository = transactionRepository;
    }

    public DashboardInsightsResponse buildInsights(String accountName, LocalDate startDate, LocalDate endDate) {
        BudgetSummary summary = budgetService.buildSummary(accountName, startDate, endDate);
        BigDecimal actualIncome = summary.actualIncome();
        BigDecimal actualExpense = summary.actualExpense();
        BigDecimal netCashflow = actualIncome.subtract(actualExpense);
        BigDecimal savingsRate = calculateSavingsRate(netCashflow, actualIncome);

        List<Transaction> transactions = ledgerService.listTransactions(accountName, startDate, endDate);
        int transactionsCount = transactions.size();
        LocalDate lastMovementDate = transactions.isEmpty()
            ? null
            : transactions.get(transactions.size() - 1).getTransactionDate();

        DashboardTopCategory topExpenseCategory = resolveTopCategory(accountName, startDate, endDate);
        BigDecimal variance = summary.actualBalance().subtract(summary.expectedBalance());
        String reconciliationStatus = resolveReconciliationStatus(variance, summary.expectedBalance());

        List<DashboardAlert> alerts = buildAlerts(
            summary,
            actualIncome,
            actualExpense,
            netCashflow,
            variance,
            topExpenseCategory,
            transactionsCount
        );

        return new DashboardInsightsResponse(
            summary.accountName(),
            summary.startDate(),
            summary.endDate(),
            netCashflow,
            savingsRate,
            actualIncome,
            actualExpense,
            summary.expectedBalance(),
            summary.actualBalance(),
            variance,
            reconciliationStatus,
            lastMovementDate,
            transactionsCount,
            topExpenseCategory,
            alerts
        );
    }

    private DashboardTopCategory resolveTopCategory(String accountName, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.totalsByCategoryAndAccountNameWithinDateRange(
                accountName,
                CategoryType.GASTO,
                startDate,
                endDate
            ).stream()
            .map(row -> new DashboardTopCategory((String) row[0], (BigDecimal) row[1]))
            .max(Comparator.comparing(DashboardTopCategory::amount))
            .orElse(null);
    }

    private BigDecimal calculateSavingsRate(BigDecimal netCashflow, BigDecimal income) {
        if (income == null || income.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return netCashflow
            .divide(income, 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED);
    }

    private String resolveReconciliationStatus(BigDecimal variance, BigDecimal expectedBalance) {
        BigDecimal threshold = expectedBalance.abs().multiply(new BigDecimal("0.10"));
        if (threshold.compareTo(MIN_VARIANCE_THRESHOLD) < 0) {
            threshold = MIN_VARIANCE_THRESHOLD;
        }
        return variance.abs().compareTo(threshold) <= 0 ? "ok" : "warn";
    }

    private List<DashboardAlert> buildAlerts(
        BudgetSummary summary,
        BigDecimal actualIncome,
        BigDecimal actualExpense,
        BigDecimal netCashflow,
        BigDecimal variance,
        DashboardTopCategory topExpenseCategory,
        int transactionsCount
    ) {
        List<DashboardAlert> alerts = new ArrayList<>();

        if (transactionsCount == 0) {
            alerts.add(new DashboardAlert(
                "info",
                "Sin movimientos",
                "No se registraron transacciones en el periodo seleccionado."
            ));
        }

        if (actualIncome.signum() == 0) {
            alerts.add(new DashboardAlert(
                "warn",
                "Ingresos ausentes",
                "No hay ingresos registrados en el periodo. Revisa tus fuentes principales."
            ));
        }

        if (netCashflow.signum() < 0) {
            alerts.add(new DashboardAlert(
                "warn",
                "Flujo de caja negativo",
                "Los gastos superan a los ingresos en el rango actual."
            ));
        }

        if (summary.fixedExpense().signum() > 0
            && actualExpense.compareTo(summary.fixedExpense().multiply(EXPENSE_OVERAGE_MULTIPLIER)) > 0) {
            alerts.add(new DashboardAlert(
                "warn",
                "Gasto por encima del plan",
                "Los gastos reales superan el plan fijo en más de un 10%."
            ));
        }

        if (topExpenseCategory != null
            && actualExpense.signum() > 0
            && topExpenseCategory.amount().compareTo(actualExpense.multiply(CONCENTRATION_THRESHOLD)) > 0) {
            alerts.add(new DashboardAlert(
                "info",
                "Concentración de gasto",
                "La categoría " + topExpenseCategory.name() + " concentra más del 50% del gasto."
            ));
        }

        if (variance.signum() < 0) {
            alerts.add(new DashboardAlert(
                "info",
                "Balance bajo lo esperado",
                "El balance real está por debajo del balance planificado."
            ));
        }

        return alerts;
    }
}
