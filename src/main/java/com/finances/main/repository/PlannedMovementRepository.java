package com.finances.main.repository;

import com.finances.main.model.PlannedMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de movimientos planificados.
 */
public interface PlannedMovementRepository extends JpaRepository<PlannedMovement, Long> {
    /**
     * Obtiene movimientos planificados por nombre de cuenta.
     */
    @Query("""
        select movement
        from PlannedMovement movement
        join fetch movement.account account
        where lower(account.name) = lower(:accountName)
        order by movement.startDate desc
        """)
    List<PlannedMovement> findByAccountNameIgnoreCaseOrderByStartDateDesc(
        @Param("accountName") String accountName
    );

    /**
     * Obtiene movimientos planificados filtrando por tipo.
     */
    @Query("""
        select movement
        from PlannedMovement movement
        join fetch movement.account account
        where lower(account.name) = lower(:accountName)
          and movement.type in :types
        order by movement.startDate desc
        """)
    List<PlannedMovement> findByAccountNameAndTypes(
        @Param("accountName") String accountName,
        @Param("types") List<com.finances.main.model.PlannedMovementType> types
    );
}
