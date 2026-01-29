package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para registrar movimientos reales (ingresos y gastos).
 */
@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public TransactionService(
        TransactionRepository transactionRepository,
        AccountService accountService,
        CategoryService categoryService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    /**
     * Registra un movimiento financiero para una cuenta.
     */
    public Transaction registerTransaction(
        String accountName,
        String categoryName,
        CategoryType categoryType,
        BigDecimal amount,
        LocalDate transactionDate,
        String description
    ) {
        Account account = accountService.getByName(accountName);
        Category category = categoryService.getOrCreate(categoryName, categoryType);
        Transaction transaction = new Transaction(account, category, amount, transactionDate, description);
        return transactionRepository.save(transaction);
    }


        @Transactional
        public void deleteTransactionById(Long transactionId) {
            if (transactionId == null) {
                throw new IllegalArgumentException("transactionId no puede ser null");
            }

            if (!transactionRepository.existsById(transactionId)) {
                throw new NoSuchElementException("No existe la transacción con id=" + transactionId);
            }

            transactionRepository.deleteById(transactionId);
        }
    }




