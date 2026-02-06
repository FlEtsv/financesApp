package com.finances.main.service.ai;

import com.finances.main.model.CategoryType;
import com.finances.main.service.AccountService;
import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de recopilar contexto y solicitar recomendaciones a la IA.
 */
@Service
public class AiRecommendationService {
    private static final String DEFAULT_RECOMMENDATION_PROMPT =
        "Analiza el contexto financiero y sugiere movimientos o ajustes posibles. "
            + "Entrega recomendaciones concretas y una opinión general del estado.";

    private final AiContextService aiContextService;
    private final ExtChatClient extChatClient;
    private final AccountService accountService;
    private final AiProperties aiProperties;
    private final Clock clock;
    private final Map<String, AiRecommendationSnapshot> latestRecommendations = new ConcurrentHashMap<>();

    public AiRecommendationService(
        AiContextService aiContextService,
        ExtChatClient extChatClient,
        AccountService accountService,
        AiProperties aiProperties,
        Clock clock
    ) {
        this.aiContextService = aiContextService;
        this.extChatClient = extChatClient;
        this.accountService = accountService;
        this.aiProperties = aiProperties;
        this.clock = clock;
    }

    /**
     * Genera recomendaciones cuando ocurre un nuevo movimiento real.
     */
    public void onMovementRecorded(String accountName) {
        if (!aiProperties.getRecommendations().isEnabled()) {
            return;
        }
        generateAndStoreRecommendation(accountName);
    }

    /**
     * Devuelve la última recomendación disponible para la cuenta solicitada.
     */
    public Optional<AiRecommendationSnapshot> getLatestRecommendation(String accountName) {
        if (accountName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestRecommendations.get(accountName));
    }

    /**
     * Ejecuta un ciclo automático para actualizar recomendaciones de todas las cuentas.
     */
    @Scheduled(fixedDelayString = "${ai.recommendations.interval-ms:1800000}")
    public void refreshRecommendations() {
        if (!aiProperties.getRecommendations().isEnabled()) {
            return;
        }
        accountService.listAccounts().forEach(account -> generateAndStoreRecommendation(account.getName()));
    }

    private void generateAndStoreRecommendation(String accountName) {
        AiContextResponse context = buildContext(accountName);
        AiChatRequest request = new AiChatRequest(null, resolvePrompt(), context);
        AiChatResponse response = extChatClient.sendChat(request);
        latestRecommendations.put(accountName, new AiRecommendationSnapshot(accountName, context, response.reply(), response.respondedAt()));
    }

    private AiContextResponse buildContext(String accountName) {
        LocalDate endDate = LocalDate.now(clock);
        int lookbackDays = Math.max(1, aiProperties.getRecommendations().getLookbackDays());
        LocalDate startDate = endDate.minusDays(lookbackDays);
        CategoryType categoryType = Optional.ofNullable(aiProperties.getRecommendations().getCategoryType())
            .orElse(CategoryType.GASTO);
        return aiContextService.buildContext(accountName, startDate, endDate, categoryType);
    }

    private String resolvePrompt() {
        String prompt = aiProperties.getRecommendations().getPrompt();
        if (prompt == null || prompt.isBlank()) {
            return DEFAULT_RECOMMENDATION_PROMPT;
        }
        return prompt;
    }

    /**
     * Snapshot inmutable de la recomendación más reciente.
     */
    public record AiRecommendationSnapshot(
        String accountName,
        AiContextResponse context,
        String recommendation,
        Instant generatedAt
    ) {
    }
}
