package com.finances.main.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Periodicidad;
import com.finances.main.model.PlannedMovementType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración para el flujo financiero completo.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanceFlowIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PlannedMovementService plannedMovementService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private FinancialGoalService financialGoalService;

    @Test
    void updatesInitialBalanceForAccount() {
        var account = accountService.createAccount("Cuenta Test", "EUR", new BigDecimal("100.00"));

        var updated = accountService.updateInitialBalance(account.getName(), new BigDecimal("250.00"));

        assertThat(updated.getInitialBalance()).isEqualByComparingTo("250.00");
    }

    @Test
    void calculatesBudgetSummaryWithFixedAndActualMovements() {
        accountService.createAccount("Cuenta Presupuesto", "EUR", new BigDecimal("1000.00"));

        plannedMovementService.createPlannedMovement(
            "Cuenta Presupuesto",
            "Nómina",
            new BigDecimal("2000.00"),
            PlannedMovementType.INGRESO_FIJO_NOMINA,
            Periodicidad.MENSUAL,
            LocalDate.now().minusMonths(1),
            true
        );

        plannedMovementService.createPlannedMovement(
            "Cuenta Presupuesto",
            "Alquiler",
            new BigDecimal("800.00"),
            PlannedMovementType.GASTO_FIJO,
            Periodicidad.MENSUAL,
            LocalDate.now().minusMonths(1),
            true
        );

        transactionService.registerTransaction(
            "Cuenta Presupuesto",
            "Pago cliente",
            CategoryType.INGRESO,
            new BigDecimal("500.00"),
            LocalDate.now(),
            "Ingreso puntual"
        );

        transactionService.registerTransaction(
            "Cuenta Presupuesto",
            "Comida",
            CategoryType.GASTO,
            new BigDecimal("100.00"),
            LocalDate.now(),
            "Gasto variable"
        );

        var summary = budgetService.buildSummary(
            "Cuenta Presupuesto",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(1)
        );

        assertThat(summary.expectedBalance()).isEqualByComparingTo("2200.00");
        assertThat(summary.actualBalance()).isEqualByComparingTo("1400.00");
    }

    @Test
    void ignoresPlannedMovementsOutsideSummaryRange() {
        accountService.createAccount("Cuenta Futuro", "EUR", new BigDecimal("500.00"));

        plannedMovementService.createPlannedMovement(
            "Cuenta Futuro",
            "Ingreso futuro",
            new BigDecimal("1200.00"),
            PlannedMovementType.INGRESO_FIJO_NOMINA,
            Periodicidad.MENSUAL,
            LocalDate.now().plusMonths(1),
            true
        );

        var summary = budgetService.buildSummary(
            "Cuenta Futuro",
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );

        assertThat(summary.fixedIncome()).isEqualByComparingTo("0.00");
        assertThat(summary.expectedBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void registersProgressOnFinancialGoal() {
        accountService.createAccount("Cuenta Objetivos", "EUR", new BigDecimal("0.00"));

        var goal = financialGoalService.createGoal(
            "Cuenta Objetivos",
            "Viaje",
            new BigDecimal("1500.00"),
            new BigDecimal("200.00"),
            LocalDate.now().plusMonths(6),
            "Ahorrar para viaje"
        );

        var updated = financialGoalService.addProgress(goal.getId(), new BigDecimal("100.00"));

        assertThat(updated.getCurrentAmount()).isEqualByComparingTo("300.00");
    }
}
