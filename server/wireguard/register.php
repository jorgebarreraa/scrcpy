<?php
/**
 * POST /api/wireguard/register
 *
 * Recibe la solicitud de registro WireGuard desde una máquina vending.
 * Asigna una IP del rango 10.99.0.0/24, actualiza la tabla vending_machines
 * en MySQL, y agrega el peer al servidor WireGuard via shell.
 *
 * INSTALACIÓN:
 *   Copiar a: /var/www/html/api/wireguard/register.php
 *   (o configurar el router PHP de tu framework)
 *
 * RANGO WireGuard:
 *   Servidor: 10.99.0.1  (wg0 en el VPS)
 *   Máquinas: 10.99.0.2 → 10.99.0.254
 *   Puerto:   51820/udp
 */

header('Content-Type: application/json; charset=utf-8');

// ──────────────────────────────────────────────
// CONFIGURACIÓN — ajustar según tu entorno
// ──────────────────────────────────────────────
define('DB_HOST',    'localhost');
define('DB_NAME',    'powerboxchile_ips');
define('DB_USER',    'tu_usuario');
define('DB_PASS',    'tu_password');

define('WG_INTERFACE',       'wg0');
define('WG_SERVER_PUBKEY',   'PEGA_AQUI_LA_CLAVE_PUBLICA_DEL_SERVIDOR');
define('WG_SERVER_ENDPOINT', 'vpn.powerboxchile.cl:51820');
define('WG_DNS',             '1.1.1.1');
define('WG_SUBNET_BASE',     '10.99.0');   // IPs: 10.99.0.2 → 10.99.0.254
define('WG_SUBNET_START',    2);           // primera IP asignable
define('WG_SUBNET_END',      254);         // última IP asignable
// ──────────────────────────────────────────────

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method Not Allowed']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid JSON body']);
    exit;
}

$deviceNo    = trim($input['device_no']    ?? '');
$deviceExtNo = trim($input['device_ext_no'] ?? '');
$deviceName  = trim($input['device_name']  ?? '');
$publicKey   = trim($input['public_key']   ?? '');

if (!$deviceNo || !$publicKey) {
    http_response_code(400);
    echo json_encode(['error' => 'device_no y public_key son requeridos']);
    exit;
}

// Validar formato de clave pública WireGuard (base64, 44 chars)
if (!preg_match('/^[A-Za-z0-9+\/]{43}=$/', $publicKey)) {
    http_response_code(400);
    echo json_encode(['error' => 'public_key inválida']);
    exit;
}

try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER, DB_PASS,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
    );
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => 'DB connection failed']);
    error_log('[WG] DB error: ' . $e->getMessage());
    exit;
}

