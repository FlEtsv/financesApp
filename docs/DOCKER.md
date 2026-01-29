# Docker (genérico)

## Dockerfile
El proyecto incluye un `Dockerfile` multi-etapa para construir el JAR con Gradle y ejecutar la aplicación con JRE 21.

## docker-compose
Se incluye un `docker-compose.yml` con dos servicios:
- `app`: la aplicación Spring Boot.
- `db`: MySQL para persistencia.

## Ejecución
```bash
docker compose up --build
```

## Variables de entorno
- `DB_URL`: cadena JDBC (por defecto `jdbc:mysql://db:3306/finances_db`).
- `DB_USERNAME`: usuario de base de datos.
- `DB_PASSWORD`: contraseña de base de datos.
- `DB_DDL_AUTO`: estrategia de creación (`update`, `validate`, etc.).

## Puertos
- Aplicación: `8080`.
- MySQL: `3306`.
