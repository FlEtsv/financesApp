package com.finances.main.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "ai.ext.api-key=test-key",
    "ai.system-prompt=Prompt de prueba"
})
class ExtChatControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsRequestsWithInvalidApiKey() throws Exception {
        String payload = """
            {"sessionId":"","message":"Hola","context":null}
            """;

        mockMvc.perform(post("/api/ext/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsNormalizedReplyWhenApiKeyIsValid() throws Exception {
        String payload = """
            {"sessionId":"","message":"Necesito ayuda","context":null}
            """;

        MvcResult result = mockMvc.perform(post("/api/ext/chat")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("sessionId").asText()).isNotBlank();
        assertThat(response.get("reply").asText()).contains("Necesito ayuda");
    }
}
