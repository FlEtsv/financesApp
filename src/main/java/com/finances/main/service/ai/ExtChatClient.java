package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente para conectarse a un endpoint externo de chat IA.
 */
@Component
public class ExtChatClient {
    private static final String DEFAULT_MODEL = "fast";
    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final AiChatService fallbackService;
    private static final Logger log = LoggerFactory.getLogger(ExtChatClient.class);

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
                    .header("X-API-KEY", aiProperties.getExt().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(normalizedRequest)
                    .retrieve()
                    .body(AiChatResponse.class);
            log.debug("Create transaction response ={}", response);

            if (!isValidResponse(response)) {
                return resolveFallbackOrThrow(normalizedRequest, null);
            }
            return response;

        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // Esto te da status + body real del error (oro para debug)
            log.error("AI call failed. Status={} Body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return resolveFallbackOrThrow(normalizedRequest, ex);

        } catch (RestClientException ex) {
            log.error("AI call failed (client error)", ex);
            return resolveFallbackOrThrow(normalizedRequest, ex);
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
            throw new IllegalArgumentException("AiChatRequest no puede ser null");
        }

        String message = request.message();
        log.debug("message = {}",request.message());
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("AiChatRequest.message es obligatorio");
        }

        String model = request.model();
        if (model == null || model.isBlank()) {
            model = DEFAULT_MODEL;
        }

        return new AiChatRequest(request.sessionId(), message.trim(), model, request.context());
    }


    /**
     * Usa el fallback solo cuando está habilitado; de lo contrario, propaga el fallo.
     */
    private AiChatResponse resolveFallbackOrThrow(AiChatRequest request, Exception cause) {
        if (aiProperties.getExt().isFallbackEnabled()) {
            return fallbackService.generateReply(request);
        }
        throw new ExternalAiUnavailableException("No se pudo obtener respuesta del proveedor de IA.", cause);
    }
}
