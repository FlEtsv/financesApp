# Finances App

Finances App es una aplicacion de gestion financiera basada en Spring Boot. Permite registrar ingresos y gastos, planificar movimientos recurrentes, administrar objetivos y visualizar un dashboard con indicadores. Incluye un frontend web estatico y servicios de IA para analisis contextual.

## Caracteristicas principales
- Libro mayor con cuentas, transacciones y saldos.
- Movimientos planificados: gastos fijos e ingresos recurrentes.
- Presupuestos por periodo y resumen mensual.
- Objetivos financieros con seguimiento y progreso.
- Dashboard con metricas, alertas y top categorias.
- Asistente de IA con contexto financiero y RAG opcional.

## Stack tecnico
- Java 21, Spring Boot, Spring Data JPA.
- MySQL en runtime, H2 en tests.
- UI estatica en `src/main/resources/static/app`.

## Requisitos
- JDK 21.
- MySQL 8+ (o Docker).
- Gradle wrapper incluido.

## Configuracion rapida
Las variables se definen en `src/main/resources/application.yml` y se pueden sobrescribir por entorno.

Variables clave:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO`.
- `AI_EXT_BASE_URL`, `AI_EXT_API_KEY`, `AI_EXT_TIMEOUT`, `AI_EXT_FALLBACK_ENABLED`.
- `AI_RAG_BASE_URL`, `AI_RAG_API_KEY`, `AI_RAG_TIMEOUT`.
- `AI_RECOMMENDATIONS_INTERVAL_MS`, `AI_RECOMMENDATIONS_LOOKBACK_DAYS`, `AI_RECOMMENDATIONS_CATEGORY_TYPE`.

Detalle completo en `docs/CONFIGURATION.md` y `docs/AI.md`.

## Ejecucion local
```bash
./gradlew bootRun
```

- UI: `http://localhost:8080/app`
- API: `http://localhost:8080/app/api`

## Pruebas
```bash
./gradlew test
```

## Docker
```bash
docker compose up --build
```

## Documentacion
- `docs/API.md`
- `docs/DB.md`
- `docs/DOCKER.md`
- `docs/CONFIGURATION.md`
- `docs/AI.md`
