package com.finances.main.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finances.main.service.AccountService;
import com.finances.main.service.BudgetService;
import com.finances.main.service.FinancialGoalService;
import com.finances.main.service.LedgerService;
import com.finances.main.service.PlannedMovementService;
import com.finances.main.service.RagClientService;
import com.finances.main.service.RagUnavailableException;
import com.finances.main.service.TransactionService;
import com.finances.main.service.ai.AiContextService;
import com.finances.main.service.ai.AiRecommendationService;
import com.finances.main.service.ai.ExtChatClient;
import com.finances.main.web.dto.RagDtos.RagDocumentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppApiController.class)
class AppApiControllerRagTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerService ledgerService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private PlannedMovementService plannedMovementService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private FinancialGoalService financialGoalService;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private AiContextService aiContextService;

    @MockBean
    private ExtChatClient extChatClient;

    @MockBean
    private AiRecommendationService aiRecommendationService;

    @MockBean
    private RagClientService ragClientService;

    @Test
    void uploadRagReturnsResponseWhenRequestIsValid() throws Exception {
        when(ragClientService.sendDocument(any()))
            .thenReturn(new RagDocumentResponse("ok", "123"));

        mockMvc.perform(post("/app/api/ai/rag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Doc\",\"content\":\"Contenido\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void uploadRagReturnsBadRequestWhenContentIsBlank() throws Exception {
        mockMvc.perform(post("/app/api/ai/rag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Doc\",\"content\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void uploadRagReturnsBadGatewayWhenRagFails() throws Exception {
        when(ragClientService.sendDocument(any()))
            .thenThrow(new RagUnavailableException("Fallo", new RuntimeException("down")));

        mockMvc.perform(post("/app/api/ai/rag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Doc\",\"content\":\"Contenido\"}"))
            .andExpect(status().isBadGateway());
    }
}
