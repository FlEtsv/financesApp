package com.finances.main.service;

import com.finances.main.model.Account;
import com.finances.main.model.PlannedMovement;
import com.finances.main.model.PlannedMovementType;
import com.finances.main.model.Periodicidad;
import com.finances.main.repository.PlannedMovementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que gestiona gastos e ingresos planificados.
 */
@Service
@Transactional
public class PlannedMovementService {
    private final PlannedMovementRepository plannedMovementRepository;
    private final AccountService accountService;

    public PlannedMovementService(
        PlannedMovementRepository plannedMovementRepository,
        AccountService accountService
    ) {
        this.plannedMovementRepository = plannedMovementRepository;
        this.accountService = accountService;
    }

    /**
     * Crea un movimiento planificado asociado a una cuenta.
     */
    public PlannedMovement createPlannedMovement(
        String accountName,
        String name,
        BigDecimal amount,
        PlannedMovementType type,
        Periodicidad periodicidad,
        LocalDate startDate,
        boolean active
    ) {
        Account account = accountService.getByName(accountName);
        PlannedMovement movement = new PlannedMovement(
            name,
            amount,
            type,
            periodicidad,
            startDate,
            active,
            account
        );
        return plannedMovementRepository.save(movement);
    }

    /**
     * Lista movimientos planificados de una cuenta.
     */
    @Transactional(readOnly = true)
    public List<PlannedMovement> listByAccountName(String accountName) {
        return plannedMovementRepository.findByAccountNameIgnoreCaseOrderByStartDateDesc(accountName);
    }
}
