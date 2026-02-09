package com.finances.main.service;

import com.finances.main.service.ai.AiProperties;
import com.finances.main.web.dto.RagDtos.RagDocumentRequest;
import com.finances.main.web.dto.RagDtos.RagDocumentResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente para enviar documentos al servicio RAG externo.
 */
@Component
public class RagClientService {
    private static final Logger log = LoggerFactory.getLogger(RagClientService.class);
    private static final String DOCUMENTS_PATH = "/api/ext/rag/documents";
    private final RestClient restClient;
    private final AiProperties aiProperties;

    public RagClientService(RestClient.Builder restClientBuilder, AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.restClient = restClientBuilder
            .requestFactory(() -> buildRequestFactory(aiProperties))
            .build();
    }

    /**
     * Envía un documento al RAG y retorna la respuesta del servicio externo.
     */
    public RagDocumentResponse sendDocument(RagDocumentRequest request) {
        validateRequest(request);
        String endpoint = buildEndpoint();

        try {
            return restClient.post()
                .uri(endpoint)
                .header("X-API-KEY", aiProperties.getRag().getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RagDocumentResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("RAG call failed. Status={} Body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new RagUnavailableException("El proveedor RAG no respondió correctamente.", ex);
        } catch (RestClientException ex) {
            log.error("RAG call failed (client error)", ex);
            throw new RagUnavailableException("No se pudo conectar con el proveedor RAG.", ex);
        }
    }

    private void validateRequest(RagDocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RagDocumentRequest no puede ser null");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("RagDocumentRequest.content es obligatorio");
        }
    }

    private String buildEndpoint() {
        String baseUrl = normalizeBaseUrl(aiProperties.getRag().getBaseUrl());
        return baseUrl + DOCUMENTS_PATH;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("AI RAG base-url no está configurado");
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(AiProperties aiProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutSeconds = aiProperties.getRag().getTimeoutSeconds();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }
}
