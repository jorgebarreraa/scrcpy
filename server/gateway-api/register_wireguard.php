<?php
/**
 * Powerbox Gateway — Registro WireGuard de máquinas vending
 *
 * Ruta: /gateway-api/register_wireguard.php
 *
 * Acepta POST con JSON:
 *   { "device_no", "device_ext_no", "device_name", "public_key" }
 *
 * Comportamiento:
 *   - Si la máquina no tiene wireguard_ip → asigna la próxima libre del pool
 *   - Si ya tiene wireguard_ip           → actualiza solo la public_key (re-registro)
 *   - Retorna la config completa para que el Android levante el túnel
 *
 * Nota: este PHP NO ejecuta `wg set` (el hosting no tiene root).
 *       El VPS WireGuard lee MySQL cada minuto via sync_wireguard_peers.sh
 *       y agrega los peers automáticamente.
 */
header('Content-Type: application/json; charset=utf-8');

// ─── Configuración BD ─────────────────────────────────────────────────────────
define('DB_HOST', 'localhost');
define('DB_NAME', 'powerboxchile_ips');
define('DB_USER', 'powerboxchile_ips');
define('DB_PASS', '@Playstation9875!');
define('DB_PORT', 3306);

// ─── Config WireGuard (datos del VPS, no del hosting) ────────────────────────
define('WG_SERVER_PUBKEY',   'bUrYyrwojzalEtSwQVh1MdP7oQqBq6BOuDnNpYtQyUI=');
define('WG_SERVER_ENDPOINT', 'vpn.powerboxchile.cl:51820');
define('WG_DNS',             '1.1.1.1');
define('WG_SUBNET_BASE',     '10.99.0');   // rango separado de la LAN 10.0.0.x
define('WG_SUBNET_START',    2);
define('WG_SUBNET_END',      254);
// ─────────────────────────────────────────────────────────────────────────────

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['ok' => false, 'error' => 'Method not allowed']);
    exit;
}

$raw  = file_get_contents('php://input');
$data = json_decode($raw, true);
if (!$data) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Invalid JSON']);
    exit;
}

$deviceNo    = trim($data['device_no']    ?? '');
$deviceExtNo = trim($data['device_ext_no'] ?? '');
$deviceName  = trim($data['device_name']  ?? '');
$publicKey   = trim($data['public_key']   ?? '');

if (!$deviceNo || !$publicKey) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Missing required fields: device_no, public_key']);
    exit;
}

// Validar formato de clave pública WireGuard (base64, 44 chars)
if (!preg_match('/^[A-Za-z0-9+\/]{43}=$/', $publicKey)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'public_key invalida (debe ser base64 de 44 chars)']);
    exit;
}

// ─── Conexión BD ──────────────────────────────────────────────────────────────
try {
    $dsn = "mysql:host=" . DB_HOST . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=utf8mb4";
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
} catch (PDOException $e) {
    http_response_code(503);
    echo json_encode(['ok' => false, 'error' => 'DB connection failed']);
    exit;
}

// ─── Buscar máquina existente ─────────────────────────────────────────────────
$stmt = $pdo->prepare("SELECT id, wireguard_ip, wg_public_key FROM vending_machines WHERE device_no = ? LIMIT 1");
$stmt->execute([$deviceNo]);
$machine = $stmt->fetch();

if (!$machine) {
    http_response_code(404);
    echo json_encode(['ok' => false, 'error' => 'Maquina no encontrada. Ejecutar register_device.php primero.']);
    exit;
}

// ─── Asignar IP WireGuard ─────────────────────────────────────────────────────
if (!empty($machine['wireguard_ip'])) {
    // Ya tiene IP → reusar (permite re-registro con nueva clave)
    $assignedIp = $machine['wireguard_ip'];
    $action = 'updated';
} else {
    // Primera vez → asignar la próxima libre
    $used = $pdo->query("SELECT wireguard_ip FROM vending_machines WHERE wireguard_ip IS NOT NULL")
                ->fetchAll(PDO::FETCH_COLUMN);

    $assignedIp = null;
    for ($i = WG_SUBNET_START; $i <= WG_SUBNET_END; $i++) {
        $candidate = WG_SUBNET_BASE . '.' . $i . '/32';
        if (!in_array($candidate, $used)) {
            $assignedIp = $candidate;
            break;
        }
    }

    if (!$assignedIp) {
        http_response_code(503);
        echo json_encode(['ok' => false, 'error' => 'Sin IPs disponibles en el pool WireGuard']);
        exit;
    }
    $action = 'inserted';
}

// ─── Guardar en MySQL (el VPS leerá esto para ejecutar wg set) ────────────────
$upd = $pdo->prepare("
    UPDATE vending_machines
    SET wireguard_ip        = :wireguard_ip,
        wg_public_key       = :wg_public_key,
        wg_peer_active      = 0,
        wg_registered_at    = COALESCE(wg_registered_at, NOW()),
        last_seen           = NOW()
    WHERE device_no = :device_no
");
$upd->execute([
    ':wireguard_ip'  => $assignedIp,
    ':wg_public_key' => $publicKey,
    ':device_no'     => $deviceNo,
]);

// ─── Respuesta al Android ─────────────────────────────────────────────────────
echo json_encode([
    'ok'               => true,
    'action'           => $action,
    'wireguard_ip'     => $assignedIp,           // e.g. "10.99.0.2/32"
    'server_public_key'=> WG_SERVER_PUBKEY,
    'server_endpoint'  => WG_SERVER_ENDPOINT,    // "vpn.powerboxchile.cl:51820"
    'dns'              => WG_DNS,
    'note'             => 'El tunel estara activo en menos de 60 segundos (sync del VPS)',
]);
