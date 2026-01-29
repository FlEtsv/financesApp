package com.finances.main.repository;

import com.finances.main.model.PlannedMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de movimientos planificados.
 */
public interface PlannedMovementRepository extends JpaRepository<PlannedMovement, Long> {
    /**
     * Obtiene movimientos planificados por nombre de cuenta.
     */
    List<PlannedMovement> findByAccountNameIgnoreCaseOrderByStartDateDesc(String accountName);
}
