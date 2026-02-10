# IA y RAG

La aplicacion integra IA externa para chat y recomendaciones. El backend se comunica con un proveedor configurable y soporta ingestion RAG para enriquecer respuestas.

## Flujo basico
1. La UI llama a `/app/api/ai/context` para construir contexto.
2. La UI envia el mensaje a `/app/api/ai/chat`.
3. El backend reenvia la solicitud al proveedor externo configurado.

## Endpoints (App API)
- `GET /app/api/ai/context?accountName=...&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&categoryType=INGRESO|GASTO`
- `POST /app/api/ai/chat`
- `GET /app/api/ai/recommendations?accountName=...`
- `POST /app/api/ai/rag`

### Ejemplo: chat
```json
{
  "sessionId": "ui-123",
  "message": "Como va mi flujo de caja este mes?"
}
```

Respuesta:
```json
{
  "sessionId": "ui-123",
  "reply": "...",
  "respondedAt": "2025-02-01T12:00:00Z"
}
```

### Ejemplo: RAG
```json
{
  "title": "Factura Enero",
  "content": "Resumen de la factura..."
}
```

## Endpoint externo protegido
- `POST /api/ext/chat`
  - Requiere header `x-api-key`.
  - El cuerpo debe respetar el formato de `AiChatRequest` (message, sessionId, model, context).

## Configuracion
Ver `docs/CONFIGURATION.md` para variables `AI_EXT_*` y `AI_RAG_*`.

## Consideraciones
- Si el proveedor externo no responde, el backend devuelve 502.
- Evita versionar API keys en el repositorio.
