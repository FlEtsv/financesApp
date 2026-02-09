package com.finances.main.service.ai;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final RestClient restClient;
    private final AiProperties.Ext ext;
    private final AiChatService fallbackService;
    private static final Logger log = LoggerFactory.getLogger(ExtChatClient.class);

    public ExtChatClient(
        AiProperties aiProperties,
        AiChatService fallbackService,
        RestClient.Builder restClientBuilder
    ) {
        this.ext = requireExtConfig(aiProperties);
        this.fallbackService = fallbackService;
        this.restClient = restClientBuilder
            .requestFactory(buildRequestFactory(ext))
            .build();
    }

    /**
     * Envía la solicitud de chat al endpoint configurado.
     */
    public AiChatResponse sendChat(AiChatRequest request) {
        AiChatRequest normalizedRequest = normalizeRequest(request);

        try {

            AiChatResponse response = restClient
                    .post()
                    .uri(normalizeBaseUrl(ext.getBaseUrl()))
                    .header(API_KEY_HEADER, requireApiKey(ext))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(normalizedRequest)
                    .retrieve()
                    .body(AiChatResponse.class);
            log.debug("Create transaction response ={}", response);

            if (!isValidResponse(response)) {
                return resolveFallbackOrThrow(normalizedRequest, null);
            }
            return response;

        } catch (RestClientResponseException ex) {
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
        if (ext.isFallbackEnabled()) {
            return fallbackService.generateReply(request);
        }
        throw new ExternalAiUnavailableException("No se pudo obtener respuesta del proveedor de IA.", cause);
    }

    private AiProperties.Ext requireExtConfig(AiProperties aiProperties) {
        if (aiProperties == null || aiProperties.getExt() == null) {
            throw new IllegalStateException("AI EXT no está configurado");
        }
        return aiProperties.getExt();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("AI EXT base-url no está configurado");
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(AiProperties.Ext ext) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutSeconds = ext.getTimeoutSeconds();
        if (timeoutSeconds <= 0) {
            throw new IllegalStateException("AI EXT timeoutSeconds debe ser mayor a 0");
        }
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }

    private String requireApiKey(AiProperties.Ext ext) {
        if (ext.getApiKey() == null || ext.getApiKey().isBlank()) {
            throw new IllegalStateException("AI EXT api-key no está configurado");
        }
        return ext.getApiKey();
    }
}
