package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio dedicado a operaciones de cuentas.
 */
@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Crea una nueva cuenta validando nombre único.
     */
    public Account createAccount(String name, String currency, BigDecimal initialBalance) {
        if (accountRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese nombre.");
        }
        Account account = new Account(name, currency, initialBalance);
        return accountRepository.save(account);
    }

    /**
     * Actualiza el saldo inicial de una cuenta por nombre.
     */
    public Account updateInitialBalance(String accountName, BigDecimal initialBalance) {
        Account account = getByName(accountName);
        account.setInitialBalance(initialBalance);
        return accountRepository.save(account);
    }

    /**
     * Devuelve todas las cuentas ordenadas.
     */
    @Transactional(readOnly = true)
    public List<Account> listAccounts() {
        return accountRepository.findAllByOrderByNameAsc();
    }

    /**
     * Obtiene una cuenta por nombre o falla si no existe.
     */
    @Transactional(readOnly = true)
    public Account getByName(String accountName) {
        return accountRepository.findByNameIgnoreCase(accountName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada."));
    }
}
