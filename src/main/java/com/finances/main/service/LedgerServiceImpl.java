package com.finances.main.service;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
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

    public LedgerServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public BigDecimal calculateBalance(Long accountId) {
        BigDecimal income = transactionRepository.sumByAccountAndCategoryType(accountId, CategoryType.INCOME);
        BigDecimal expense = transactionRepository.sumByAccountAndCategoryType(accountId, CategoryType.EXPENSE);
        return income.subtract(expense);
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
    public List<Transaction> listTransactions(Long accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByAccountAndDateRange(accountId, startDate, endDate);
    }
}
