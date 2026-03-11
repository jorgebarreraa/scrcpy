/**
 * scrcpy-ws — WebSocket bridge para streaming de pantalla Android
 * Powerbox Vending Machine Network
 *
 * Protocolo:
 *   Server → Client : chunks binarios MPEG-TS (video MPEG1)
 *   Client → Server : JSON { type: 'tap'|'swipe'|'key', ... }
 */

const WebSocket = require('ws');
const { spawn }  = require('child_process');
const http       = require('http');
const url        = require('url');

const PORT       = 3001;
const ADB        = 'adb';
const FFMPEG     = 'ffmpeg';
const VIDEO_BPS  = '1200k';   // bitrate video MPEG1
const FPS        = 20;
// IPs permitidas en la VPN (solo rango vending)
const ALLOWED_SUBNET = /^10\.99\.0\.\d{1,3}$/;

// ─── Servidor HTTP base ───────────────────────────────────────────────────────
const server = http.createServer((req, res) => {
    res.writeHead(200);
    res.end('scrcpy-ws OK\n');
});

const wss = new WebSocket.Server({ server });

// ─── Conexión WebSocket ───────────────────────────────────────────────────────
wss.on('connection', (ws, req) => {
    // Debug: mostrar URL exacta que llega (ayuda a diagnosticar proxies nginx)
    console.log(`[scrcpy-ws] req.url = "${req.url}"`);

    // Parsear query string robustamente (soporta /path?k=v y ?k=v)
    let params;
    try {
        params = new url.URL(req.url, 'http://localhost').searchParams;
    } catch (_) {
        params = new url.URLSearchParams((req.url || '').split('?')[1] || '');
    }

    const device    = params.get('device');   // IP de la vending, ej: 10.99.0.2
    const widthReq  = parseInt(params.get('w') || '720',  10);
    const heightReq = parseInt(params.get('h') || '1280', 10);

    // Validar IP
    if (!device || !ALLOWED_SUBNET.test(device)) {
        console.warn(`[scrcpy-ws] IP rechazada: "${device}" (req.url="${req.url}")`);
        ws.close(1008, 'IP no permitida');
        return;
    }

    const adbTarget = `${device}:5555`;
    console.log(`[scrcpy-ws] Conectando a ${adbTarget} (${widthReq}x${heightReq})`);

    let adb    = null;
    let ffmpeg = null;
    let alive  = true;

    // ─── Lanzar stream ────────────────────────────────────────────────────────
    function startStream() {
        if (!alive) return;

        // screenrecord → H.264 raw por stdout
        adb = spawn(ADB, [
            '-s', adbTarget,
            'exec-out',
            'screenrecord',
            '--output-format=h264',
            '--bit-rate', '2000000',
            '--size', `${widthReq}x${heightReq}`,
            '-'
        ]);

        // H.264 → MPEG1 en MPEG-TS (formato que JSMpeg entiende)
        ffmpeg = spawn(FFMPEG, [
            '-loglevel', 'quiet',
            '-f',        'h264',
            '-i',        'pipe:0',
            '-c:v',      'mpeg1video',
            '-b:v',      VIDEO_BPS,
            '-r',        String(FPS),
            '-bf',       '0',
            '-f',        'mpegts',
            'pipe:1'
        ]);

        adb.stdout.pipe(ffmpeg.stdin);

        // Enviar chunks de video al browser
        ffmpeg.stdout.on('data', chunk => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(chunk, { binary: true }, err => {
                    if (err) console.error('[scrcpy-ws] send error:', err.message);
                });
            }
        });

        adb.stderr.on('data', d => console.error(`[adb] ${d}`));
        ffmpeg.stderr.on('data', d => { /* silencio — ya está en loglevel quiet */ });

        // Reiniciar si screenrecord termina (límite 3 min en algunos Android)
        adb.on('exit', code => {
            console.log(`[scrcpy-ws] screenrecord terminó (code ${code}), reiniciando...`);
            ffmpeg.kill('SIGKILL');
            if (alive) setTimeout(startStream, 800);
        });

        ffmpeg.on('exit', code => {
            console.log(`[scrcpy-ws] ffmpeg terminó (code ${code})`);
            adb.kill('SIGKILL');
            if (alive) setTimeout(startStream, 800);
        });
    }

    startStream();

    // ─── Recibir eventos de input desde el browser ────────────────────────────
    ws.on('message', raw => {
        try {
            const ev = JSON.parse(raw);
            const s  = `${adbTarget}`;

            if (ev.type === 'tap') {
                spawn(ADB, ['-s', s, 'shell', 'input', 'tap',
                    Math.round(ev.x), Math.round(ev.y)]);

            } else if (ev.type === 'swipe') {
                spawn(ADB, ['-s', s, 'shell', 'input', 'swipe',
                    Math.round(ev.x1), Math.round(ev.y1),
                    Math.round(ev.x2), Math.round(ev.y2),
                    ev.duration || 200]);

            } else if (ev.type === 'key') {
                // keycode: 3=Home, 4=Back, 187=Recents
                spawn(ADB, ['-s', s, 'shell', 'input', 'keyevent', ev.keycode]);

            } else if (ev.type === 'text') {
                spawn(ADB, ['-s', s, 'shell', 'input', 'text', ev.value]);
            }
        } catch (e) {
            console.error('[scrcpy-ws] mensaje inválido:', e.message);
        }
    });

    // ─── Limpiar al desconectar ────────────────────────────────────────────────
    ws.on('close', () => {
        console.log(`[scrcpy-ws] Cliente desconectado de ${adbTarget}`);
        alive = false;
        if (adb)    adb.kill('SIGKILL');
        if (ffmpeg) ffmpeg.kill('SIGKILL');
    });

    ws.on('error', err => {
        console.error('[scrcpy-ws] WebSocket error:', err.message);
    });
});

server.listen(PORT, '127.0.0.1', () => {
    console.log(`[scrcpy-ws] Servidor iniciado en 127.0.0.1:${PORT}`);
});
