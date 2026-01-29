package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.CategoryType;
import com.finances.main.model.PlannedMovement;
import com.finances.main.model.PlannedMovementType;
import com.finances.main.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para construir presupuestos y resúmenes mensuales.
 */
@Service
@Transactional(readOnly = true)
public class BudgetService {
    private final AccountService accountService;
    private final PlannedMovementService plannedMovementService;
    private final TransactionRepository transactionRepository;

    public BudgetService(
        AccountService accountService,
        PlannedMovementService plannedMovementService,
        TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.plannedMovementService = plannedMovementService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Construye el resumen presupuestario de una cuenta para un rango de fechas.
     */
    public BudgetSummary buildSummary(String accountName, LocalDate startDate, LocalDate endDate) {
        Account account = accountService.getByName(accountName);
        BigDecimal initialBalance = account.getInitialBalance();

        BigDecimal fixedIncome = sumPlannedMovements(accountName, EnumSet.of(PlannedMovementType.INGRESO_FIJO_NOMINA));
        BigDecimal fixedExpense = sumPlannedMovements(accountName, EnumSet.of(PlannedMovementType.GASTO_FIJO));

        BigDecimal actualIncome = transactionRepository.sumByAccountNameAndCategoryTypeWithinDateRange(
            accountName,
            CategoryType.INGRESO,
            startDate,
            endDate
        );
        BigDecimal actualExpense = transactionRepository.sumByAccountNameAndCategoryTypeWithinDateRange(
            accountName,
            CategoryType.GASTO,
            startDate,
            endDate
        );

        BigDecimal expectedBalance = initialBalance.add(fixedIncome.subtract(fixedExpense));
        BigDecimal actualBalance = initialBalance.add(actualIncome.subtract(actualExpense));

        return new BudgetSummary(
            accountName,
            startDate,
            endDate,
            initialBalance,
            fixedIncome,
            fixedExpense,
            actualIncome,
            actualExpense,
            expectedBalance,
            actualBalance
        );
    }

    /**
     * Genera un resumen mensual de ingresos y gastos reales para un año.
     */
    public List<MonthlySummary> buildMonthlySummary(String accountName, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        var transactions = transactionRepository.findByAccountNameAndDateRange(accountName, start, end);
        Map<YearMonth, MonthlyAccumulator> byMonth = new TreeMap<>();
        transactions.forEach(transaction -> {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            MonthlyAccumulator accumulator = byMonth.computeIfAbsent(month, value -> new MonthlyAccumulator());
            if (transaction.getCategory().getType() == CategoryType.INGRESO) {
                accumulator.income = accumulator.income.add(transaction.getAmount());
            } else {
                accumulator.expense = accumulator.expense.add(transaction.getAmount());
            }
        });

        return byMonth.entrySet().stream()
            .map(entry -> new MonthlySummary(
                entry.getKey().getYear(),
                entry.getKey().getMonthValue(),
                entry.getValue().income,
                entry.getValue().expense,
                entry.getValue().income.subtract(entry.getValue().expense)
            ))
            .toList();
    }

    private BigDecimal sumPlannedMovements(String accountName, EnumSet<PlannedMovementType> types) {
        List<PlannedMovement> movements = plannedMovementService.listByAccountNameAndTypes(
            accountName,
            List.copyOf(types)
        );
        return movements.stream()
            .filter(PlannedMovement::isActive)
            .map(PlannedMovement::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * DTO interno para acumular montos.
     */
    private static class MonthlyAccumulator {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
    }

    /**
     * DTO interno de resumen presupuestario.
     */
    public record BudgetSummary(
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
     * DTO interno de resumen mensual.
     */
    public record MonthlySummary(
        int year,
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
    ) {
    }
}
