package com.finances.main.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finances.main.model.Account;
import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas unitarias de los endpoints /app/api.
 */
@WebMvcTest(AppApiController.class)
class AppApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerService ledgerService;

    @Test
    void returnsBalanceForApp() throws Exception {
        when(ledgerService.calculateBalance(10L)).thenReturn(new BigDecimal("1200.00"));

        mockMvc.perform(get("/app/api/accounts/10/balance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(10))
            .andExpect(jsonPath("$.balance").value(1200.00));
    }

    @Test
    void returnsTotalsForApp() throws Exception {
        when(ledgerService.totalsByCategory(10L, CategoryType.INCOME))
            .thenReturn(Map.of("Ventas", new BigDecimal("3000.00")));

        mockMvc.perform(get("/app/api/accounts/10/totals")
                .param("type", "INCOME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(jsonPath("$.totals.Ventas").value(3000.00));
    }

    @Test
    void returnsTransactionsForApp() throws Exception {
        Account account = new Account("Cuenta", "EUR");
        Category category = new Category("Servicios", CategoryType.EXPENSE);
        Transaction transaction = new Transaction(
            account,
            category,
            new BigDecimal("99.99"),
            LocalDate.of(2024, 6, 1),
            "Suscripción"
        );

        when(ledgerService.listTransactions(10L, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30)))
            .thenReturn(List.of(transaction));

        mockMvc.perform(get("/app/api/accounts/10/transactions")
                .param("startDate", "2024-06-01")
                .param("endDate", "2024-06-30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactions[0].amount").value(99.99))
            .andExpect(jsonPath("$.transactions[0].categoryName").value("Servicios"))
            .andExpect(jsonPath("$.transactions[0].categoryType").value("EXPENSE"));
    }
}
