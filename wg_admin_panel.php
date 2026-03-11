<?php
/**
 * Powerbox WireGuard Admin Panel
 * Genera y administra configuraciones WireGuard para PC, Android e iOS.
 * Incluye escritorio remoto vía WebSocket/JSMpeg para máquinas vending.
 *
 * URL: /gateway-api/wg_admin_panel.php
 * Contraseña por defecto: Powerbox@2026
 */
// ─── Configuración ────────────────────────────────────────────────────────────
define('PANEL_PASSWORD',     'Powerbox@2026');
define('DB_HOST',            'localhost');
define('DB_NAME',            'powerboxchile_ips');
define('DB_USER',            'powerboxchile_ips');
define('DB_PASS',            '@Playstation9875!');
define('DB_PORT',            3306);
define('WG_SERVER_PUBKEY',   'Qy1nQI/K6kuZkR2FWvJfwwwiELXa2WSkWvy+wNh10Fw=');
define('WG_SERVER_ENDPOINT', 'vpn.powerboxchile.cl:51820');
define('WG_DNS',             '1.1.1.1');
define('WG_SUBNET_BASE',     '10.99.0');
define('WG_SUBNET_START',    100);
define('WG_SUBNET_END',      254);
define('SCRCPY_WSS',         'wss://vpn.powerboxchile.cl/scrcpy-ws/');
// ─────────────────────────────────────────────────────────────────────────────
session_start();
// ─── API JSON ─────────────────────────────────────────────────────────────────
$api = $_GET['api'] ?? '';
if ($api) {
    header('Content-Type: application/json; charset=utf-8');
    requireAuth();
    $pdo = getDb();
    switch ($api) {
        case 'list_peers':   apiListPeers($pdo);   break;
        case 'next_ip':      apiNextIp($pdo);      break;
        case 'create_peer':
            if ($_SERVER['REQUEST_METHOD'] === 'POST') apiCreatePeer($pdo);
            break;
        case 'delete_peer':
            if ($_SERVER['REQUEST_METHOD'] === 'POST') apiDeletePeer($pdo);
            break;
        default:
            http_response_code(400);
            echo json_encode(['ok' => false, 'error' => 'Unknown endpoint']);
    }
    exit;
}
// ─── Auth ─────────────────────────────────────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['password'])) {
    if ($_POST['password'] === PANEL_PASSWORD) {
        $_SESSION['wg_auth'] = true;
    } else {
        $loginError = 'Contraseña incorrecta';
    }
}
if (isset($_POST['logout'])) {
    session_destroy();
    header('Location: ' . $_SERVER['PHP_SELF']);
    exit;
}
// ─── Helpers ──────────────────────────────────────────────────────────────────
function requireAuth(): void {
    if (empty($_SESSION['wg_auth'])) {
        http_response_code(401);
        echo json_encode(['ok' => false, 'error' => 'Not authenticated']);
        exit;
    }
}
function getDb(): PDO {
    $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', DB_HOST, DB_PORT, DB_NAME);
    return new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
}
function initManualPeersTable(PDO $pdo): void {
    $pdo->exec("CREATE TABLE IF NOT EXISTS `wg_manual_peers` (
        `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT,
        `label`          VARCHAR(255) NOT NULL COMMENT 'Nombre descriptivo del cliente',
        `peer_type`      VARCHAR(20)  NOT NULL DEFAULT 'pc' COMMENT 'pc|android|ios|other',
        `wireguard_ip`   VARCHAR(18)  NOT NULL COMMENT 'IP asignada en el túnel',
        `wg_public_key`  VARCHAR(64)  NOT NULL COMMENT 'Clave pública del cliente',
        `wg_peer_active` TINYINT      NOT NULL DEFAULT 0 COMMENT '1=sincronizado en el VPS',
        `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (`id`),
        UNIQUE KEY `uq_ip`     (`wireguard_ip`),
        UNIQUE KEY `uq_pubkey` (`wg_public_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
}
function apiListPeers(PDO $pdo): void {
    $vending = $pdo->query("
        SELECT
            CONCAT('vm_', id) AS uid,
            'vending'         AS peer_type,
            CONCAT(device_ext_no, ' — ', device_name) AS label,
            wireguard_ip,
            wg_public_key,
            wg_peer_active,
            wg_registered_at  AS created_at
        FROM vending_machines
        WHERE wg_public_key IS NOT NULL
        ORDER BY device_ext_no
    ")->fetchAll();
    initManualPeersTable($pdo);
    $manual = $pdo->query("
        SELECT
            CONCAT('mp_', id) AS uid,
            id,
            peer_type,
            label,
            wireguard_ip,
            wg_public_key,
            wg_peer_active,
            created_at
        FROM wg_manual_peers
        ORDER BY created_at DESC
    ")->fetchAll();
    echo json_encode(['ok' => true, 'vending' => $vending, 'manual' => $manual]);
}
function apiNextIp(PDO $pdo): void {
    $usedVm = $pdo->query(
        "SELECT wireguard_ip FROM vending_machines WHERE wireguard_ip IS NOT NULL"
    )->fetchAll(PDO::FETCH_COLUMN);
    initManualPeersTable($pdo);
    $usedMp = $pdo->query(
        "SELECT wireguard_ip FROM wg_manual_peers"
    )->fetchAll(PDO::FETCH_COLUMN);
    $used = array_merge($usedVm, $usedMp);
    for ($i = WG_SUBNET_START; $i <= WG_SUBNET_END; $i++) {
        $candidate = WG_SUBNET_BASE . '.' . $i . '/32';
        if (!in_array($candidate, $used)) {
            echo json_encode(['ok' => true, 'ip' => $candidate]);
            return;
        }
    }
    http_response_code(503);
    echo json_encode(['ok' => false, 'error' => 'Sin IPs disponibles en el pool WireGuard']);
}
function apiCreatePeer(PDO $pdo): void {
    $data      = json_decode(file_get_contents('php://input'), true) ?? [];
    $label     = trim($data['label']      ?? '');
    $type      = trim($data['type']       ?? 'pc');
    $publicKey = trim($data['public_key'] ?? '');
    $ip        = trim($data['ip']         ?? '');
    if (!$label || !$publicKey || !$ip) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'Faltan campos: label, public_key, ip']);
        return;
    }
    if (!preg_match('/^[A-Za-z0-9+\/]{43}=$/', $publicKey)) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'Clave pública WireGuard inválida (debe ser base64 de 44 chars)']);
        return;
    }
    if (!preg_match('/^10\.99\.0\.(\d+)\/32$/', $ip, $m) || (int)$m[1] < 2 || (int)$m[1] > 254) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'IP fuera del rango permitido (10.99.0.2–254/32)']);
        return;
    }
    initManualPeersTable($pdo);
    try {
        $stmt = $pdo->prepare("
            INSERT INTO wg_manual_peers (label, peer_type, wireguard_ip, wg_public_key, wg_peer_active)
            VALUES (?, ?, ?, ?, 0)
        ");
        $stmt->execute([$label, $type, $ip, $publicKey]);
        echo json_encode(['ok' => true, 'id' => $pdo->lastInsertId()]);
    } catch (PDOException $e) {
        http_response_code(409);
        echo json_encode(['ok' => false, 'error' => 'IP o clave pública ya registrada']);
    }
}
function apiDeletePeer(PDO $pdo): void {
    $data = json_decode(file_get_contents('php://input'), true) ?? [];
    $id   = intval($data['id'] ?? 0);
    if (!$id) {
        http_response_code(400);
        echo json_encode(['ok' => false, 'error' => 'ID inválido']);
        return;
    }
    initManualPeersTable($pdo);
    $stmt = $pdo->prepare("DELETE FROM wg_manual_peers WHERE id = ?");
    $stmt->execute([$id]);
    echo json_encode(['ok' => true, 'deleted' => $stmt->rowCount()]);
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Powerbox — Panel Cuentas Acceso Remoto</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<style>
  body { background: #f0f2f5; font-family: 'Segoe UI', sans-serif; }
  .navbar-brand { font-weight: 700; letter-spacing: 1px; }
  .card { border: none; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,.08); }
  .badge-active   { background: #198754; }
  .badge-pending  { background: #fd7e14; }
  .badge-vending  { background: #0d6efd; }
  .badge-pc       { background: #6f42c1; }
  .badge-android  { background: #3ddc84; color: #000; }
  .badge-ios      { background: #555; }
  .badge-other    { background: #6c757d; }
  .conf-box { font-family: monospace; font-size: 12px; background: #1e1e2e; color: #cdd6f4;
              border-radius: 8px; padding: 14px; white-space: pre; overflow-x: auto; }
  .table th { font-size: 12px; text-transform: uppercase; color: #6c757d; }
  .table td { vertical-align: middle; }
  #loginCard { max-width: 380px; margin: 120px auto; }
  .ip-badge { font-family: monospace; font-size: 12px; }
  .pubkey-short { font-family: monospace; font-size: 11px; color: #6c757d; }
  .spinner-overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,.4);
                     z-index:9999; align-items:center; justify-content:center; }
  .spinner-overlay.show { display:flex; }

  /* ── Escritorio Remoto ── */
  #remoteCanvas {
    display: block;
    background: #000;
    max-width: 100%;
    max-height: calc(100vh - 130px);
    cursor: crosshair;
    touch-action: none;
  }
  #modalRemote .modal-content { background: #0f1117; }
  #modalRemote .modal-header  { border-bottom: 1px solid #2a2d3a; }
  #modalRemote .modal-body    { padding: 0; display:flex; align-items:center; justify-content:center; background:#000; }
  .remote-controls {
    position: absolute;
    bottom: 14px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 8px;
    z-index: 10;
  }
</style>
</head>
<body>
<?php if (empty($_SESSION['wg_auth'])): ?>
<!-- ═══════════════════════════════ LOGIN ═══════════════════════════════════ -->
<div class="container">
  <div id="loginCard" class="card p-4">
    <div class="text-center mb-4">
      <h5 class="fw-bold">🔐 Powerbox Acceso Intranet</h5>
      <small class="text-muted">Panel Creador de Cuentas</small>
    </div>
    <?php if (!empty($loginError)): ?>
      <div class="alert alert-danger py-2"><?= htmlspecialchars($loginError) ?></div>
    <?php endif; ?>
    <form method="post">
      <div class="mb-3">
        <label class="form-label fw-semibold">Contraseña</label>
        <input type="password" name="password" class="form-control" autofocus required>
      </div>
      <button type="submit" class="btn btn-primary w-100">Ingresar</button>
    </form>
  </div>
</div>
<?php else: ?>
<!-- ══════════════════════════════ PANEL ════════════════════════════════════ -->
<!-- Navbar -->
<nav class="navbar navbar-dark bg-dark px-3">
  <span class="navbar-brand"><img src="https://powerboxchile.cl/wp-content/uploads/2025/03/Logo_wordpress.png" alt="Logo" style="max-width: 15%; height: auto;"> Vending Machine Network </span>
  <div class="d-flex align-items-center gap-3">
    <small class="text-light opacity-75">VPS: 45.225.92.2</small>
    <form method="post" class="m-0">
      <button name="logout" class="btn btn-outline-light btn-sm">
        <i class="bi bi-box-arrow-right"></i> Salir
      </button>
    </form>
  </div>
</nav>
<div class="container-fluid py-4 px-4">
  <!-- Info cards -->
  <div class="row g-3 mb-4">
    <div class="col-md-4">
      <div class="card p-3">
        <div class="d-flex align-items-center gap-3">
          <div class="fs-2 text-primary"><i class="bi bi-server"></i></div>
          <div>
            <div class="fw-bold">Servidor Intranet</div>
            <small class="text-muted font-monospace">vpn.powerboxchile.cl:51820</small><br>
            <small class="text-muted">IP: 45.225.92.2</small>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card p-3">
        <div class="d-flex align-items-center gap-3">
          <div class="fs-2 text-success"><i class="bi bi-key-fill"></i></div>
          <div>
            <div class="fw-bold">Clave Pública Servidor</div>
            <small class="font-monospace text-muted" style="font-size:10px;word-break:break-all">
              <?= htmlspecialchars(WG_SERVER_PUBKEY) ?>
            </small>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card p-3">
        <div class="d-flex align-items-center gap-3">
          <div class="fs-2 text-warning"><i class="bi bi-hdd-network"></i></div>
          <div>
            <div class="fw-bold">Subred del Túnel</div>
            <small class="text-muted">10.99.0.0/24 · DNS: <?= WG_DNS ?></small><br>
            <small class="text-muted">Vending: 10.99.0.2–99 · Clientes: 10.99.0.100–254</small>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Botón nuevo peer -->
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="fw-bold mb-0"><i class="bi bi-people-fill me-2"></i>Peers registrados</h5>
    <button class="btn btn-success" onclick="openNewPeerModal()">
      <i class="bi bi-plus-circle me-1"></i> Nuevo peer
    </button>
  </div>
  <!-- Tabla peers manuales -->
  <div class="card mb-4">
    <div class="card-header bg-white fw-semibold py-2">
      <i class="bi bi-laptop me-1"></i> Clientes manuales (PC · Android · iOS)
    </div>
    <div class="card-body p-0">
      <table class="table table-hover mb-0" id="tblManual">
        <thead class="table-light">
          <tr>
            <th class="ps-3">Tipo</th>
            <th>Nombre</th>
            <th>IP Túnel</th>
            <th>Clave Pública</th>
            <th>Estado</th>
            <th>Creado</th>
            <th class="text-end pe-3">Acciones</th>
          </tr>
        </thead>
        <tbody id="bodyManual">
          <tr><td colspan="7" class="text-center py-3 text-muted">Cargando…</td></tr>
        </tbody>
      </table>
    </div>
  </div>
  <!-- Tabla vending machines -->
  <div class="card">
    <div class="card-header bg-white fw-semibold py-2">
      <i class="bi bi-display me-1"></i> Máquinas Vending (registradas automáticamente)
    </div>
    <div class="card-body p-0">
      <table class="table table-hover mb-0" id="tblVending">
        <thead class="table-light">
          <tr>
            <th class="ps-3">Dispositivo</th>
            <th>IP Túnel</th>
            <th>Clave Pública</th>
            <th>Estado</th>
            <th class="text-end pe-3">Acciones</th>
          </tr>
        </thead>
        <tbody id="bodyVending">
          <tr><td colspan="5" class="text-center py-3 text-muted">Cargando…</td></tr>
        </tbody>
      </table>
    </div>
  </div>
  <!-- Descargas: WireGuard -->
  <div class="card mt-4">
    <div class="card-header bg-white fw-semibold py-2">
      <i class="bi bi-shield-lock me-1 text-primary"></i> Descargar WireGuard
    </div>
    <div class="card-body p-0">
      <table class="table table-hover mb-0">
        <thead class="table-light">
          <tr>
            <th class="ps-3" style="width:20%">Plataforma</th>
            <th style="width:55%">Descripción</th>
            <th class="text-end pe-3" style="width:25%">Descarga</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="ps-3"><i class="bi bi-windows me-1 text-primary"></i> <strong>Windows</strong></td>
            <td><small class="text-muted">Instalador oficial para Windows 7/8/10/11</small></td>
            <td class="text-end pe-3">
              <a href="https://download.wireguard.com/windows-client/wireguard-installer.exe"
                 class="btn btn-sm btn-outline-primary" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar .exe
              </a>
            </td>
          </tr>
          <tr>
            <td class="ps-3"><i class="bi bi-android2 me-1 text-success"></i> <strong>Android</strong></td>
            <td><small class="text-muted">Aplicación oficial desde Google Play Store</small></td>
            <td class="text-end pe-3">
              <a href="https://play.google.com/store/apps/details?id=com.wireguard.android"
                 class="btn btn-sm btn-outline-success" target="_blank">
                <i class="bi bi-google-play me-1"></i> Play Store
              </a>
            </td>
          </tr>
          <tr>
            <td class="ps-3"><i class="bi bi-apple me-1"></i> <strong>iPhone / iPad / macOS</strong></td>
            <td><small class="text-muted">Aplicación oficial desde Apple App Store</small></td>
            <td class="text-end pe-3">
              <a href="https://apps.apple.com/app/wireguard/id1441195209"
                 class="btn btn-sm btn-outline-secondary" target="_blank">
                <i class="bi bi-apple me-1"></i> App Store
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <!-- Descargas: scrcpy -->
  <div class="card mt-4 mb-4">
    <div class="card-header bg-white fw-semibold py-2">
      <i class="bi bi-phone me-1 text-success"></i> Descargar scrcpy
    </div>
    <div class="card-body p-0">
      <table class="table table-hover mb-0">
        <thead class="table-light">
          <tr>
            <th class="ps-3" style="width:20%">Plataforma</th>
            <th style="width:55%">Descripción</th>
            <th class="text-end pe-3" style="width:25%">Descarga</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="ps-3"><i class="bi bi-windows me-1 text-primary"></i> <strong>Windows</strong></td>
            <td><small class="text-muted">Control remoto de pantalla Android desde PC (sin root)</small></td>
            <td class="text-end pe-3">
              <a href="https://github.com/Genymobile/scrcpy/releases/download/v3.3.4/scrcpy-win32-v3.3.4.zip"
                 class="btn btn-sm btn-outline-primary" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar x32
              </a>&nbsp;&nbsp;<a href="https://github.com/Genymobile/scrcpy/releases/download/v3.3.4/scrcpy-win64-v3.3.4.zip"
                 class="btn btn-sm btn-outline-primary" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar x64
              </a>
            </td>
          </tr>
          <tr>
            <td class="ps-3"><i class="bi bi-terminal me-1 text-warning"></i> <strong>macOS</strong></td>
            <td><small class="text-muted">Disponible en GitHub releases o via Homebrew (<code>brew install scrcpy</code>)</small></td>
            <td class="text-end pe-3">
              <a href="https://github.com/Genymobile/scrcpy/releases/download/v3.3.4/scrcpy-macos-x86_64-v3.3.4.tar.gz"
                 class="btn btn-sm btn-outline-secondary" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar x86/x64
              </a>&nbsp;&nbsp;<a href="https://github.com/Genymobile/scrcpy/releases/download/v3.3.4/scrcpy-macos-aarch64-v3.3.4.tar.gz"
                 class="btn btn-sm btn-outline-secondary" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar Aarch64
              </a>
            </td>
          </tr>
          <tr>
            <td class="ps-3"><i class="bi bi-ubuntu me-1 text-danger"></i> <strong>Linux</strong></td>
            <td><small class="text-muted">Disponible en GitHub releases o via gestor de paquetes (<code>apt install scrcpy</code>)</small></td>
            <td class="text-end pe-3">
              <a href="https://github.com/Genymobile/scrcpy/releases/download/v3.3.4/scrcpy-linux-x86_64-v3.3.4.tar.gz"
                 class="btn btn-sm btn-outline-danger" target="_blank">
                <i class="bi bi-download me-1"></i> Descargar x86/x64
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</div><!-- /container -->

<!-- ══════════════════ MODAL: Nuevo peer ════════════════════════════════════ -->
<div class="modal fade" id="modalNewPeer" tabindex="-1">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header bg-success text-white">
        <h5 class="modal-title"><i class="bi bi-plus-circle me-2"></i>Nuevo peer WireGuard</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <!-- Paso 1: datos -->
        <div id="step1">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">Nombre / Descripción</label>
              <input type="text" id="peerLabel" class="form-control" placeholder="Ej: Jorge PC Casa">
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold">Tipo de dispositivo</label>
              <select id="peerType" class="form-select">
                <option value="pc">💻 PC / Laptop</option>
                <option value="android">🤖 Android</option>
                <option value="ios">🍎 iPhone / iPad</option>
                <option value="other">🔧 Otro</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold">Modo de red</label>
              <select id="peerAllowedIps" class="form-select">
                <option value="10.99.0.0/24">Solo VPN (10.99.0.x)</option>
                <option value="0.0.0.0/0, ::/0">Túnel completo</option>
              </select>
            </div>
          </div>
          <div class="alert alert-info mt-3 py-2 mb-0">
            <i class="bi bi-info-circle me-1"></i>
            Las claves se generan <strong>localmente en tu navegador</strong>. La clave privada
            <strong>nunca se envía al servidor</strong>. Solo la clave pública y la IP se guardan en la BD.
          </div>
        </div>
        <!-- Paso 2: config generada -->
        <div id="step2" class="d-none">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">IP asignada</label>
              <input type="text" id="showIp" class="form-control font-monospace" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Clave pública (guardada en servidor)</label>
              <input type="text" id="showPubKey" class="form-control font-monospace" readonly style="font-size:11px">
            </div>
          </div>
          <div class="mt-3">
            <label class="form-label fw-semibold">Archivo de configuración (.conf)</label>
            <div id="confPreview" class="conf-box mt-1"></div>
          </div>
          <div class="alert alert-warning mt-3 py-2 mb-0">
            <i class="bi bi-exclamation-triangle me-1"></i>
            <strong>Guarda este archivo ahora.</strong> La clave privada no se vuelve a mostrar.
            El peer estará activo en el servidor en menos de 60 segundos.
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button id="btnGenerate" class="btn btn-primary" onclick="generatePeer()">
          <i class="bi bi-cpu me-1"></i> Generar configuración
        </button>
        <button id="btnDownload" class="btn btn-success d-none" onclick="downloadConf()">
          <i class="bi bi-download me-1"></i> Descargar .conf
        </button>
        <button id="btnSave" class="btn btn-primary d-none" onclick="savePeer()">
          <i class="bi bi-cloud-upload me-1"></i> Guardar en servidor
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ══════════════════ MODAL: Ver config vending ════════════════════════════ -->
<div class="modal fade" id="modalViewConf" tabindex="-1">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="bi bi-file-code me-2"></i>Config WireGuard — <span id="confTitle"></span></h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-warning py-2">
          <i class="bi bi-exclamation-triangle me-1"></i>
          Esta config <strong>no incluye la clave privada</strong> (solo la conoce el dispositivo).
          Usa esta vista solo como referencia o para copiar la IP y endpoint.
        </div>
        <div id="viewConfPreview" class="conf-box"></div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
        <button class="btn btn-primary" onclick="downloadViewConf()">
          <i class="bi bi-download me-1"></i> Descargar (sin privada)
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ══════════════════ MODAL: Escritorio Remoto ════════════════════════════ -->
<div class="modal fade" id="modalRemote" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
  <div class="modal-dialog modal-xl modal-dialog-centered">
    <div class="modal-content" style="background:#0f1117;border:1px solid #2a2d3a;">
      <div class="modal-header py-2" style="border-bottom:1px solid #2a2d3a;">
        <h6 class="modal-title text-white mb-0" id="remoteTitle">
          <i class="bi bi-display me-2 text-info"></i>Escritorio Remoto
        </h6>
        <div class="d-flex align-items-center gap-2 ms-3">
          <span id="remoteStatus" class="badge bg-warning">Conectando…</span>
        </div>
        <button type="button" class="btn-close btn-close-white ms-auto" onclick="closeRemoteDesktop()"></button>
      </div>
      <div class="modal-body p-0 position-relative" style="background:#000;min-height:400px;display:flex;align-items:center;justify-content:center;">
        <canvas id="remoteCanvas"></canvas>
        <!-- Botones ADB -->
        <div class="remote-controls">
          <button class="btn btn-sm btn-outline-light" onclick="sendKey(4)" title="Atrás">
            <i class="bi bi-arrow-left"></i>
          </button>
          <button class="btn btn-sm btn-outline-light" onclick="sendKey(3)" title="Home">
            <i class="bi bi-house"></i>
          </button>
          <button class="btn btn-sm btn-outline-light" onclick="sendKey(187)" title="Recientes">
            <i class="bi bi-grid-3x3-gap"></i>
          </button>
          <button class="btn btn-sm btn-outline-secondary" onclick="sendKey(26)" title="Encender/Apagar pantalla">
            <i class="bi bi-power"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Spinner overlay -->
<div class="spinner-overlay" id="spinner">
  <div class="text-center text-white">
    <div class="spinner-border mb-2" style="width:3rem;height:3rem"></div>
    <div>Procesando…</div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<!-- TweetNaCl — genera claves Curve25519 (X25519) compatibles con WireGuard -->
<script src="https://cdn.jsdelivr.net/npm/tweetnacl@1.0.3/nacl.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/tweetnacl-util@0.15.1/nacl-util.min.js"></script>
<script>
// ─── Constantes servidor ──────────────────────────────────────────────────────
const WG_SERVER_PUBKEY   = <?= json_encode(WG_SERVER_PUBKEY) ?>;
const WG_SERVER_ENDPOINT = <?= json_encode(WG_SERVER_ENDPOINT) ?>;
const WG_DNS             = <?= json_encode(WG_DNS) ?>;
const SCRCPY_WSS         = <?= json_encode(SCRCPY_WSS) ?>;

// Estado del peer actual
let currentPeer = null;
let viewConfData = null;

// ─── Inicialización ───────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', loadPeers);

// ─── Cargar tabla ─────────────────────────────────────────────────────────────
async function loadPeers() {
  try {
    const r = await fetch('?api=list_peers');
    const d = await r.json();
    if (!d.ok) return;
    renderManual(d.manual);
    renderVending(d.vending);
  } catch(e) {
    console.error(e);
  }
}

function peerTypeBadge(t) {
  const map = {
    vending: '<span class="badge badge-vending">Vending</span>',
    pc:      '<span class="badge badge-pc text-white">💻 PC</span>',
    android: '<span class="badge badge-android">🤖 Android</span>',
    ios:     '<span class="badge badge-ios text-white">🍎 iOS</span>',
    other:   '<span class="badge badge-other text-white">🔧 Otro</span>',
  };
  return map[t] || map.other;
}

function statusBadge(active) {
  return active
    ? '<span class="badge badge-active">✅ Activo</span>'
    : '<span class="badge badge-pending">⏳ Pendiente</span>';
}

function shortKey(k) {
  if (!k) return '—';
  return `<span class="pubkey-short" title="${k}">${k.substring(0,12)}…${k.slice(-6)}</span>`;
}

function renderManual(peers) {
  const tbody = document.getElementById('bodyManual');
  if (!peers.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Sin peers manuales todavía</td></tr>';
    return;
  }
  tbody.innerHTML = peers.map(p => `
    <tr>
      <td class="ps-3">${peerTypeBadge(p.peer_type)}</td>
      <td class="fw-semibold">${escHtml(p.label)}</td>
      <td><span class="badge bg-secondary ip-badge">${escHtml(p.wireguard_ip)}</span></td>
      <td>${shortKey(p.wg_public_key)}</td>
      <td>${statusBadge(p.wg_peer_active == 1)}</td>
      <td><small class="text-muted">${escHtml(p.created_at || '—')}</small></td>
      <td class="text-end pe-3">
        <button class="btn btn-sm btn-outline-danger" onclick="deletePeer(${p.id}, '${escHtml(p.label)}')">
          <i class="bi bi-trash"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

function renderVending(peers) {
  const tbody = document.getElementById('bodyVending');
  if (!peers.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Sin máquinas vending con WireGuard</td></tr>';
    return;
  }
  tbody.innerHTML = peers.map(p => `
    <tr>
      <td class="ps-3 fw-semibold">${escHtml(p.label)}</td>
      <td><span class="badge bg-secondary ip-badge">${escHtml(p.wireguard_ip || '—')}</span></td>
      <td>${shortKey(p.wg_public_key)}</td>
      <td>${statusBadge(p.wg_peer_active == 1)}</td>
      <td class="text-end pe-3">
        <button class="btn btn-sm btn-outline-info me-1"
                onclick="openRemoteDesktop('${escHtml(p.wireguard_ip || '')}', '${escHtml(p.label)}')"
                title="Ver pantalla en tiempo real">
          <i class="bi bi-display me-1"></i>Pantalla
        </button>
        <button class="btn btn-sm btn-outline-secondary"
                onclick="viewVendingConf(JSON.parse(this.dataset.peer))"
                data-peer="${escHtml(JSON.stringify(p))}">
          <i class="bi bi-eye me-1"></i>Config
        </button>
      </td>
    </tr>
  `).join('');
}

// ─── Generar keypair en el navegador ─────────────────────────────────────────
function genKeypair() {
  const kp = nacl.box.keyPair();
  const toB64 = b => btoa(String.fromCharCode(...b));
  return { private: toB64(kp.secretKey), public: toB64(kp.publicKey) };
}

// ─── Construir archivo .conf ──────────────────────────────────────────────────
function buildConf(label, privateKey, ip, allowedIps) {
  const ipClean = ip.replace('/32', '');
  return `[Interface]
# ${label}
PrivateKey = ${privateKey}
Address = ${ipClean}/32
DNS = ${WG_DNS}
[Peer]
PublicKey = ${WG_SERVER_PUBKEY}
Endpoint = ${WG_SERVER_ENDPOINT}
AllowedIPs = ${allowedIps}
PersistentKeepalive = 25`;
}

// ─── Abrir modal nuevo peer ───────────────────────────────────────────────────
function openNewPeerModal() {
  currentPeer = null;
  document.getElementById('peerLabel').value = '';
  document.getElementById('peerType').value  = 'pc';
  document.getElementById('peerAllowedIps').value = '10.99.0.0/24';
  document.getElementById('step1').classList.remove('d-none');
  document.getElementById('step2').classList.add('d-none');
  document.getElementById('btnGenerate').classList.remove('d-none');
  document.getElementById('btnDownload').classList.add('d-none');
  document.getElementById('btnSave').classList.add('d-none');
  new bootstrap.Modal(document.getElementById('modalNewPeer')).show();
}

// ─── Paso 1: generar keypair + obtener IP ────────────────────────────────────
async function generatePeer() {
  const label = document.getElementById('peerLabel').value.trim();
  if (!label) { alert('Ingresa un nombre para el peer'); return; }
  showSpinner(true);
  try {
    const ipResp = await fetch('?api=next_ip');
    const ipData = await ipResp.json();
    if (!ipData.ok) { alert('Error: ' + ipData.error); return; }
    const kp         = genKeypair();
    const allowedIps = document.getElementById('peerAllowedIps').value;
    const conf       = buildConf(label, kp.private, ipData.ip, allowedIps);
    currentPeer = { label, type: document.getElementById('peerType').value, kp, ip: ipData.ip, conf };
    document.getElementById('showIp').value     = ipData.ip;
    document.getElementById('showPubKey').value = kp.public;
    document.getElementById('confPreview').textContent = conf;
    document.getElementById('step1').classList.add('d-none');
    document.getElementById('step2').classList.remove('d-none');
    document.getElementById('btnGenerate').classList.add('d-none');
    document.getElementById('btnDownload').classList.remove('d-none');
    document.getElementById('btnSave').classList.remove('d-none');
  } catch(e) {
    alert('Error al obtener IP: ' + e.message);
  } finally {
    showSpinner(false);
  }
}

// ─── Descargar .conf ──────────────────────────────────────────────────────────
function downloadConf() {
  if (!currentPeer) return;
  const filename = currentPeer.label.replace(/[^a-zA-Z0-9_\-]/g, '_') + '_wg.conf';
  triggerDownload(currentPeer.conf, filename);
}

// ─── Guardar peer en servidor ─────────────────────────────────────────────────
async function savePeer() {
  if (!currentPeer) return;
  showSpinner(true);
  try {
    const r = await fetch('?api=create_peer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        label:      currentPeer.label,
        type:       currentPeer.type,
        public_key: currentPeer.kp.public,
        ip:         currentPeer.ip,
      })
    });
    const d = await r.json();
    if (d.ok) {
      bootstrap.Modal.getInstance(document.getElementById('modalNewPeer')).hide();
      await loadPeers();
      showToast('✅ Peer guardado. Activo en el servidor en ~60 s');
    } else {
      alert('Error: ' + d.error);
    }
  } catch(e) {
    alert('Error de red: ' + e.message);
  } finally {
    showSpinner(false);
  }
}

