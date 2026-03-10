<?php
/**
 * Powerbox Gateway — API proxy para sync WireGuard desde el VPS
 *
 * Ruta: /gateway-api/wg_sync_api.php
 *
 * El VPS no puede conectarse directamente al MySQL del hosting (puerto bloqueado).
 * Este script actúa como proxy HTTP: el VPS llama via curl y este PHP ejecuta
 * las queries localmente (localhost) devolviendo JSON.
 *
 * Endpoints:
 *   GET  ?action=pending              → lista peers con wg_peer_active=0
 *   POST action=activate  id=<int>    → marca wg_peer_active=1
 *
 * Seguridad: header X-WG-Secret debe coincidir con WG_API_SECRET
 */
header('Content-Type: application/json; charset=utf-8');

// ─── Configuración ────────────────────────────────────────────────────────────
define('DB_HOST',       'localhost');
define('DB_NAME',       'powerboxchile_ips');
define('DB_USER',       'powerboxchile_ips');
define('DB_PASS',       '@Playstation9875!');
define('DB_PORT',       3306);
define('WG_API_SECRET', 'pb_wg_s3cr3t_2026');   // mismo valor en el script bash del VPS
// ─────────────────────────────────────────────────────────────────────────────

// ─── Autenticación ────────────────────────────────────────────────────────────
$secret = $_SERVER['HTTP_X_WG_SECRET'] ?? '';
if ($secret !== WG_API_SECRET) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Forbidden']);
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

$action = $_GET['action'] ?? ($_POST['action'] ?? '');

// ─── GET pending: peers listos para activar ───────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'GET' && $action === 'pending') {
    $rows = $pdo->query("
        SELECT id, device_ext_no, wireguard_ip, wg_public_key
        FROM vending_machines
        WHERE wg_public_key  IS NOT NULL
          AND wireguard_ip   IS NOT NULL
          AND wg_peer_active = 0
    ")->fetchAll();

    echo json_encode(['ok' => true, 'peers' => $rows]);
    exit;
}

// ─── POST activate: marcar peer como activo ───────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && $action === 'activate') {
    $raw  = file_get_contents('php://input');
    $data = json_decode($raw, true);
    $id   = intval($data['id'] ?? 0);

    if (!$id) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'Missing id']);
        exit;
    }

    $stmt = $pdo->prepare("UPDATE vending_machines SET wg_peer_active = 1 WHERE id = ?");
    $stmt->execute([$id]);

    echo json_encode(['ok' => true, 'updated' => $stmt->rowCount()]);
    exit;
}

http_response_code(400);
echo json_encode(['ok' => false, 'error' => 'Unknown action']);
