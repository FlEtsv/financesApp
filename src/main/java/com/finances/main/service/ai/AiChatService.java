package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de IA local que prepara respuestas deterministas.
 */
@Service
public class AiChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatService.class);
    private final AiPromptBuilder promptBuilder;
    private final AiChatRequestNormalizer requestNormalizer;

    public AiChatService(AiPromptBuilder promptBuilder, AiChatRequestNormalizer requestNormalizer) {
        this.promptBuilder = promptBuilder;
        this.requestNormalizer = requestNormalizer;
    }

    /**
     * Genera una respuesta en base al prompt y el contexto disponible.
     */
    public AiChatResponse generateReply(AiChatRequest request) {
        String sessionId = Optional.ofNullable(request.sessionId())
            .filter(id -> !id.isBlank())
            .orElseGet(() -> UUID.randomUUID().toString());

        String prompt = promptBuilder.buildPrompt(request);
        logPrompt(prompt);
        String responseText = buildResponseMessage(request);

        return new AiChatResponse(sessionId, responseText, Instant.now());
    }

    private String buildResponseMessage(AiChatRequest request) {
        StringBuilder builder = new StringBuilder();
        String normalizedMessage = requestNormalizer.normalizeMessage(request.message());
        builder.append("Entendido. He recibido tu consulta: \"")
            .append(normalizedMessage)
            .append("\".");
        appendContextSummary(builder, request);
        return builder.toString();
    }

    /**
     * Agrega un resumen del contexto disponible sin exponer valores nulos.
     */
    private void appendContextSummary(StringBuilder builder, AiChatRequest request) {
        if (request.context() == null) {
            builder.append(" No se recibió contexto adicional para esta sesión.");
            return;
        }

        boolean hasAccountName = request.context().accountName() != null && !request.context().accountName().isBlank();
        boolean hasBalance = request.context().balance() != null;
        boolean hasTotals = request.context().totalsByCategory() != null && !request.context().totalsByCategory().isEmpty();

        if (hasAccountName || hasBalance || hasTotals) {
            builder.append(" Contexto disponible:");
        } else {
            builder.append(" No se recibió contexto adicional para esta sesión.");
            return;
        }

        if (hasAccountName) {
            builder.append(" cuenta ").append(request.context().accountName()).append(",");
        }
        if (hasBalance) {
            builder.append(" balance ").append(request.context().balance()).append(",");
        }
        if (hasTotals) {
            builder.append(" totales por categoría ").append(request.context().totalsByCategory()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1).append(".");
    }

    /**
     * Registra el prompt solo cuando se requiere diagnóstico.
     */
    private void logPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Prompt generado para IA: {}", prompt);
        }
    }
}