// ─── Eliminar peer ────────────────────────────────────────────────────────────
async function deletePeer(id, label) {
  if (!confirm(`¿Eliminar el peer "${label}"?\nEl peer seguirá en el servidor WireGuard hasta el próximo reinicio.`)) return;
  showSpinner(true);
  try {
    const r = await fetch('?api=delete_peer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id })
    });
    const d = await r.json();
    if (d.ok) { await loadPeers(); showToast('Peer eliminado de la BD'); }
    else alert('Error: ' + d.error);
  } finally {
    showSpinner(false);
  }
}

// ─── Ver config vending (sin clave privada) ───────────────────────────────────
function viewVendingConf(peer) {
  const ipClean = (peer.wireguard_ip || '').replace('/32', '');
  const conf = `[Interface]
# ${peer.label}
PrivateKey = <PRIVADA_DEL_DISPOSITIVO>
Address = ${ipClean}/32
DNS = ${WG_DNS}
[Peer]
PublicKey = ${WG_SERVER_PUBKEY}
Endpoint = ${WG_SERVER_ENDPOINT}
AllowedIPs = 10.99.0.0/24
PersistentKeepalive = 25`;
  document.getElementById('confTitle').textContent = peer.label;
  document.getElementById('viewConfPreview').textContent = conf;
  viewConfData = { conf, label: peer.label };
  new bootstrap.Modal(document.getElementById('modalViewConf')).show();
}

