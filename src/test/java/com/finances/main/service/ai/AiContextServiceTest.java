package com.finances.main.service.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.finances.main.model.Account;
import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.model.Periodicidad;
import com.finances.main.model.PlannedMovement;
import com.finances.main.model.PlannedMovementType;
import com.finances.main.model.Transaction;
import com.finances.main.service.LedgerService;
import com.finances.main.service.PlannedMovementService;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiContextServiceTest {
    @Mock
    private LedgerService ledgerService;

    @Mock
    private PlannedMovementService plannedMovementService;

    @InjectMocks
    private AiContextService aiContextService;

    @Test
    void buildContextAggregatesBackendData() {
        String accountName = "Cuenta demo";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        CategoryType categoryType = CategoryType.INGRESO;
        Account account = new Account(accountName, "EUR");
        Category category = new Category("Ventas", CategoryType.INGRESO);
        Transaction transaction = new Transaction(account, category, BigDecimal.TEN, endDate, "Ingreso");
        PlannedMovement plannedMovement = new PlannedMovement(
            "Nómina",
            BigDecimal.ONE,
            PlannedMovementType.INGRESO_FIJO_NOMINA,
            Periodicidad.MENSUAL,
            startDate,
            true,
            account
        );

        when(ledgerService.calculateBalance(accountName)).thenReturn(BigDecimal.TEN);
        when(ledgerService.totalsByCategory(accountName, categoryType)).thenReturn(Map.of("Ventas", BigDecimal.TEN));
        when(ledgerService.listTransactions(accountName, startDate, endDate)).thenReturn(List.of(transaction));
        when(plannedMovementService.listByAccountName(accountName)).thenReturn(List.of(plannedMovement));

        AiContextResponse response = aiContextService.buildContext(accountName, startDate, endDate, categoryType);

        assertThat(response.accountName()).isEqualTo(accountName);
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.totalsByCategory()).containsEntry("Ventas", BigDecimal.TEN);
        assertThat(response.recentTransactions()).hasSize(1);
        assertThat(response.plannedMovements()).hasSize(1);
    }

    @Test
    void enrichContextFillsMissingDataWhenPayloadIsIncomplete() {
        String accountName = "Cuenta principal";
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        CategoryType categoryType = CategoryType.GASTO;
        Account account = new Account(accountName, "EUR");
        Category category = new Category("Marketing", CategoryType.GASTO);
        Transaction transaction = new Transaction(account, category, BigDecimal.ONE, endDate, "Campaña");
        PlannedMovement plannedMovement = new PlannedMovement(
            "Hosting",
            BigDecimal.ONE,
            PlannedMovementType.GASTO_FIJO,
            Periodicidad.MENSUAL,
            startDate,
            true,
            account
        );

        AiContextResponse incompleteContext = new AiContextResponse(
            accountName,
            startDate,
            endDate,
            categoryType,
            null,
            null,
            List.of(),
            List.of()
        );

        when(ledgerService.calculateBalance(accountName)).thenReturn(BigDecimal.ONE);
        when(ledgerService.totalsByCategory(accountName, categoryType)).thenReturn(Map.of("Marketing", BigDecimal.ONE));
        when(ledgerService.listTransactions(accountName, startDate, endDate)).thenReturn(List.of(transaction));
        when(plannedMovementService.listByAccountName(accountName)).thenReturn(List.of(plannedMovement));

        AiContextResponse enriched = aiContextService.enrichContext(incompleteContext);

        assertThat(enriched.balance()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(enriched.totalsByCategory()).containsEntry("Marketing", BigDecimal.ONE);
        assertThat(enriched.recentTransactions()).hasSize(1);
        assertThat(enriched.plannedMovements()).hasSize(1);
    }
}
