package com.finances.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.finances.main.model.Account;
import com.finances.main.model.CategoryType;
import com.finances.main.repository.AccountRepository;
import com.finances.main.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Test
    void calculatesBalanceFromIncomeAndExpense() {
        Account account = new Account("Cuenta", "USD", new BigDecimal("250.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.sumByAccountAndCategoryType(1L, CategoryType.INGRESO))
            .thenReturn(new BigDecimal("1500.00"));
        when(transactionRepository.sumByAccountAndCategoryType(1L, CategoryType.GASTO))
            .thenReturn(new BigDecimal("500.00"));

        BigDecimal balance = ledgerService.calculateBalance(1L);

        assertThat(balance).isEqualByComparingTo(new BigDecimal("1250.00"));
    }

    @Test
    void mapsTotalsByCategory() {
        when(transactionRepository.totalsByCategory(1L, CategoryType.GASTO))
            .thenReturn(List.of(
                new Object[] {"Renta", new BigDecimal("800.00")},
                new Object[] {"Servicios", new BigDecimal("200.00")}
            ));

        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(1L, CategoryType.GASTO);

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