function downloadViewConf() {
  if (!viewConfData) return;
  const filename = viewConfData.label.replace(/[^a-zA-Z0-9_\-]/g, '_') + '_ref.conf';
  triggerDownload(viewConfData.conf, filename);
}

// ─── Escritorio Remoto (JSMpeg + WebSocket) ───────────────────────────────────
let remotePlayer = null;   // instancia JSMpeg.Player
let remoteWs     = null;   // WebSocket para enviar input

async function openRemoteDesktop(wgIpRaw, label) {
  const deviceIp = wgIpRaw.replace('/32', '').trim();
  if (!deviceIp.match(/^10\.99\.0\.\d{1,3}$/)) {
    alert('IP de VPN no válida: ' + deviceIp);
    return;
  }

  // Mostrar modal
  document.getElementById('remoteTitle').innerHTML =
    '<i class="bi bi-display me-2 text-info"></i>' + escHtml(label) +
    ' <small class="text-muted font-monospace">' + deviceIp + '</small>';
  document.getElementById('remoteStatus').textContent = 'Conectando…';
  document.getElementById('remoteStatus').className   = 'badge bg-warning';
  new bootstrap.Modal(document.getElementById('modalRemote')).show();

  // Cerrar conexión previa si existe
  _destroyRemote();

  // Cargar JSMpeg dinámicamente la primera vez
  if (typeof JSMpeg === 'undefined') {
    try {
      await loadScript('https://cdn.jsdelivr.net/gh/phoboslab/jsmpeg/jsmpeg.min.js');
    } catch(e) {
      document.getElementById('remoteStatus').textContent = '❌ Error cargando JSMpeg';
      document.getElementById('remoteStatus').className   = 'badge bg-danger';
      return;
    }
  }

  const canvas = document.getElementById('remoteCanvas');
  const wsUrl  = SCRCPY_WSS + '?device=' + deviceIp;

  // JSMpeg crea su propio WebSocket internamente
  remotePlayer = new JSMpeg.Player(wsUrl, {
    canvas:    canvas,
    autoplay:  true,
    audio:     false,
    disableGl: true,
    onSourceEstablished: () => {
      document.getElementById('remoteStatus').textContent = '● En vivo';
      document.getElementById('remoteStatus').className   = 'badge bg-success';
      // Guardar referencia al WebSocket que JSMpeg creó para poder enviar input
      if (remotePlayer.source && remotePlayer.source.socket) {
        remoteWs = remotePlayer.source.socket;
        attachCanvasInput(canvas, remoteWs);
      }
    },
    onSourceCompleted: () => {
      document.getElementById('remoteStatus').textContent = '⏹ Stream finalizado';
      document.getElementById('remoteStatus').className   = 'badge bg-secondary';
    }
  });
}

