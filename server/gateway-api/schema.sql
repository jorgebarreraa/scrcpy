-- =============================================================
--  Powerbox Gateway — Schema completo
--  Base de datos : powerboxchile_ips
-- =============================================================

CREATE DATABASE IF NOT EXISTS `powerboxchile_ips`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE `powerboxchile_ips`;

-- Tabla principal (igual a la original + columnas WireGuard)
CREATE TABLE IF NOT EXISTS `vending_machines` (
    `id`               INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `device_no`        VARCHAR(64)  NOT NULL COMMENT 'ID interno (deviceNo / android_id)',
    `device_ext_no`    VARCHAR(32)  NOT NULL COMMENT 'ID externo visible (deviceExtNo)',
    `device_name`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'Nombre de la máquina',
    `device_type`      VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Modelo / serie',
    `public_ip`        VARCHAR(45)  NOT NULL DEFAULT '' COMMENT 'IP pública actual del dispositivo',
    `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '1=online, 0=offline',
    `last_seen`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Columnas WireGuard (se agregan con ALTER si la tabla ya existe)
    `wireguard_ip`     VARCHAR(18)  NULL DEFAULT NULL COMMENT 'IP asignada túnel WireGuard (10.99.0.x/32)',
    `wg_public_key`    VARCHAR(64)  NULL DEFAULT NULL COMMENT 'Clave pública WireGuard del dispositivo',
    `wg_peer_active`   TINYINT      NOT NULL DEFAULT 0 COMMENT '1=peer ya agregado en el VPS via wg set',
    `wg_registered_at` DATETIME     NULL DEFAULT NULL COMMENT 'Fecha de primer registro WireGuard',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_device` (`device_no`, `device_ext_no`),
    UNIQUE KEY `uq_wireguard_ip` (`wireguard_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================
-- Si la tabla ya existe (BD en producción), agregar columnas WireGuard
-- Ejecutar cada ALTER por separado; ignorar el error si la columna ya existe
-- Compatible con MySQL 5.7+
-- =============================================================
ALTER TABLE `vending_machines` ADD COLUMN `wireguard_ip`     VARCHAR(18) NULL DEFAULT NULL COMMENT 'IP asignada túnel WireGuard (10.99.0.x/32)';
ALTER TABLE `vending_machines` ADD COLUMN `wg_public_key`    VARCHAR(64) NULL DEFAULT NULL COMMENT 'Clave pública WireGuard del dispositivo';
ALTER TABLE `vending_machines` ADD COLUMN `wg_peer_active`   TINYINT NOT NULL DEFAULT 0 COMMENT '1=peer ya agregado en el VPS via wg set';
ALTER TABLE `vending_machines` ADD COLUMN `wg_registered_at` DATETIME NULL DEFAULT NULL COMMENT 'Fecha de primer registro WireGuard';


-- =============================================================
-- Peers manuales: PC, Android, iOS — creados desde el panel web
-- IPs asignadas: 10.99.0.100–254 (para no colisionar con vending)
-- =============================================================
CREATE TABLE IF NOT EXISTS `wg_manual_peers` (
    `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `label`          VARCHAR(255) NOT NULL COMMENT 'Nombre descriptivo del cliente',
    `peer_type`      VARCHAR(20)  NOT NULL DEFAULT 'pc' COMMENT 'pc|android|ios|other',
    `wireguard_ip`   VARCHAR(18)  NOT NULL COMMENT 'IP asignada en el túnel (10.99.0.x/32)',
    `wg_public_key`  VARCHAR(64)  NOT NULL COMMENT 'Clave pública WireGuard del cliente',
    `wg_peer_active` TINYINT      NOT NULL DEFAULT 0 COMMENT '1=peer sincronizado en el VPS',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_wg_ip`     (`wireguard_ip`),
    UNIQUE KEY `uq_wg_pubkey` (`wg_public_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
