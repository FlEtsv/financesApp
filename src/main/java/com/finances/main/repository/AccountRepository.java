package com.finances.main.repository;

import com.finances.main.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de cuentas.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
}
