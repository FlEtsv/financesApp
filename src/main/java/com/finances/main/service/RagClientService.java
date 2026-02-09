package com.finances.main.service;

import com.finances.main.service.ai.AiProperties;
import com.finances.main.web.dto.RagDtos.RagDocumentRequest;
import com.finances.main.web.dto.RagDtos.RagDocumentResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RagClient {
    private final RestClient restClient;
    private final AiProperties aiProperties;

    public RagClient(RestClient.Builder restClientBuilder, AiProperties aiProperties) {
        this.restClient = restClientBuilder.build();
        this.aiProperties = aiProperties;
    }

    public RagDocumentResponse sendDocument(RagDocumentRequest request) {
        return restClient.post()
            .uri(aiProperties.getRag().getBaseUrl() + "/api/ext/rag/documents")
            .header("X-API-KEY", aiProperties.getRag().getApiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(RagDocumentResponse.class);
    }
}
