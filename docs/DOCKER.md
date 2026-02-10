# Docker

## Dockerfile
El proyecto incluye un `Dockerfile` multi-etapa para construir el JAR con Gradle y ejecutar la aplicacion con JRE 21.

## docker-compose
Se incluye un `docker-compose.yml` con dos servicios:
- `app`: la aplicacion Spring Boot.
- `db`: MySQL para persistencia.

## Ejecucion
```bash
docker compose up --build
```

## Variables de entorno
- `DB_URL`: cadena JDBC (por defecto `jdbc:mysql://db:3306/finances_db`).
- `DB_USERNAME`: usuario de base de datos.
- `DB_PASSWORD`: contrasena de base de datos.
- `DB_DDL_AUTO`: estrategia de creacion (`update`, `validate`, etc.).
- `AI_EXT_BASE_URL`, `AI_EXT_API_KEY`: IA externa (opcional).
- `AI_RAG_BASE_URL`, `AI_RAG_API_KEY`: RAG (opcional).

## Puertos
- Aplicacion: `8080`.
- MySQL: `3306`.
