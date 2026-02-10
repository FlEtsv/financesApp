# SQL para crear la base de datos

Usa este script para crear la base de datos y el usuario por defecto.

```sql
CREATE DATABASE IF NOT EXISTS finances_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'finances'@'%' IDENTIFIED BY 'finances';
GRANT ALL PRIVILEGES ON finances_db.* TO 'finances'@'%';
FLUSH PRIVILEGES;
```

Tambien puedes ejecutar el archivo `docs/DB.sql`.
