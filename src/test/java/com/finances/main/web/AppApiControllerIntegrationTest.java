package com.finances.main.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppApiControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAccountAndPlannedMovementFlow() throws Exception {
        String accountPayload = """
            {"name":"Cuenta demo","currency":"EUR"}
            """;

        MvcResult accountResult = mockMvc.perform(post("/app/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountPayload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode accountNode = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        assertThat(accountNode.get("name").asText()).isEqualTo("Cuenta demo");

        String plannedPayload = String.format(
            """
                {"accountName":"Cuenta demo","name":"Nómina","amount":1500.00,
                 "type":"INGRESO_FIJO_NOMINA","periodicidad":"MENSUAL","startDate":"%s","active":true}
                """,
            LocalDate.now()
        );

        mockMvc.perform(post("/app/api/planned-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(plannedPayload))
            .andExpect(status().isCreated());

        MvcResult listResult = mockMvc.perform(get("/app/api/planned-movements")
                .param("accountName", "Cuenta demo"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode listNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(listNode.isArray()).isTrue();
        assertThat(listNode.size()).isEqualTo(1);
    }
}