// ──────────────────────────────────────────────
// Agregar columnas si no existen (idempotente)
// ──────────────────────────────────────────────
try {
    $pdo->exec("
        ALTER TABLE vending_machines
            ADD COLUMN IF NOT EXISTS wireguard_ip  VARCHAR(18)  NULL COMMENT 'IP asignada en túnel WireGuard (10.99.0.x/32)',
            ADD COLUMN IF NOT EXISTS wg_public_key VARCHAR(64)  NULL COMMENT 'Clave pública WireGuard del dispositivo',
            ADD COLUMN IF NOT EXISTS wg_registered_at DATETIME NULL COMMENT 'Fecha de primer registro WireGuard'
    ");
} catch (PDOException $e) {
    // Si la columna ya existe en versiones MySQL < 8.0, ignorar
    error_log('[WG] ALTER TABLE (ignorable): ' . $e->getMessage());
}

// ──────────────────────────────────────────────
// Buscar máquina existente por device_no
// ──────────────────────────────────────────────
$stmt = $pdo->prepare("SELECT * FROM vending_machines WHERE device_no = ? LIMIT 1");
$stmt->execute([$deviceNo]);
$machine = $stmt->fetch(PDO::FETCH_ASSOC);

// ──────────────────────────────────────────────
// Asignar IP WireGuard
// ──────────────────────────────────────────────
if ($machine && !empty($machine['wireguard_ip'])) {
    // Ya tiene IP asignada → reusar (permite re-registro por cambio de clave)
    $assignedIp = $machine['wireguard_ip'];
    error_log("[WG] Re-registro: device={$deviceExtNo} ip={$assignedIp}");
} else {
    // Obtener IPs ya usadas
    $usedStmt = $pdo->query("SELECT wireguard_ip FROM vending_machines WHERE wireguard_ip IS NOT NULL");
    $usedIps  = $usedStmt->fetchAll(PDO::FETCH_COLUMN);

    $assignedIp = null;
    for ($i = WG_SUBNET_START; $i <= WG_SUBNET_END; $i++) {
        $candidate = WG_SUBNET_BASE . '.' . $i . '/32';
        if (!in_array($candidate, $usedIps)) {
            $assignedIp = $candidate;
            break;
        }
    }

    if (!$assignedIp) {
        http_response_code(503);
        echo json_encode(['error' => 'Sin IPs disponibles en el pool WireGuard']);
        exit;
    }
}

// IP sin el /32 para el comando wg
$ipBare = str_replace('/32', '', $assignedIp);

// ──────────────────────────────────────────────
// Agregar / actualizar peer en WireGuard via shell
// ──────────────────────────────────────────────
// Nota: el usuario que ejecuta PHP necesita sudo para wg sin password.
// Agregar en /etc/sudoers: www-data ALL=(ALL) NOPASSWD: /usr/bin/wg
$wgCmd  = escapeshellcmd("wg set " . WG_INTERFACE . " peer " . $publicKey . " allowed-ips " . $ipBare . "/32 persistent-keepalive 25");
$output = shell_exec("sudo " . $wgCmd . " 2>&1");
error_log("[WG] wg set output: " . ($output ?? 'ok'));

// Persistir config para que sobreviva reinicios
shell_exec("sudo wg-quick save " . escapeshellarg(WG_INTERFACE) . " 2>&1");

// ──────────────────────────────────────────────
// Actualizar / insertar en MySQL
// ──────────────────────────────────────────────
$now = date('Y-m-d H:i:s');

if ($machine) {
    $upd = $pdo->prepare("
        UPDATE vending_machines
        SET wg_public_key     = ?,
            wireguard_ip      = ?,
            wg_registered_at  = COALESCE(wg_registered_at, ?),
            device_ext_no     = COALESCE(NULLIF(?, ''), device_ext_no),
            device_name       = COALESCE(NULLIF(?, ''), device_name),
            last_seen         = NOW(),
            status            = 1
        WHERE device_no = ?
    ");
    $upd->execute([$publicKey, $assignedIp, $now, $deviceExtNo, $deviceName, $deviceNo]);
} else {
    // Primera vez que se ve esta máquina → crear registro
    $ins = $pdo->prepare("
        INSERT INTO vending_machines
            (device_no, device_ext_no, device_name, device_type, public_ip, status,
             wireguard_ip, wg_public_key, wg_registered_at)
        VALUES (?, ?, ?, 'Gateway', '', 1, ?, ?, ?)
    ");
    $ins->execute([$deviceNo, $deviceExtNo ?: $deviceNo, $deviceName ?: 'Máquina nueva', $assignedIp, $publicKey, $now]);
    error_log("[WG] Nueva máquina registrada: device_no={$deviceNo} ip={$assignedIp}");
}

// ──────────────────────────────────────────────
// Respuesta a la app Android
// ──────────────────────────────────────────────
echo json_encode([
    'wireguard_ip'      => $assignedIp,          // e.g. "10.99.0.5/32"
    'server_public_key' => WG_SERVER_PUBKEY,
    'server_endpoint'   => WG_SERVER_ENDPOINT,   // e.g. "vpn.powerboxchile.cl:51820"
    'dns'               => WG_DNS,
]);