function closeRemoteDesktop() {
  _destroyRemote();
  const m = bootstrap.Modal.getInstance(document.getElementById('modalRemote'));
  if (m) m.hide();
}

function _destroyRemote() {
  if (remotePlayer) { remotePlayer.destroy(); remotePlayer = null; }
  remoteWs = null;
  // Limpiar canvas
  const c = document.getElementById('remoteCanvas');
  const ctx = c.getContext('2d');
  ctx.clearRect(0, 0, c.width, c.height);
}

// ─── Enviar taps/swipes al dispositivo ───────────────────────────────────────
function attachCanvasInput(canvas, ws) {
  // Limpiar listeners previos clonando el canvas (truco limpio)
  const fresh = canvas.cloneNode(true);
  canvas.parentNode.replaceChild(fresh, canvas);

  let touchStart = null;

  // Click → tap
  fresh.addEventListener('click', (e) => {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    const { dx, dy } = canvasToDevice(fresh, e.clientX, e.clientY);
    ws.send(JSON.stringify({ type: 'tap', x: dx, y: dy }));
  });

  // Touch start → recordar posición inicial
  fresh.addEventListener('touchstart', (e) => {
    e.preventDefault();
    const t = e.touches[0];
    touchStart = { cx: t.clientX, cy: t.clientY };
  }, { passive: false });

  // Touch end → tap o swipe
  fresh.addEventListener('touchend', (e) => {
    e.preventDefault();
    if (!ws || ws.readyState !== WebSocket.OPEN || !touchStart) return;
    const t = e.changedTouches[0];
    const { dx: x2, dy: y2 } = canvasToDevice(fresh, t.clientX, t.clientY);
    const { dx: x1, dy: y1 } = canvasToDevice(fresh, touchStart.cx, touchStart.cy);
    const dist = Math.hypot(x2 - x1, y2 - y1);
    if (dist < 15) {
      ws.send(JSON.stringify({ type: 'tap', x: x1, y: y1 }));
    } else {
      ws.send(JSON.stringify({ type: 'swipe', x1, y1, x2, y2, duration: 300 }));
    }
    touchStart = null;
  }, { passive: false });
}

