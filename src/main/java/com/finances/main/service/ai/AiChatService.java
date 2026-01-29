package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Servicio de IA local que prepara respuestas deterministas.
 */
@Service
public class AiChatService {
    private final AiPromptBuilder promptBuilder;

    public AiChatService(AiPromptBuilder promptBuilder) {
        this.promptBuilder = promptBuilder;
    }

    /**
     * Genera una respuesta en base al prompt y el contexto disponible.
     */
    public AiChatResponse generateReply(AiChatRequest request) {
        String sessionId = Optional.ofNullable(request.sessionId())
            .filter(id -> !id.isBlank())
            .orElseGet(() -> UUID.randomUUID().toString());

        String prompt = promptBuilder.buildPrompt(request);
        String responseText = buildResponseMessage(request, prompt);

        return new AiChatResponse(sessionId, responseText, Instant.now());
    }

    private String buildResponseMessage(AiChatRequest request, String prompt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Entendido. He recibido tu consulta y estoy analizando la información disponible.");
        builder.append(" ");
        if (request.context() != null) {
            builder.append("Contexto: cuenta ")
                .append(request.context().accountName())
                .append(", balance ")
                .append(request.context().balance())
                .append(", totales por categoría ")
                .append(request.context().totalsByCategory())
                .append(".");
        } else {
            builder.append("No se recibió contexto adicional para esta sesión.");
        }
        builder.append(" Prompt aplicado: ").append(prompt);
        return builder.toString();
    }
}
