# Finances App

Aplicación Spring Boot para gestionar un libro mayor con endpoints REST y una interfaz web básica. El desarrollo sigue principios SOLID y una estructura modular para facilitar la evolución en ciclos AGILE.

## Estructura rápida
- `src/main/java/com/finances/main/web`: controladores y DTOs web.
- `src/main/resources/static/app`: interfaz web básica (HTML/JS).
- `docs/`: documentación operativa (API, Docker y base de datos).

## Endpoints principales
- **UI**: `GET /app` (interfaz web básica).
- **API web**: `GET /app/api/accounts/{id}/balance`
- **API web**: `GET /app/api/accounts/{id}/totals?type=INCOME|EXPENSE`
- **API web**: `GET /app/api/accounts/{id}/transactions?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- **API ledger**: `GET /api/ledger/...` (endpoints originales).

Consulta más detalles en [docs/API.md](docs/API.md).

## Base de datos
Consulta el script SQL en [docs/DB.md](docs/DB.md).

## Ejecución local
```bash
./gradlew bootRun
```

Variables de entorno recomendadas:
- `DB_URL` (por ejemplo `jdbc:mysql://localhost:3306/finances_db`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_DDL_AUTO` (`update`, `validate`, etc.)

## Docker
Consulta la guía de despliegue en [docs/DOCKER.md](docs/DOCKER.md).
