package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.FinancialGoal;
import com.finances.main.repository.FinancialGoalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Servicio de objetivos financieros por cuenta.
 */
@Service
@Transactional
public class FinancialGoalService {
    private final FinancialGoalRepository financialGoalRepository;
    private final AccountService accountService;

    public FinancialGoalService(
        FinancialGoalRepository financialGoalRepository,
        AccountService accountService
    ) {
        this.financialGoalRepository = financialGoalRepository;
        this.accountService = accountService;
    }

    /**
     * Crea un objetivo financiero asociado a una cuenta.
     */
    public FinancialGoal createGoal(
        String accountName,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate targetDate,
        String description
    ) {
        Account account = accountService.getByName(accountName);
        FinancialGoal goal = new FinancialGoal(
            account,
            name,
            targetAmount,
            currentAmount,
            targetDate,
            description
        );
        return financialGoalRepository.save(goal);
    }

    /**
     * Lista objetivos por cuenta.
     */
    @Transactional(readOnly = true)
    public List<FinancialGoal> listGoals(String accountName) {
        return financialGoalRepository.findByAccountName(accountName);
    }

    /**
     * Incrementa el progreso de un objetivo financiero.
     */
    public FinancialGoal addProgress(Long goalId, BigDecimal amount) {
        FinancialGoal goal = financialGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo no encontrado."));
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        goal.setCurrentAmount(goal.getCurrentAmount().add(safeAmount));
        return financialGoalRepository.save(goal);
    }
}
