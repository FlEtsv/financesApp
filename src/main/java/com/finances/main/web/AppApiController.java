package com.finances.main.web;

import com.finances.main.model.CategoryType;
import com.finances.main.model.PlannedMovement;
import com.finances.main.model.Transaction;
import com.finances.main.service.AccountService;
import com.finances.main.service.BudgetService;
import com.finances.main.service.FinancialGoalService;
import com.finances.main.service.LedgerService;
import com.finances.main.service.PlannedMovementService;
import com.finances.main.service.TransactionService;
import com.finances.main.service.ai.AiContextService;
import com.finances.main.service.ai.ExtChatClient;
import com.finances.main.web.dto.AccountDtos.AccountBalanceUpdateRequest;
import com.finances.main.web.dto.AccountDtos.AccountCreateRequest;
import com.finances.main.web.dto.AccountDtos.AccountSummary;
import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import com.finances.main.web.dto.BudgetDtos.BudgetSummaryResponse;
import com.finances.main.web.dto.BudgetDtos.MonthlySummaryResponse;
import com.finances.main.web.dto.FinancialGoalDtos.FinancialGoalCreateRequest;
import com.finances.main.web.dto.FinancialGoalDtos.FinancialGoalProgressRequest;
import com.finances.main.web.dto.FinancialGoalDtos.FinancialGoalResponse;
import com.finances.main.web.dto.LedgerResponses.BalanceByNameResponse;
import com.finances.main.web.dto.LedgerResponses.BalanceResponse;
import com.finances.main.web.dto.LedgerResponses.CategoryTotalsByNameResponse;
import com.finances.main.web.dto.LedgerResponses.CategoryTotalsResponse;
import com.finances.main.web.dto.LedgerResponses.TransactionSummary;
import com.finances.main.web.dto.LedgerResponses.TransactionsByNameResponse;
import com.finances.main.web.dto.LedgerResponses.TransactionsResponse;
import com.finances.main.web.dto.PlannedMovementDtos.FixedMovementsOverview;
import com.finances.main.web.dto.PlannedMovementDtos.PlannedMovementRequest;
import com.finances.main.web.dto.PlannedMovementDtos.PlannedMovementResponse;
import com.finances.main.web.dto.TransactionDtos.TransactionCreateRequest;
import com.finances.main.web.dto.TransactionDtos.TransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints /app/api para consumo de la interfaz web básica.
 */
@RestController
@RequestMapping("/app/api")
public class AppApiController {
    private final LedgerService ledgerService;
    private final AccountService accountService;
    private final PlannedMovementService plannedMovementService;
    private final TransactionService transactionService;
    private final FinancialGoalService financialGoalService;
    private final BudgetService budgetService;
    private final AiContextService aiContextService;
    private final ExtChatClient extChatClient;

    public AppApiController(
        LedgerService ledgerService,
        AccountService accountService,
        PlannedMovementService plannedMovementService,
        TransactionService transactionService,
        FinancialGoalService financialGoalService,
        BudgetService budgetService,
        AiContextService aiContextService,
        ExtChatClient extChatClient
    ) {
        this.ledgerService = ledgerService;
        this.accountService = accountService;
        this.plannedMovementService = plannedMovementService;
        this.transactionService = transactionService;
        this.financialGoalService = financialGoalService;
        this.budgetService = budgetService;
        this.aiContextService = aiContextService;
        this.extChatClient = extChatClient;
    }

