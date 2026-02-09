package com.finances.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.finances.main.service.ai.AiProperties;
import com.finances.main.web.dto.RagDtos.RagDocumentRequest;
import com.finances.main.web.dto.RagDtos.RagDocumentResponse;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class RagClientServiceTest {

    @Test
    void sendDocumentReturnsResponseWhenServiceIsAvailable() {
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        RagDocumentResponse expected = new RagDocumentResponse("ok", "123");
        when(restClient.post()
            .uri(anyString())
            .header(anyString(), anyString())
            .contentType(any(MediaType.class))
            .body(any(RagDocumentRequest.class))
            .retrieve()
            .body(RagDocumentResponse.class))
            .thenReturn(expected);

        RagClientService service = new RagClientService(mockBuilder(restClient), buildProperties());
        RagDocumentResponse response = service.sendDocument(new RagDocumentRequest("titulo", "contenido"));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void sendDocumentThrowsWhenContentIsBlank() {
        RagClientService service = new RagClientService(mockBuilder(mock(RestClient.class)), buildProperties());

        assertThatThrownBy(() -> service.sendDocument(new RagDocumentRequest("titulo", " ")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("content");
    }

    @Test
    void sendDocumentWrapsRestClientErrors() {
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        when(restClient.post()
            .uri(anyString())
            .header(anyString(), anyString())
            .contentType(any(MediaType.class))
            .body(any(RagDocumentRequest.class))
            .retrieve()
            .body(RagDocumentResponse.class))
            .thenThrow(new RestClientException("boom"));

        RagClientService service = new RagClientService(mockBuilder(restClient), buildProperties());

        assertThatThrownBy(() -> service.sendDocument(new RagDocumentRequest("titulo", "contenido")))
            .isInstanceOf(RagUnavailableException.class)
            .hasMessageContaining("RAG");
    }

    private RestClient.Builder mockBuilder(RestClient restClient) {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);
        return builder;
    }

    private AiProperties buildProperties() {
        AiProperties properties = new AiProperties();
        properties.getRag().setBaseUrl("http://localhost:8080");
        properties.getRag().setApiKey("test-key");
        properties.getRag().setTimeoutSeconds(5);
        return properties;
    }
}
