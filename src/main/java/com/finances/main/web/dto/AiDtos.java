package com.finances.main.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finances.main.model.CategoryType;
import com.finances.main.model.PlannedMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs para las conversaciones con IA.
 */
public final class AiDtos {
    private AiDtos() {
        // Utilidad estática.
    }

    /**
     * Request para obtener contexto de IA.
     */
    public record AiContextRequest(
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        CategoryType categoryType
    ) {
    }

    /**
     * Resumen de movimiento planificado para IA.
     */
    public record PlannedMovementSummary(
        String name,
        PlannedMovementType type,
        BigDecimal amount,
        LocalDate startDate,
        boolean active
    ) {
    }

    /**
     * Respuesta de contexto de IA.
     */
    public record AiContextResponse(
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        CategoryType categoryType,
        BigDecimal balance,
        Map<String, BigDecimal> totalsByCategory,
        List<LedgerResponses.TransactionSummary> recentTransactions,
        List<PlannedMovementSummary> plannedMovements
    ) {
    }

    /**
     * Request de chat IA.
     */
    public record AiChatRequest(
        String sessionId,
        @JsonProperty("mensaje") @JsonAlias("message") String message,
        String model,
        AiContextResponse context
    ) {
    }

    /**
     * Respuesta del chat IA.
     */
    public record AiChatResponse(String sessionId, String reply, Instant respondedAt) {
    }

    /**
     * Respuesta con recomendaciones periódicas de la IA.
     */
    public record AiRecommendationResponse(
        String accountName,
        String recommendation,
        Instant generatedAt,
        AiContextResponse context
    ) {
    }
}
