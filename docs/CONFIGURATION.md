# Configuracion

Esta guia resume las variables de entorno disponibles y como afectan la aplicacion.

## Base de datos
- `DB_URL`: URL JDBC. Ejemplo: `jdbc:mysql://localhost:3306/finances_db`.
- `DB_USERNAME`: usuario de base de datos.
- `DB_PASSWORD`: contrasena de base de datos.
- `DB_DDL_AUTO`: estrategia de creacion (create, update, validate, none).

## IA externa (chat)
- `AI_EXT_BASE_URL`: endpoint del proveedor externo. Ejemplo: `http://host:port/api/ext/chat`.
- `AI_EXT_API_KEY`: clave para autenticar la comunicacion.
- `AI_EXT_TIMEOUT`: timeout en segundos.
- `AI_EXT_FALLBACK_ENABLED`: true/false para habilitar fallback.

## RAG
- `AI_RAG_BASE_URL`: base URL del servicio RAG.
- `AI_RAG_API_KEY`: clave del servicio RAG.
- `AI_RAG_TIMEOUT`: timeout en segundos.

## Recomendaciones automaticas
- `AI_RECOMMENDATIONS_INTERVAL_MS`: intervalo de generacion.
- `AI_RECOMMENDATIONS_LOOKBACK_DAYS`: dias a considerar.
- `AI_RECOMMENDATIONS_CATEGORY_TYPE`: `INGRESO` o `GASTO`.

## Prompts y logging
- Los prompts se ajustan en `src/main/resources/application.yml` (`ai.system-prompt`, `ai.recommendations.prompt`).
- El logging se configura en `src/main/resources/application.yml`.
