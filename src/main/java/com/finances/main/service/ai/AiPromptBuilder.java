package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Construye el prompt base para la IA con el contexto disponible.
 */
@Component
public class AiPromptBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiPromptBuilder.class);
    private final AiProperties aiProperties;
    private final AiChatRequestNormalizer requestNormalizer;

    public AiPromptBuilder(AiProperties aiProperties, AiChatRequestNormalizer requestNormalizer) {
        this.aiProperties = aiProperties;
        this.requestNormalizer = requestNormalizer;
    }

    /**
     * Devuelve un prompt enriquecido para la conversación actual.
     */
    public String buildPrompt(AiChatRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        String systemPrompt = aiProperties.getSystemPrompt();
        addLineIfPresent(joiner, systemPrompt);
        String userMessage = requestNormalizer.normalizeMessage(request.message());
        joiner.add("Mensaje del usuario: " + userMessage);
        if (request.context() != null) {
            addLineIfPresent(joiner, formatContextLine("Cuenta", request.context().accountName()));
            addLineIfPresent(joiner, formatContextLine("Balance", request.context().balance()));
            addLineIfPresent(joiner, formatContextLine("Totales por categoría", request.context().totalsByCategory()));
            addLineIfPresent(joiner, formatContextLine("Movimientos recientes", request.context().recentTransactions()));
            addLineIfPresent(joiner, formatContextLine("Movimientos planificados", request.context().plannedMovements()));
        }
        String prompt = joiner.toString();
        logPrompt(prompt);
        return prompt;
    }

    /**
     * Registra el prompt solo para depuración, sin exponerlo al usuario final.
     */
    private void logPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Prompt construido para IA: {}", prompt);
        }
    }

    /**
     * Agrega líneas solo cuando el contenido está disponible.
     */
    private void addLineIfPresent(StringJoiner joiner, String line) {
        if (line != null && !line.isBlank()) {
            joiner.add(line);
        }
    }

    /**
     * Formatea el valor del contexto para evitar nulos.
     */
    private String formatContextLine(String label, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Map<?, ?> map && map.isEmpty()) {
            return null;
        }
        if (value instanceof java.util.Collection<?> collection && collection.isEmpty()) {
            return null;
        }
        if (value instanceof String text && text.isBlank()) {
            return null;
        }
        return label + ": " + value;
    }
}
