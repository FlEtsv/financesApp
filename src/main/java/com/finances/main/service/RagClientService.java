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
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final RestClient restClient;
    private final AiProperties.Rag rag;

    public RagClientService(RestClient.Builder restClientBuilder, AiProperties aiProperties) {
        this.rag = requireRagConfig(aiProperties);
        this.restClient = restClientBuilder
            .requestFactory(buildRequestFactory(rag))
            .build();
    }

    /**
     * Envía un documento al RAG y retorna la respuesta del servicio externo.
     */
    public RagDocumentResponse sendDocument(RagDocumentRequest request) {
        validateRequest(request);
        String endpoint = buildEndpoint();

        try {
            RagDocumentResponse response = restClient.post()
                .uri(endpoint)
                .header(API_KEY_HEADER, requireApiKey(rag))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RagDocumentResponse.class);

            if (response == null) {

            }
            return response;
        } catch (RestClientResponseException ex) {
            log.error("RAG call failed. Status={} Body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new RagUnavailableException("El proveedor RAG no respondió correctamente.", ex);
        } catch (RestClientException ex) {
            log.error("RAG call failed (client error)", ex);
            throw new RagUnavailableException("No se pudo conectar con el proveedor RAG.", ex);
        }
    }

    private AiProperties.Rag requireRagConfig(AiProperties aiProperties) {
        if (aiProperties == null || aiProperties.getRag() == null) {
            throw new IllegalStateException("AI RAG no está configurado");
        }
        return aiProperties.getRag();
    }

    private void validateRequest(RagDocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RagDocumentRequest no puede ser null");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("RagDocumentRequest.content es obligatorio");
        }
        if (request.title() != null && request.title().isBlank()) {
            throw new IllegalArgumentException("RagDocumentRequest.title no puede ser vacío");
        }
    }

    private String buildEndpoint() {
        String baseUrl = normalizeBaseUrl(rag.getBaseUrl());
        return baseUrl + DOCUMENTS_PATH;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("AI RAG base-url no está configurado");
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(AiProperties.Rag rag) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutSeconds = rag.getTimeoutSeconds();
        if (timeoutSeconds <= 0) {
            throw new IllegalStateException("AI RAG timeoutSeconds debe ser mayor a 0");
        }
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }

    private String requireApiKey(AiProperties.Rag rag) {
        if (rag.getApiKey() == null || rag.getApiKey().isBlank()) {
            throw new IllegalStateException("AI RAG api-key no está configurado");
        }
        return rag.getApiKey();
    }
}
