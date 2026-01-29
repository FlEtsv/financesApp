-- Script para crear la base de datos y usuario de la aplicación.
CREATE DATABASE IF NOT EXISTS finances_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'finances'@'%' IDENTIFIED BY 'finances';
GRANT ALL PRIVILEGES ON finances_db.* TO 'finances'@'%';
FLUSH PRIVILEGES;
