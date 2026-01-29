package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.repository.AccountRepository;
import com.finances.main.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de libro mayor.
 */
@Service
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public LedgerServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public BigDecimal calculateBalance(Long accountId) {
        BigDecimal income = transactionRepository.sumByAccountAndCategoryType(accountId, CategoryType.INGRESO);
        BigDecimal expense = transactionRepository.sumByAccountAndCategoryType(accountId, CategoryType.GASTO);
        return resolveInitialBalanceById(accountId).add(income.subtract(expense));
    }

    @Override
    public BigDecimal calculateBalance(String accountName) {
        BigDecimal income = transactionRepository.sumByAccountNameAndCategoryType(accountName, CategoryType.INGRESO);
        BigDecimal expense = transactionRepository.sumByAccountNameAndCategoryType(accountName, CategoryType.GASTO);
        return resolveInitialBalanceByName(accountName).add(income.subtract(expense));
    }

    @Override
    public Map<String, BigDecimal> totalsByCategory(Long accountId, CategoryType type) {
        List<Object[]> rows = transactionRepository.totalsByCategory(accountId, type);
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totals.put(category, amount);
        }
        return totals;
    }

    @Override
    public Map<String, BigDecimal> totalsByCategory(String accountName, CategoryType type) {
        List<Object[]> rows = transactionRepository.totalsByCategoryAndAccountName(accountName, type);
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totals.put(category, amount);
        }
        return totals;
    }

    @Override
    public List<Transaction> listTransactions(Long accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByAccountAndDateRange(accountId, startDate, endDate);
    }

    @Override
    public List<Transaction> listTransactions(String accountName, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByAccountNameAndDateRange(accountName, startDate, endDate);
    }

    private BigDecimal resolveInitialBalanceById(Long accountId) {
        return accountRepository.findById(accountId)
            .map(Account::getInitialBalance)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal resolveInitialBalanceByName(String accountName) {
        return accountRepository.findByNameIgnoreCase(accountName)
            .map(Account::getInitialBalance)
            .orElse(BigDecimal.ZERO);
    }
}
