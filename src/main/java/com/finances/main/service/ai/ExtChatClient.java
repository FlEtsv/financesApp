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
    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final AiChatService fallbackService;

    public ExtChatClient(AiProperties aiProperties, AiChatService fallbackService, RestClient.Builder restClientBuilder ) {
        this.aiProperties = aiProperties;
        this.fallbackService = fallbackService;
        this.restClient = restClientBuilder.build();
    }

    /**
     * Envía la solicitud de chat al endpoint configurado.
     */
    public AiChatResponse sendChat(AiChatRequest request) {
        try {
            AiChatResponse response = restClient
                .post()
                .uri(aiProperties.getExt().getBaseUrl())
                .header("X-API-KEY", aiProperties.getExt().getApiKey())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .retrieve()
                .body(AiChatResponse.class);
            if (!isValidResponse(response)) {
                return fallbackService.generateReply(request);
            }
            return response;
        } catch (RestClientException ex) {
            return fallbackService.generateReply(request);
        }
    }

    /**
     * Valida que la respuesta externa tenga un payload utilizable.
     */
    private boolean isValidResponse(AiChatResponse response) {
        return response != null && response.reply() != null && !response.reply().isBlank();
    }
}
