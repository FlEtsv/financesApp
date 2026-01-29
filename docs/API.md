# API Finances App

## Endpoints de la interfaz web (`/app/api`)

### Obtener balance
`GET /app/api/accounts/{accountId}/balance`

**Respuesta**
```json
{
  "accountId": 1,
  "balance": 1200.00
}
```

### Totales por categoría
`GET /app/api/accounts/{accountId}/totals?type=INCOME|EXPENSE`

**Respuesta**
```json
{
  "accountId": 1,
  "type": "INCOME",
  "totals": {
    "Salario": 3000.00
  }
}
```

### Transacciones por rango de fechas
`GET /app/api/accounts/{accountId}/transactions?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

**Respuesta**
```json
{
  "accountId": 1,
  "startDate": "2024-05-01",
  "endDate": "2024-05-31",
  "transactions": [
    {
      "id": 10,
      "amount": 150.00,
      "transactionDate": "2024-05-05",
      "description": "Pago",
      "categoryName": "Servicios",
      "categoryType": "EXPENSE"
    }
  ]
}
```

## Endpoints REST originales (`/api/ledger`)
Los endpoints existentes permanecen disponibles para integraciones externas.
