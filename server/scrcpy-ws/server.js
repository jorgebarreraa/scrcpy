/**
 * scrcpy-ws  –  Servidor WebSocket multi-máquina
 * ------------------------------------------------
 * Recibe conexiones WebSocket del browser con un parámetro ?device=<IP_WG>
 * y ejecuta: adb connect → scrcpy --video-codec=h264 → ffmpeg → stream binario
 *
 * Requisitos en el VPS:
 *   apt install nodejs npm adb ffmpeg
 *   npm install ws
 *
 * Arrancar:
 *   node server.js
 * O como servicio (ver scrcpy-ws.service)
 */

const { WebSocketServer, WebSocket } = require('ws');
const { spawn, execSync }            = require('child_process');

const PORT             = 3001;
const ADB_CONNECT_WAIT = 3000; // ms para esperar que adb conecte

const wss = new WebSocketServer({ port: PORT });
console.log(`[scrcpy-ws] Escuchando en ws://127.0.0.1:${PORT}`);

wss.on('connection', (ws, req) => {
    const params = new URLSearchParams(req.url.replace(/^\/\?/, ''));
    const device = params.get('device'); // ej: 10.99.0.2 o 10.99.0.2:5555

    if (!device || !/^[\d.]+(?::\d+)?$/.test(device)) {
        ws.close(1008, 'Parámetro device inválido');
        return;
    }

    const target = device.includes(':') ? device : `${device}:5555`;
    console.log(`[scrcpy-ws] Nueva conexión → device=${target}`);

    // ── Conectar ADB ──────────────────────────────────────────
    try {
        execSync(`adb connect ${target}`, { timeout: 5000 });
    } catch (e) {
        console.error(`[scrcpy-ws] adb connect falló: ${e.message}`);
        ws.close(1011, 'adb connect falló');
        return;
    }

    // Breve pausa para que adb establezca la conexión
    setTimeout(() => startStream(ws, target), ADB_CONNECT_WAIT);
});

function startStream(ws, target) {
    if (ws.readyState !== WebSocket.OPEN) return;

    // ── Lanzar scrcpy en modo headless ───────────────────────
    // --no-display : sin ventana local
    // --video-codec=h264 : H.264 para decodificar en browser con MSE
    // --record-format=mkv : salida a stdout en contenedor mkv
    // --serial : elige el dispositivo específico
    const scrcpy = spawn('scrcpy', [
        '--serial',        target,
        '--no-display',
        '--video-codec=h264',
        '--video-encoder=OMX.google.h264.encoder',
        '--record=-',
        '--record-format=mkv',
        '--bit-rate=2M',
        '--max-fps=30',
        '--max-size=1280',
    ]);

    // ── Pipe stdout de scrcpy al WebSocket ────────────────────
    scrcpy.stdout.on('data', (chunk) => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(chunk, { binary: true }, (err) => {
                if (err) console.error(`[scrcpy-ws] ws.send error: ${err.message}`);
            });
        }
    });

    scrcpy.stderr.on('data', (d) => process.stdout.write(`[scrcpy] ${d}`));

    scrcpy.on('close', (code) => {
        console.log(`[scrcpy-ws] scrcpy terminó (code=${code}) device=${target}`);
        if (ws.readyState === WebSocket.OPEN) ws.close();
    });

    // ── Cuando el browser cierra → matar scrcpy ───────────────
    ws.on('close', () => {
        console.log(`[scrcpy-ws] Browser desconectado → device=${target}`);
        scrcpy.kill('SIGTERM');
        try { execSync(`adb disconnect ${target}`); } catch (_) {}
    });

    ws.on('error', (err) => {
        console.error(`[scrcpy-ws] ws error: ${err.message}`);
        scrcpy.kill('SIGTERM');
    });
}
