package com.finances.main.web.dto;

import com.finances.main.model.Periodicidad;
import com.finances.main.model.PlannedMovementType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs para movimientos planificados.
 */
public final class PlannedMovementDtos {
    private PlannedMovementDtos() {
        // Utilidad estática.
    }

    /**
     * Request para crear un movimiento planificado.
     */
    public record PlannedMovementRequest(
        String accountName,
        String name,
        BigDecimal amount,
        PlannedMovementType type,
        Periodicidad periodicidad,
        LocalDate startDate,
        boolean active
    ) {
    }

    /**
     * Respuesta para movimientos planificados.
     */
    public record PlannedMovementResponse(
        Long id,
        String accountName,
        String name,
        BigDecimal amount,
        PlannedMovementType type,
        Periodicidad periodicidad,
        LocalDate startDate,
        boolean active
    ) {
    }
}
