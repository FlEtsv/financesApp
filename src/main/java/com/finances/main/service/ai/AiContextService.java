package com.finances.main.service.ai;

import com.finances.main.model.CategoryType;
import com.finances.main.model.PlannedMovement;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import com.finances.main.service.PlannedMovementService;
import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import com.finances.main.web.dto.AiDtos.PlannedMovementSummary;
import com.finances.main.web.dto.LedgerResponses.TransactionSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable de construir y enriquecer el contexto de IA.
 */
@Service
public class AiContextService {
    private final LedgerService ledgerService;
    private final PlannedMovementService plannedMovementService;

    public AiContextService(LedgerService ledgerService, PlannedMovementService plannedMovementService) {
        this.ledgerService = ledgerService;
        this.plannedMovementService = plannedMovementService;
    }

    /**
     * Construye el contexto completo desde el backend para una consulta de IA.
     */
    public AiContextResponse buildContext(
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        CategoryType categoryType
    ) {
        BigDecimal balance = ledgerService.calculateBalance(accountName);
        Map<String, BigDecimal> totals = categoryType == null
            ? Collections.emptyMap()
            : ledgerService.totalsByCategory(accountName, categoryType);
        List<TransactionSummary> transactions = startDate == null || endDate == null
            ? List.of()
            : ledgerService.listTransactions(accountName, startDate, endDate)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        List<PlannedMovementSummary> plannedMovements = plannedMovementService.listByAccountName(accountName)
            .stream()
            .map(this::toPlannedSummary)
            .collect(Collectors.toList());

        return new AiContextResponse(
            accountName,
            startDate,
            endDate,
            categoryType,
            balance,
            totals,
            transactions,
            plannedMovements
        );
    }

    /**
     * Enriquecer el contexto enviado por el cliente cuando faltan datos.
     */
    public AiContextResponse enrichContext(AiContextResponse context) {
        if (context == null) {
            return null;
        }

        String accountName = context.accountName();
        if (accountName == null || accountName.isBlank()) {
            return context;
        }

        LocalDate startDate = context.startDate();
        LocalDate endDate = context.endDate();
        CategoryType categoryType = context.categoryType();

        boolean needsRefresh = context.balance() == null || context.totalsByCategory() == null;

        BigDecimal balance = context.balance();
        if (balance == null) {
            balance = ledgerService.calculateBalance(accountName);
        }

        Map<String, BigDecimal> totals = context.totalsByCategory();
        if (totals == null && categoryType != null) {
            totals = ledgerService.totalsByCategory(accountName, categoryType);
        }

        List<TransactionSummary> recentTransactions = resolveTransactions(
            context.recentTransactions(),
            accountName,
            startDate,
            endDate,
            needsRefresh
        );

        List<PlannedMovementSummary> plannedMovements = resolvePlannedMovements(
            context.plannedMovements(),
            accountName,
            needsRefresh
        );

        return new AiContextResponse(
            accountName,
            startDate,
            endDate,
            categoryType,
            balance,
            totals,
            recentTransactions,
            plannedMovements
        );
    }

    /**
     * Enriquecer el request de chat para asegurar contexto actualizado.
     */
    public AiChatRequest enrichChatRequest(AiChatRequest request) {
        if (request == null) {
            return null;
        }
        AiContextResponse context = enrichContext(request.context());
        if (Objects.equals(context, request.context())) {
            return request;
        }
        return new AiChatRequest(request.sessionId(), request.message(), context);
    }

    private List<TransactionSummary> resolveTransactions(
        List<TransactionSummary> current,
        String accountName,
        LocalDate startDate,
        LocalDate endDate,
        boolean needsRefresh
    ) {
        if (current == null || (needsRefresh && current.isEmpty())) {
            if (startDate == null || endDate == null) {
                return List.of();
            }
            return ledgerService.listTransactions(accountName, startDate, endDate)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        }
        return current;
    }

    private List<PlannedMovementSummary> resolvePlannedMovements(
        List<PlannedMovementSummary> current,
        String accountName,
        boolean needsRefresh
    ) {
        if (current == null || (needsRefresh && current.isEmpty())) {
            return plannedMovementService.listByAccountName(accountName)
                .stream()
                .map(this::toPlannedSummary)
                .collect(Collectors.toList());
        }
        return current;
    }

    private TransactionSummary toSummary(Transaction transaction) {
        return new TransactionSummary(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getDescription(),
            transaction.getCategory().getName(),
            transaction.getCategory().getType()
        );
    }

    private PlannedMovementSummary toPlannedSummary(PlannedMovement movement) {
        return new PlannedMovementSummary(
            movement.getName(),
            movement.getType(),
            movement.getAmount(),
            movement.getStartDate(),
            movement.isActive()
        );
    }
}
