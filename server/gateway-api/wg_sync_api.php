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

// Para POST con JSON body, leer el body una sola vez y cachear
$jsonBody = null;
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $jsonBody = json_decode(file_get_contents('php://input'), true) ?? [];
}

$action = $_GET['action'] ?? ($_POST['action'] ?? ($jsonBody['action'] ?? ''));

// ─── GET pending: peers listos para activar (vending + manuales) ─────────────
if ($_SERVER['REQUEST_METHOD'] === 'GET' && $action === 'pending') {
    // Peers de máquinas vending
    $rowsVending = $pdo->query("
        SELECT id, device_ext_no AS label, wireguard_ip, wg_public_key, 'vending' AS source
        FROM vending_machines
        WHERE wg_public_key  IS NOT NULL
          AND wireguard_ip   IS NOT NULL
          AND wg_peer_active = 0
    ")->fetchAll();

    // Peers manuales (si la tabla existe)
    $rowsManual = [];
    try {
        $rowsManual = $pdo->query("
            SELECT id, label, wireguard_ip, wg_public_key, 'manual' AS source
            FROM wg_manual_peers
            WHERE wg_public_key IS NOT NULL
              AND wireguard_ip  IS NOT NULL
              AND wg_peer_active = 0
        ")->fetchAll();
    } catch (PDOException $e) {
        // La tabla no existe todavía — ignorar
    }

    $peers = array_map(function($r) {
        return [
            'id'           => $r['id'],
            'source'       => $r['source'],
            'device_ext_no'=> $r['label'],
            'wireguard_ip' => $r['wireguard_ip'],
            'wg_public_key'=> $r['wg_public_key'],
        ];
    }, array_merge($rowsVending, $rowsManual));

    echo json_encode(['ok' => true, 'peers' => $peers]);
    exit;
}

// ─── POST activate: marcar peer como activo ───────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && $action === 'activate') {
    $data   = $jsonBody ?? [];
    $id     = intval($data['id']     ?? 0);
    $source = $data['source'] ?? 'vending';   // 'vending' | 'manual'

    if (!$id) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'Missing id']);
        exit;
    }

    if ($source === 'manual') {
        $stmt = $pdo->prepare("UPDATE wg_manual_peers SET wg_peer_active = 1 WHERE id = ?");
    } else {
        $stmt = $pdo->prepare("UPDATE vending_machines SET wg_peer_active = 1 WHERE id = ?");
    }
    $stmt->execute([$id]);

    echo json_encode(['ok' => true, 'updated' => $stmt->rowCount()]);
    exit;
}

http_response_code(400);
echo json_encode(['ok' => false, 'error' => 'Unknown action']);
