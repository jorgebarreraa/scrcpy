<?php
/**
 * Panel de Administración Powerbox – Escritorio Remoto Multi-Máquina
 * ------------------------------------------------------------------
 * Ubicación sugerida en webhosting:
 *   /public_html/gateway-api/wg_admin_panel.php
 *   (o donde ya tengas el panel existente)
 *
 * Requiere las mismas credenciales DB que register_wireguard.php
 */

// ─── Configuración ────────────────────────────────────────────────
define('DB_HOST', 'localhost');
define('DB_NAME', 'powerbox_db');    // cambia si es diferente
define('DB_USER', 'powerbox_user');  // cambia
define('DB_PASS', 'tu_password');    // cambia
define('VPS_WS_HOST', 'vpn.powerboxchile.cl'); // dominio del VPS

// ─── Obtener lista de máquinas conectadas vía WireGuard ──────────
$machines = [];
try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8",
        DB_USER, DB_PASS,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
    );
    // Trae máquinas que tienen IP WireGuard asignada
    $stmt = $pdo->query("
        SELECT id, device_no, device_ext_no, device_name, wg_ip,
               last_seen, status
        FROM vending_machines
        WHERE wg_ip IS NOT NULL AND wg_ip != ''
        ORDER BY device_name ASC
    ");
    $machines = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (Exception $e) {
    $db_error = $e->getMessage();
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Panel Powerbox – Máquinas Vending</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', sans-serif; background: #0f1117; color: #e0e0e0; }
  header { background: #1a1d27; padding: 16px 24px; border-bottom: 2px solid #2563eb;
           display: flex; align-items: center; gap: 12px; }
  header h1 { font-size: 1.2rem; color: #fff; }
  .badge { background: #2563eb; color: #fff; font-size: .75rem;
           padding: 2px 8px; border-radius: 99px; }

  .machines-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px,1fr));
                   gap: 16px; padding: 24px; }
  .card { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 10px;
          padding: 16px; transition: border-color .2s; }
  .card:hover { border-color: #2563eb; }
  .card-title { font-size: 1rem; font-weight: 600; color: #fff; margin-bottom: 4px; }
  .card-sub { font-size: .8rem; color: #888; margin-bottom: 12px; }
  .pill { display: inline-block; font-size: .72rem; padding: 2px 8px;
          border-radius: 99px; margin-bottom: 10px; }
  .pill.online  { background: #14532d; color: #4ade80; }
  .pill.offline { background: #3b1919; color: #f87171; }
  .pill.unknown { background: #2a2a2a; color: #aaa; }
  .card-ip { font-size: .78rem; color: #64748b; margin-bottom: 14px; font-family: monospace; }
  .btn-remote { width: 100%; padding: 9px; background: #2563eb; color: #fff;
                border: none; border-radius: 6px; cursor: pointer; font-size: .9rem;
                transition: background .2s; }
  .btn-remote:hover { background: #1d4ed8; }
  .btn-remote:disabled { background: #374151; cursor: not-allowed; }

  /* Modal de escritorio remoto */
  .modal-overlay { display: none; position: fixed; inset: 0;
                   background: rgba(0,0,0,.85); z-index: 1000;
                   align-items: center; justify-content: center; flex-direction: column; }
  .modal-overlay.active { display: flex; }
  .modal-header { width: 95vw; max-width: 1280px; display: flex; justify-content: space-between;
                  align-items: center; padding: 8px 12px; background: #1a1d27;
                  border-radius: 10px 10px 0 0; border: 1px solid #2a2d3a; }
  .modal-title { font-weight: 600; font-size: .95rem; }
  .modal-close { background: #ef4444; border: none; color: #fff; padding: 4px 12px;
                 border-radius: 6px; cursor: pointer; font-size: .85rem; }
  #remote-canvas { width: 95vw; max-width: 1280px; height: 70vh;
                   background: #000; border: 1px solid #2a2d3a;
                   border-radius: 0 0 10px 10px; display: block; }
  .modal-status { margin-top: 8px; font-size: .8rem; color: #888; }
</style>
</head>
<body>

<header>
  <h1>⚡ Panel Powerbox</h1>
  <span class="badge"><?= count($machines) ?> máquinas WireGuard</span>
</header>

<?php if (isset($db_error)): ?>
  <div style="background:#3b1919;color:#f87171;padding:16px 24px;margin:16px">
    ❌ Error DB: <?= htmlspecialchars($db_error) ?>
  </div>
<?php endif; ?>

<div class="machines-grid">
<?php foreach ($machines as $m):
    // Estado: online si last_seen < 5 minutos
    $last_seen = strtotime($m['last_seen'] ?? '');
    $is_online = $last_seen && (time() - $last_seen) < 300;
    $pill_class = $is_online ? 'online' : ($last_seen ? 'offline' : 'unknown');
    $pill_label = $is_online ? '● En línea' : ($last_seen ? '● Fuera de línea' : '● Sin datos');
?>
  <div class="card">
    <div class="card-title"><?= htmlspecialchars($m['device_name'] ?: 'Sin nombre') ?></div>
    <div class="card-sub"><?= htmlspecialchars($m['device_ext_no']) ?></div>
    <span class="pill <?= $pill_class ?>"><?= $pill_label ?></span>
    <div class="card-ip">WG IP: <?= htmlspecialchars($m['wg_ip']) ?></div>
    <button class="btn-remote"
            onclick="openRemote('<?= htmlspecialchars($m['wg_ip']) ?>',
                                '<?= htmlspecialchars($m['device_name'] ?: $m['device_ext_no']) ?>')"
            <?= $is_online ? '' : 'disabled title="Máquina fuera de línea"' ?>>
      🖥 Escritorio Remoto
    </button>
  </div>
<?php endforeach; ?>
<?php if (empty($machines)): ?>
  <p style="color:#666;padding:24px">No hay máquinas con WireGuard registrado aún.</p>
<?php endif; ?>
</div>

<!-- Modal Escritorio Remoto -->
<div class="modal-overlay" id="remote-modal">
  <div class="modal-header">
    <span class="modal-title" id="modal-machine-name">Máquina</span>
    <button class="modal-close" onclick="closeRemote()">✕ Cerrar</button>
  </div>
  <canvas id="remote-canvas"></canvas>
  <div class="modal-status" id="modal-status">Conectando…</div>
</div>

<script>
// ─── Variables globales del stream ────────────────────────────────
let ws       = null;
let mediaSource, sourceBuffer;
const VPS_HOST = '<?= VPS_WS_HOST ?>';

function openRemote(wgIp, machineName) {
    document.getElementById('modal-machine-name').textContent = '🖥 ' + machineName + ' (' + wgIp + ')';
    document.getElementById('remote-modal').classList.add('active');
    document.getElementById('modal-status').textContent = 'Conectando a ' + wgIp + '…';
    startWebSocketStream(wgIp);
}

function closeRemote() {
    document.getElementById('remote-modal').classList.remove('active');
    if (ws) { ws.close(); ws = null; }
    const video = document.getElementById('remote-canvas');
    const ctx = video.getContext('2d');
    ctx.clearRect(0, 0, video.width, video.height);
    document.getElementById('modal-status').textContent = '';
}

function startWebSocketStream(deviceIp) {
    const canvas = document.getElementById('remote-canvas');
    const statusEl = document.getElementById('modal-status');

    // Usamos un <video> invisible para decodificar H.264 via MSE
    const video = document.createElement('video');
    video.autoplay = true;
    video.muted    = true;
    video.style.display = 'none';
    document.body.appendChild(video);

    mediaSource = new MediaSource();
    video.src   = URL.createObjectURL(mediaSource);

    mediaSource.addEventListener('sourceopen', () => {
        try {
            sourceBuffer = mediaSource.addSourceBuffer('video/mp4; codecs="avc1.42E01E"');
        } catch (e) {
            statusEl.textContent = '❌ Codec H264 no soportado en este browser';
            return;
        }

        const wsUrl = `wss://${VPS_HOST}/scrcpy-ws/?device=${deviceIp}`;
        ws = new WebSocket(wsUrl);
        ws.binaryType = 'arraybuffer';

        ws.onopen    = () => { statusEl.textContent = '✅ Conectado – recibiendo stream…'; };
        ws.onmessage = (evt) => {
            if (sourceBuffer && !sourceBuffer.updating) {
                sourceBuffer.appendBuffer(evt.data);
            }
        };
        ws.onerror   = () => { statusEl.textContent = '❌ Error de conexión WebSocket'; };
        ws.onclose   = () => { statusEl.textContent = '🔌 Conexión cerrada'; video.remove(); };
    });

    // Dibujar video en canvas continuamente
    const ctx = canvas.getContext('2d');
    function drawFrame() {
        if (video.readyState >= 2) {
            canvas.width  = video.videoWidth  || canvas.width;
            canvas.height = video.videoHeight || canvas.height;
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        }
        requestAnimationFrame(drawFrame);
    }
    drawFrame();
}
</script>

</body>
</html>
