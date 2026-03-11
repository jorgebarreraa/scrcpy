# Instalación del servidor scrcpy-ws en el VPS

## Estructura de archivos

```
server/
├── nginx/
│   └── scrcpy-ws.conf       ← fragmento para pegar en nginx
├── scrcpy-ws/
│   ├── server.js            ← servidor Node.js (corre en VPS)
│   └── scrcpy-ws.service    ← systemd service
└── panel/
    └── wg_admin_panel.php   ← subir a tu webhosting (gateway-api/)
```

---

## 1. Instalar dependencias en el VPS

```bash
apt update
apt install -y nodejs npm adb ffmpeg scrcpy
```

## 2. Instalar el servidor Node.js

```bash
mkdir -p /opt/scrcpy-ws
cp server/scrcpy-ws/server.js /opt/scrcpy-ws/
cd /opt/scrcpy-ws && npm init -y && npm install ws
```

## 3. Configurar nginx (proxy WebSocket)

Edita el archivo de tu sitio en el VPS:
```
/etc/nginx/sites-available/vpn.powerboxchile.cl
```
Dentro del bloque `server { }`, agrega:
```nginx
include snippets/scrcpy-ws.conf;
```
O copia el bloque `location` directamente (ver `nginx/scrcpy-ws.conf`).

```bash
cp server/nginx/scrcpy-ws.conf /etc/nginx/snippets/
nginx -t && systemctl reload nginx
```

## 4. Instalar como servicio systemd

```bash
cp server/scrcpy-ws/scrcpy-ws.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable scrcpy-ws
systemctl start scrcpy-ws
systemctl status scrcpy-ws
```

## 5. Subir el panel PHP al webhosting

Edita `server/panel/wg_admin_panel.php` y ajusta:
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'powerbox_db');
define('DB_USER', 'powerbox_user');
define('DB_PASS', 'tu_password');
define('VPS_WS_HOST', 'vpn.powerboxchile.cl');
```
Sube el archivo a:
```
/public_html/gateway-api/wg_admin_panel.php
```

---

## Flujo completo

```
Browser → wg_admin_panel.php (webhosting)
            ↓ elige máquina
Browser → wss://vpn.powerboxchile.cl/scrcpy-ws/?device=10.99.0.X
            ↓ nginx proxy
          127.0.0.1:3001 (server.js Node.js)
            ↓ adb connect + scrcpy + ffmpeg
          Máquina vending (10.99.0.X:5555 via WireGuard)
```
