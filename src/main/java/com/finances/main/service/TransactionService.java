package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.repository.TransactionRepository;
import com.finances.main.service.ai.AiRecommendationService;
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
    private final AiRecommendationService aiRecommendationService;

    public TransactionService(
        TransactionRepository transactionRepository,
        AccountService accountService,
        CategoryService categoryService,
        AiRecommendationService aiRecommendationService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.aiRecommendationService = aiRecommendationService;
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
        Transaction saved = transactionRepository.save(transaction);
        aiRecommendationService.onMovementRecorded(accountName);
        return saved;
    }
    /**
     * Elimina un movimiento por su identificador.
     */
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


