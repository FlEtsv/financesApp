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
 * Pruebas unitarias del controlador de libro mayor.
 */
@WebMvcTest(LedgerController.class)
class LedgerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerService ledgerService;

    @Test
    void returnsBalance() throws Exception {
        when(ledgerService.calculateBalance(1L)).thenReturn(new BigDecimal("500.00"));

        mockMvc.perform(get("/api/ledger/accounts/1/balance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(1))
            .andExpect(jsonPath("$.balance").value(500.00));
    }

    @Test
    void returnsTotalsByCategory() throws Exception {
        when(ledgerService.totalsByCategory(1L, CategoryType.GASTO))
            .thenReturn(Map.of("Renta", new BigDecimal("800.00")));

        mockMvc.perform(get("/api/ledger/accounts/1/totals")
                .param("type", "GASTO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("GASTO"))
            .andExpect(jsonPath("$.totals.Renta").value(800.00));
    }

    @Test
    void returnsTransactions() throws Exception {
        Account account = new Account("Cuenta", "USD");
        Category category = new Category("Salario", CategoryType.INGRESO);
        Transaction transaction = new Transaction(
            account,
            category,
            new BigDecimal("1200.00"),
            LocalDate.of(2024, 5, 1),
            "Pago"
        );

        when(ledgerService.listTransactions(1L, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 31)))
            .thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/ledger/accounts/1/transactions")
                .param("startDate", "2024-05-01")
                .param("endDate", "2024-05-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactions[0].amount").value(1200.00))
            .andExpect(jsonPath("$.transactions[0].categoryName").value("Salario"))
            .andExpect(jsonPath("$.transactions[0].categoryType").value("INGRESO"));
    }
}
