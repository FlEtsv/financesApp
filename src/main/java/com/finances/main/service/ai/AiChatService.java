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
        appendFinancialGuidance(builder, request);
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
     * Añade orientación financiera para usuarios individuales y empresas.
     */
    private void appendFinancialGuidance(StringBuilder builder, AiChatRequest request) {
        builder.append(" Considera estos puntos prácticos:");
        builder.append(" si eres usuario, prioriza un fondo de emergencia, evita sobreendeudarte")
            .append(" y revisa tus gastos variables.");
        builder.append(" si gestionas una empresa, monitorea el flujo de caja, la liquidez")
            .append(" y las obligaciones recurrentes (nómina, impuestos, proveedores).");

        if (request.context() == null || request.context().balance() == null) {
            builder.append(" Comparte balance y periodos para afinar recomendaciones.");
            return;
        }

        if (request.context().balance().signum() < 0) {
            builder.append(" El balance negativo sugiere priorizar recortes de gasto")
                .append(" y revisar ingresos planificados.");
        } else {
            builder.append(" El balance positivo permite planificar inversión o ahorro")
                .append(" según tus objetivos.");
        }
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
