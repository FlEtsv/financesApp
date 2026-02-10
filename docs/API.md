# API Finances App

## Convenciones
- Fechas en formato `YYYY-MM-DD`.
- Montos como decimal (BigDecimal).

## Endpoints de la interfaz web (`/app/api`)

### Cuentas
- `GET /app/api/accounts`
- `POST /app/api/accounts`
- `POST /app/api/accounts/by-name/{accountName}/initial-balance`
- `GET /app/api/accounts/{accountId}/balance`
- `GET /app/api/accounts/by-name/{accountName}/balance`

### Categorias
- `GET /app/api/categories?type=INGRESO|GASTO`

### Totales por categoria
- `GET /app/api/accounts/{accountId}/totals?type=INGRESO|GASTO`
- `GET /app/api/accounts/by-name/{accountName}/totals?type=INGRESO|GASTO`

### Transacciones
- `POST /app/api/transactions`
- `DELETE /app/api/transactions/{transactionId}`
- `GET /app/api/accounts/{accountId}/transactions?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- `GET /app/api/accounts/by-name/{accountName}/transactions?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

### Movimientos planificados
- `POST /app/api/planned-movements`
- `GET /app/api/planned-movements?accountName=...`
- `GET /app/api/planned-movements/fixed?accountName=...`

Tipos soportados en `PlannedMovementType`: `GASTO_FIJO`, `GASTO_VARIABLE`, `INGRESO_VARIABLE`, `INGRESO_FIJO_NOMINA`.

### Objetivos financieros
- `GET /app/api/goals?accountName=...`
- `POST /app/api/goals`
- `POST /app/api/goals/{goalId}/progress`

### Presupuestos y dashboard
- `GET /app/api/budget/summary?accountName=...&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- `GET /app/api/budget/monthly?accountName=...&year=YYYY`
- `GET /app/api/dashboard/insights?accountName=...&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

### IA
- `GET /app/api/ai/context?accountName=...&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&categoryType=INGRESO|GASTO`
- `POST /app/api/ai/chat`
- `GET /app/api/ai/recommendations?accountName=...`
- `POST /app/api/ai/rag`

### Ejemplo: crear transaccion
`POST /app/api/transactions`

```json
{
  "accountName": "Principal",
  "categoryName": "Alquiler",
  "categoryType": "GASTO",
  "amount": 950.00,
  "transactionDate": "2025-01-10",
  "description": "Enero"
}
```

## Endpoints de chat (legacy / externos)
- `POST /api/chat` (sin autenticacion).
- `POST /api/ext/chat` (requiere header `x-api-key`).

## Endpoints REST originales (`/api/ledger`)
Los endpoints existentes permanecen disponibles para integraciones externas.
