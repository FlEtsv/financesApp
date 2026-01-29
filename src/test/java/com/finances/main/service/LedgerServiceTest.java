package com.finances.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.finances.main.model.CategoryType;
import com.finances.main.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del servicio de libro mayor.
 */
@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Test
    void calculatesBalanceFromIncomeAndExpense() {
        when(transactionRepository.sumByAccountAndCategoryType(1L, CategoryType.INCOME))
            .thenReturn(new BigDecimal("1500.00"));
        when(transactionRepository.sumByAccountAndCategoryType(1L, CategoryType.EXPENSE))
            .thenReturn(new BigDecimal("500.00"));

        BigDecimal balance = ledgerService.calculateBalance(1L);

        assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void mapsTotalsByCategory() {
        when(transactionRepository.totalsByCategory(1L, CategoryType.EXPENSE))
            .thenReturn(List.of(
                new Object[] {"Renta", new BigDecimal("800.00")},
                new Object[] {"Servicios", new BigDecimal("200.00")}
            ));

        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(1L, CategoryType.EXPENSE);

        assertThat(totals)
            .containsEntry("Renta", new BigDecimal("800.00"))
            .containsEntry("Servicios", new BigDecimal("200.00"));
    }

    @Test
    void returnsTransactionsInRange() {
        when(transactionRepository.findByAccountAndDateRange(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
            .thenReturn(List.of());

        List<?> results = ledgerService.listTransactions(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(results).isEmpty();
    }
}