    /**
     * Devuelve el balance de una cuenta para la interfaz web.
     */
    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        BigDecimal balance = ledgerService.calculateBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }

    /**
     * Devuelve el balance de una cuenta por nombre para la interfaz web.
     */
    @GetMapping("/accounts/by-name/{accountName}/balance")
    public BalanceByNameResponse getBalanceByName(@PathVariable String accountName) {
        BigDecimal balance = ledgerService.calculateBalance(accountName);
        return new BalanceByNameResponse(accountName, balance);
    }

    /**
     * Devuelve totales agrupados por categoría para la interfaz web.
     */
    @GetMapping("/accounts/{accountId}/totals")
    public CategoryTotalsResponse getTotalsByCategory(
        @PathVariable Long accountId,
        @RequestParam CategoryType type
    ) {
        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(accountId, type);
        return new CategoryTotalsResponse(accountId, type, totals);
    }

    /**
     * Devuelve totales agrupados por categoría para una cuenta por nombre.
     */
    @GetMapping("/accounts/by-name/{accountName}/totals")
    public CategoryTotalsByNameResponse getTotalsByCategoryByName(
        @PathVariable String accountName,
        @RequestParam CategoryType type
    ) {
        Map<String, BigDecimal> totals = ledgerService.totalsByCategory(accountName, type);
        return new CategoryTotalsByNameResponse(accountName, type, totals);
    }

    /**
     * Devuelve transacciones en un rango de fechas para la interfaz web.
     */
    @GetMapping("/accounts/{accountId}/transactions")
    public TransactionsResponse getTransactions(
        @PathVariable Long accountId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Transaction> transactions = ledgerService.listTransactions(accountId, startDate, endDate);
        List<TransactionSummary> summaries = transactions.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return new TransactionsResponse(accountId, startDate, endDate, summaries);
    }

    /**
     * Devuelve transacciones en un rango de fechas para una cuenta por nombre.
     */
    @GetMapping("/accounts/by-name/{accountName}/transactions")
    public TransactionsByNameResponse getTransactionsByName(
        @PathVariable String accountName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Transaction> transactions = ledgerService.listTransactions(accountName, startDate, endDate);
        List<TransactionSummary> summaries = transactions.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return new TransactionsByNameResponse(accountName, startDate, endDate, summaries);
    }

    /**
     * Lista cuentas disponibles para la interfaz web.
     */
    @GetMapping("/accounts")
    public List<AccountSummary> listAccounts() {
        return accountService.listAccounts().stream()
            .sorted(Comparator.comparing(account -> account.getName().toLowerCase()))
            .map(account -> new AccountSummary(
                account.getId(),
                account.getName(),
                account.getCurrency(),
                account.getInitialBalance(),
                account.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Crea una nueva cuenta desde la interfaz web.
     */
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountSummary createAccount(@RequestBody AccountCreateRequest request) {
        var account = accountService.createAccount(request.name(), request.currency(), request.initialBalance());
        return new AccountSummary(
            account.getId(),
            account.getName(),
            account.getCurrency(),
            account.getInitialBalance(),
            account.getCreatedAt()
        );
    }

    /**
     * Actualiza el saldo inicial de una cuenta.
     */
    @PostMapping("/accounts/by-name/{accountName}/initial-balance")
    public AccountSummary updateInitialBalance(
        @PathVariable String accountName,
        @RequestBody AccountBalanceUpdateRequest request
    ) {
        var account = accountService.updateInitialBalance(accountName, request.initialBalance());
        return new AccountSummary(
            account.getId(),
            account.getName(),
            account.getCurrency(),
            account.getInitialBalance(),
            account.getCreatedAt()
        );
    }

    /**
     * Registra un movimiento real (ingreso o gasto).
     */
    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionCreateRequest request) {
        Transaction transaction = transactionService.registerTransaction(
            request.accountName(),
            request.categoryName(),
            request.categoryType(),
            request.amount(),
            request.transactionDate(),
            request.description()
        );
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAccount().getName(),
            transaction.getCategory().getName(),
            transaction.getCategory().getType(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getDescription()
        );
    }

    /**
     * Elimina una transacción registrada desde la interfaz web.
     */
    @DeleteMapping("/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransactionById(transactionId);
    }

    /**
     * Registra un movimiento planificado (gasto/ingreso).
     */
    @PostMapping("/planned-movements")
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedMovementResponse createPlannedMovement(@RequestBody PlannedMovementRequest request) {
        PlannedMovement movement = plannedMovementService.createPlannedMovement(
            request.accountName(),
            request.name(),
            request.amount(),
            request.type(),
            request.periodicidad(),
            request.startDate(),
            request.active()
        );
        return toMovementResponse(movement);
    }

    /**
     * Lista movimientos planificados por nombre de cuenta.
     */
    @GetMapping("/planned-movements")
    public List<PlannedMovementResponse> listPlannedMovements(@RequestParam String accountName) {
        return plannedMovementService.listByAccountName(accountName).stream()
            .map(this::toMovementResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lista movimientos fijos e ingresos fijos por cuenta.
     */
    @GetMapping("/planned-movements/fixed")
    public FixedMovementsOverview listFixedMovements(@RequestParam String accountName) {
        List<PlannedMovementResponse> fixedExpenses = plannedMovementService.listByAccountNameAndTypes(
            accountName,
            List.copyOf(EnumSet.of(com.finances.main.model.PlannedMovementType.GASTO_FIJO))
        ).stream().map(this::toMovementResponse).collect(Collectors.toList());

        List<PlannedMovementResponse> fixedIncome = plannedMovementService.listByAccountNameAndTypes(
            accountName,
            List.copyOf(EnumSet.of(com.finances.main.model.PlannedMovementType.INGRESO_FIJO_NOMINA))
        ).stream().map(this::toMovementResponse).collect(Collectors.toList());

        BigDecimal totalFixedExpenses = fixedExpenses.stream()
            .map(PlannedMovementResponse::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFixedIncome = fixedIncome.stream()
            .map(PlannedMovementResponse::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FixedMovementsOverview(
            accountName,
            totalFixedExpenses,
            totalFixedIncome,
            fixedExpenses,
            fixedIncome
        );
    }

    /**
     * Lista objetivos financieros por cuenta.
     */
    @GetMapping("/goals")
    public List<FinancialGoalResponse> listGoals(@RequestParam String accountName) {
        return financialGoalService.listGoals(accountName).stream()
            .map(goal -> new FinancialGoalResponse(
                goal.getId(),
                goal.getAccount().getName(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetDate(),
                goal.getDescription(),
                goal.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Crea un objetivo financiero.
     */
    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialGoalResponse createGoal(@RequestBody FinancialGoalCreateRequest request) {
        var goal = financialGoalService.createGoal(
            request.accountName(),
            request.name(),
            request.targetAmount(),
            request.currentAmount(),
            request.targetDate(),
            request.description()
        );
        return new FinancialGoalResponse(
            goal.getId(),
            goal.getAccount().getName(),
            goal.getName(),
            goal.getTargetAmount(),
            goal.getCurrentAmount(),
            goal.getTargetDate(),
            goal.getDescription(),
            goal.getCreatedAt()
        );
    }

    /**
     * Registra progreso en un objetivo financiero.
     */
    @PostMapping("/goals/{goalId}/progress")
    public FinancialGoalResponse addGoalProgress(
        @PathVariable Long goalId,
        @RequestBody FinancialGoalProgressRequest request
    ) {
        var goal = financialGoalService.addProgress(goalId, request.amount());
        return new FinancialGoalResponse(
            goal.getId(),
            goal.getAccount().getName(),
            goal.getName(),
            goal.getTargetAmount(),
            goal.getCurrentAmount(),
            goal.getTargetDate(),
            goal.getDescription(),
            goal.getCreatedAt()
        );
    }

    /**
     * Construye el resumen presupuestario para un rango de fechas.
     */
    @GetMapping("/budget/summary")
    public BudgetSummaryResponse getBudgetSummary(
        @RequestParam String accountName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var summary = budgetService.buildSummary(accountName, startDate, endDate);
        return new BudgetSummaryResponse(
            summary.accountName(),
            summary.startDate(),
            summary.endDate(),
            summary.initialBalance(),
            summary.fixedIncome(),
            summary.fixedExpense(),
            summary.actualIncome(),
            summary.actualExpense(),
            summary.expectedBalance(),
            summary.actualBalance()
        );
    }

    /**
     * Devuelve un resumen mensual por año.
     */
    @GetMapping("/budget/monthly")
    public List<MonthlySummaryResponse> getMonthlySummary(
        @RequestParam String accountName,
        @RequestParam int year
    ) {
        return budgetService.buildMonthlySummary(accountName, year).stream()
            .map(summary -> new MonthlySummaryResponse(
                summary.year(),
                summary.month(),
                summary.income(),
                summary.expense(),
                summary.balance()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Construye el contexto usado por la IA para enriquecer la conversación.
     */
    @GetMapping("/ai/context")
    public AiContextResponse buildAiContext(
        @RequestParam String accountName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam CategoryType categoryType
    ) {
        return aiContextService.buildContext(accountName, startDate, endDate, categoryType);
    }

    /**
     * Envía mensajes al endpoint de IA externo configurado.
     */
    @PostMapping("/ai/chat")
    public AiChatResponse chatWithAi(@RequestBody AiChatRequest request) {
        AiChatRequest enrichedRequest = aiContextService.enrichChatRequest(request);
        return extChatClient.sendChat(enrichedRequest);
    }

    private TransactionSummary toSummary(Transaction transaction) {
        return new TransactionSummary(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getDescription(),
            transaction.getCategory().getName(),
            transaction.getCategory().getType()
        );
    }

    private PlannedMovementResponse toMovementResponse(PlannedMovement movement) {
        return new PlannedMovementResponse(
            movement.getId(),
            movement.getAccount().getName(),
            movement.getName(),
            movement.getAmount(),
            movement.getType(),
            movement.getPeriodicidad(),
            movement.getStartDate(),
            movement.isActive()
        );
    }
}
