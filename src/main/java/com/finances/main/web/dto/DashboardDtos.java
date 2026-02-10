package com.finances.main.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs para los indicadores del dashboard.
 */
public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardTopCategory(String name, BigDecimal amount) {
    }

    public record DashboardAlert(String level, String title, String detail) {
    }

    public record DashboardInsightsResponse(
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal netCashflow,
        BigDecimal savingsRate,
        BigDecimal actualIncome,
        BigDecimal actualExpense,
        BigDecimal expectedBalance,
        BigDecimal actualBalance,
        BigDecimal variance,
        String reconciliationStatus,
        LocalDate lastMovementDate,
        int transactionsCount,
        DashboardTopCategory topExpenseCategory,
        List<DashboardAlert> alerts
    ) {
    }
}
