package com.finances.main.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "ai.ext.base-url=http://localhost:0/api/ext/chat"
})
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

    @Test
    void createAndDeleteTransactionFlow() throws Exception {
        String accountPayload = """
            {"name":"Cuenta movimientos","currency":"EUR"}
            """;

        mockMvc.perform(post("/app/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountPayload))
            .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        String transactionPayload = String.format(
            """
                {"accountName":"Cuenta movimientos","categoryName":"Venta","categoryType":"INGRESO",
                 "amount":100.00,"transactionDate":"%s","description":"Cobro cliente"}
                """,
            today
        );

        MvcResult transactionResult = mockMvc.perform(post("/app/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionPayload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode transactionNode = objectMapper.readTree(transactionResult.getResponse().getContentAsString());
        long transactionId = transactionNode.get("id").asLong();

        mockMvc.perform(delete("/app/api/transactions/{transactionId}", transactionId))
            .andExpect(status().isNoContent());

        LocalDate startDate = today.withDayOfMonth(1);
        MvcResult listResult = mockMvc.perform(get("/app/api/accounts/by-name/{accountName}/transactions", "Cuenta movimientos")
                .param("startDate", startDate.toString())
                .param("endDate", today.toString()))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode listNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(listNode.get("transactions").isEmpty()).isTrue();
    }

    @Test
    void enrichesAiContextWhenChatPayloadIsIncomplete() throws Exception {
        String accountPayload = """
            {"name":"Cuenta IA","currency":"EUR"}
            """;

        mockMvc.perform(post("/app/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountPayload))
            .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        String transactionPayload = String.format(
            """
                {"accountName":"Cuenta IA","categoryName":"Marketing","categoryType":"GASTO",
                 "amount":75.00,"transactionDate":"%s","description":"Campaña"}
                """,
            today
        );

        mockMvc.perform(post("/app/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionPayload))
            .andExpect(status().isCreated());

        String chatPayload = String.format(
            """
                {"sessionId":"","message":"¿Cómo va el presupuesto?",
                 "context":{"accountName":"Cuenta IA","startDate":"%s","endDate":"%s",
                 "categoryType":"GASTO","balance":null,"totalsByCategory":null,
                 "recentTransactions":[],"plannedMovements":[]}}
                """,
            today.withDayOfMonth(1),
            today
        );

        MvcResult chatResult = mockMvc.perform(post("/app/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatPayload))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode response = objectMapper.readTree(chatResult.getResponse().getContentAsString());
        assertThat(response.get("reply").asText()).contains("balance");
        assertThat(response.get("reply").asText()).contains("totales por categoría");
    }
}