function canvasToDevice(canvas, clientX, clientY) {
  const r   = canvas.getBoundingClientRect();
  const scX = (canvas.width  || 720)  / r.width;
  const scY = (canvas.height || 1280) / r.height;
  return { dx: (clientX - r.left) * scX, dy: (clientY - r.top) * scY };
}

function sendKey(keycode) {
  if (remoteWs && remoteWs.readyState === WebSocket.OPEN) {
    remoteWs.send(JSON.stringify({ type: 'key', keycode }));
  }
}

// ─── Utilidades ───────────────────────────────────────────────────────────────
function loadScript(src) {
  return new Promise((res, rej) => {
    const s = document.createElement('script');
    s.src = src; s.onload = res; s.onerror = rej;
    document.head.appendChild(s);
  });
}

function triggerDownload(content, filename) {
  const a = document.createElement('a');
  a.href = 'data:text/plain;charset=utf-8,' + encodeURIComponent(content);
  a.download = filename;
  a.click();
}

function escHtml(s) {
  return String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function showSpinner(v) {
  document.getElementById('spinner').classList.toggle('show', v);
}

function showToast(msg) {
  const t = document.createElement('div');
  t.className = 'position-fixed bottom-0 end-0 m-3 alert alert-success shadow';
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 4000);
}
</script>
<?php endif; ?>
</body>
</html>
