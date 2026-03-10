<?php
/**
 * Powerbox Gateway — Pre-registro de máquina vending
 *
 * Ruta: /gateway-api/register_device.php
 *
 * Acepta POST con JSON:
 *   { "device_no", "device_ext_no", "device_name", "device_type", "public_ip", "status" }
 *
 * Comportamiento:
 *   - Si la máquina no existe  → INSERT en vending_machines
 *   - Si ya existe             → UPDATE device_name, device_type, public_ip, status, last_seen
 *
 * Este endpoint debe ejecutarse ANTES de register_wireguard.php,
 * ya que ese script requiere que la máquina exista en vending_machines.
 */
header('Content-Type: application/json; charset=utf-8');

// ─── Configuración BD ─────────────────────────────────────────────────────────
define('DB_HOST', 'localhost');
define('DB_NAME', 'powerboxchile_ips');
define('DB_USER', 'powerboxchile_ips');
define('DB_PASS', '@Playstation9875!');
define('DB_PORT', 3306);
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
$deviceType  = trim($data['device_type']  ?? '');
$publicIp    = trim($data['public_ip']    ?? '');
$status      = isset($data['status']) ? intval($data['status']) : 1;

if (!$deviceNo || !$deviceExtNo) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Missing required fields: device_no, device_ext_no']);
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

// ─── INSERT OR UPDATE (upsert) ────────────────────────────────────────────────
$stmt = $pdo->prepare("
    INSERT INTO vending_machines
        (device_no, device_ext_no, device_name, device_type, public_ip, status, last_seen, created_at)
    VALUES
        (:device_no, :device_ext_no, :device_name, :device_type, :public_ip, :status, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
        device_name  = VALUES(device_name),
        device_type  = VALUES(device_type),
        public_ip    = VALUES(public_ip),
        status       = VALUES(status),
        last_seen    = NOW()
");

$stmt->execute([
    ':device_no'     => $deviceNo,
    ':device_ext_no' => $deviceExtNo,
    ':device_name'   => $deviceName,
    ':device_type'   => $deviceType,
    ':public_ip'     => $publicIp,
    ':status'        => $status,
]);

$action = ($stmt->rowCount() === 1) ? 'inserted' : 'updated';

echo json_encode([
    'ok'     => true,
    'action' => $action,
]);
