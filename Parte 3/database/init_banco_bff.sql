-- Banco XYZ / EFT Backend III - Parte 3
-- Crear estructura inicial SIN eliminar bases, tablas ni registros.
-- IMPORTANTE: CREATE TABLE IF NOT EXISTS no modifica estructuras preexistentes.

CREATE DATABASE IF NOT EXISTS banco_bff
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE banco_bff;

CREATE TABLE IF NOT EXISTS cuenta_interes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cuenta_id BIGINT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  saldo DECIMAL(15,2) NOT NULL,
  edad INT NOT NULL,
  tipo VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS transaccion_diaria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaccion_id BIGINT,
  fecha DATE NOT NULL,
  monto DECIMAL(15,2) NOT NULL,
  tipo VARCHAR(30) NOT NULL
);

-- Coincide con Cliente.java.
CREATE TABLE IF NOT EXISTS cliente (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  telefono VARCHAR(30),
  perfil VARCHAR(50) NOT NULL
);

-- Coincide con MovimientoAnual.java.
-- El enum Java admite PAGO, TRANSFERENCIA, DEPOSITO.
CREATE TABLE IF NOT EXISTS movimiento_anual (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cuenta_id BIGINT,
  fecha DATE,
  tipo VARCHAR(30),
  monto DECIMAL(38,2),
  descripcion VARCHAR(255)
);

-- Tablas complementarias incluidas en el SQL proporcionado.
CREATE TABLE IF NOT EXISTS account (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL UNIQUE,
  holder_name VARCHAR(150) NOT NULL,
  type VARCHAR(30) NOT NULL,
  balance DECIMAL(15,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS bank_transaction (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id BIGINT NOT NULL UNIQUE,
  account_id BIGINT NOT NULL,
  type VARCHAR(30) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  description VARCHAR(150),
  transaction_date DATE NOT NULL
);

-- Verificación sin modificación de datos
SHOW TABLES;
DESCRIBE cliente;
DESCRIBE movimiento_anual;
