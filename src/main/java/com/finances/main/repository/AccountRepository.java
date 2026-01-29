package com.finances.main.repository;

import com.finances.main.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de cuentas.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Busca cuentas por nombre ignorando mayúsculas/minúsculas.
     */
    Optional<Account> findByNameIgnoreCase(String name);

    /**
     * Valida si existe una cuenta por nombre.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Lista todas las cuentas ordenadas por nombre.
     */
    List<Account> findAllByOrderByNameAsc();
}
