package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente para conectarse a un endpoint externo de chat IA.
 */
@Component
public class ExtChatClient {
    private static final String DEFAULT_MODEL = "fast";
    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final AiChatService fallbackService;

    public ExtChatClient(
        AiProperties aiProperties,
        AiChatService fallbackService,
        RestClient.Builder restClientBuilder
    ) {
        this.aiProperties = aiProperties;
        this.fallbackService = fallbackService;
        this.restClient = restClientBuilder.build();
    }

    /**
     * Envía la solicitud de chat al endpoint configurado.
     */
    public AiChatResponse sendChat(AiChatRequest request) {
        AiChatRequest normalizedRequest = normalizeRequest(request);
        try {
            AiChatResponse response = restClient
                .post()
                .uri(aiProperties.getExt().getBaseUrl())
                .header("ex-api-key", aiProperties.getExt().getApiKey())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(normalizedRequest)
                .retrieve()
                .body(AiChatResponse.class);
            if (!isValidResponse(response)) {
                return fallbackService.generateReply(normalizedRequest);
            }
            return response;
        } catch (RestClientException ex) {
            return fallbackService.generateReply(normalizedRequest);
        }
    }

    /**
     * Valida que la respuesta externa tenga un payload utilizable.
     */
    private boolean isValidResponse(AiChatResponse response) {
        return response != null && response.reply() != null && !response.reply().isBlank();
    }

    /**
     * Asegura el modelo por defecto requerido por el servicio externo.
     */
    private AiChatRequest normalizeRequest(AiChatRequest request) {
        if (request == null) {
            return new AiChatRequest(null, null, DEFAULT_MODEL, null);
        }
        String model = request.model();
        if (model == null || model.isBlank()) {
            model = DEFAULT_MODEL;
        }
        return new AiChatRequest(request.sessionId(), request.message(), model, request.context());
    }
}
