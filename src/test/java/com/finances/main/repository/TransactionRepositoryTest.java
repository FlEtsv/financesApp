package com.finances.main.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.main.model.Account;
import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Pruebas de integración para consultas de transacciones.
 */
@DataJpaTest
class TransactionRepositoryTest {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findsTransactionsByDateRange() {
        Account account = accountRepository.save(new Account("Cuenta Principal", "USD"));
        Category income = categoryRepository.save(new Category("Salario", CategoryType.INGRESO));

        Transaction january = new Transaction(account, income, new BigDecimal("1000.00"), LocalDate.of(2024, 1, 5), "Pago 1");
        Transaction february = new Transaction(account, income, new BigDecimal("1000.00"), LocalDate.of(2024, 2, 5), "Pago 2");
        transactionRepository.saveAll(List.of(january, february));

        List<Transaction> results = transactionRepository.findByAccountAndDateRange(
            account.getId(),
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).isEqualTo("Pago 1");
    }

    @Test
    void sumsByCategoryType() {
        Account account = accountRepository.save(new Account("Cuenta Principal", "USD"));
        Category income = categoryRepository.save(new Category("Salario", CategoryType.INGRESO));
        Category expense = categoryRepository.save(new Category("Renta", CategoryType.GASTO));

        transactionRepository.save(new Transaction(account, income, new BigDecimal("2000.00"), LocalDate.of(2024, 3, 1), "Pago"));
        transactionRepository.save(new Transaction(account, expense, new BigDecimal("700.00"), LocalDate.of(2024, 3, 2), "Renta"));

        BigDecimal incomeTotal = transactionRepository.sumByAccountAndCategoryType(account.getId(), CategoryType.INGRESO);
        BigDecimal expenseTotal = transactionRepository.sumByAccountAndCategoryType(account.getId(), CategoryType.GASTO);

        assertThat(incomeTotal).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(expenseTotal).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    void groupsTotalsByCategory() {
        Account account = accountRepository.save(new Account("Cuenta Principal", "USD"));
        Category food = categoryRepository.save(new Category("Comida", CategoryType.GASTO));
        Category transport = categoryRepository.save(new Category("Transporte", CategoryType.GASTO));

        transactionRepository.save(new Transaction(account, food, new BigDecimal("50.00"), LocalDate.of(2024, 4, 1), "Almuerzo"));
        transactionRepository.save(new Transaction(account, food, new BigDecimal("30.00"), LocalDate.of(2024, 4, 2), "Cena"));
        transactionRepository.save(new Transaction(account, transport, new BigDecimal("20.00"), LocalDate.of(2024, 4, 3), "Bus"));

        List<Object[]> totals = transactionRepository.totalsByCategory(account.getId(), CategoryType.GASTO);

        assertThat(totals).hasSize(2);
    }
}
