#!/bin/bash
# Database-per-service: cada microservicio tiene su propio esquema y un usuario
# con permisos SOLO sobre ese esquema (principio de mínimo privilegio).
# Las tablas las crea Flyway al arrancar cada servicio.
set -euo pipefail

mariadb --protocol=socket -uroot -p"${MARIADB_ROOT_PASSWORD}" <<-SQL
    CREATE DATABASE IF NOT EXISTS users_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS catalog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS orders_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    CREATE USER IF NOT EXISTS 'users_svc'@'%'   IDENTIFIED BY '${USERS_DB_PASSWORD}';
    CREATE USER IF NOT EXISTS 'catalog_svc'@'%' IDENTIFIED BY '${CATALOG_DB_PASSWORD}';
    CREATE USER IF NOT EXISTS 'orders_svc'@'%'  IDENTIFIED BY '${ORDERS_DB_PASSWORD}';

    GRANT ALL PRIVILEGES ON users_db.*   TO 'users_svc'@'%';
    GRANT ALL PRIVILEGES ON catalog_db.* TO 'catalog_svc'@'%';
    GRANT ALL PRIVILEGES ON orders_db.*  TO 'orders_svc'@'%';
    FLUSH PRIVILEGES;
SQL
