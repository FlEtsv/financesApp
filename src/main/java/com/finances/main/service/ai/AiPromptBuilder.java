package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * Construye el prompt base para la IA con el contexto disponible.
 */
@Component
public class AiPromptBuilder {
    private final AiProperties aiProperties;

    public AiPromptBuilder(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * Devuelve un prompt enriquecido para la conversación actual.
     */
    public String buildPrompt(AiChatRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        String systemPrompt = aiProperties.getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            joiner.add(systemPrompt);
        }
        joiner.add("Mensaje del usuario: " + request.message());
        if (request.context() != null) {
            joiner.add("Cuenta: " + request.context().accountName());
            joiner.add("Balance: " + request.context().balance());
            joiner.add("Totales por categoría: " + request.context().totalsByCategory());
            joiner.add("Movimientos recientes: " + request.context().recentTransactions());
            joiner.add("Movimientos planificados: " + request.context().plannedMovements());
        }
        return joiner.toString();
    }
}
