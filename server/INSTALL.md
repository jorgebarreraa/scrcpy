# Instalación del servidor scrcpy-ws en el VPS
## Guía definitiva y unificada

---

## Arquitectura

```
Tu WebHosting (maquinas.powerboxchile.cl)
  └── wg_admin_panel.php  ← lista máquinas, botón "Escritorio Remoto"
          │
          │  El browser abre WebSocket directo al VPS
          ▼
Tu VPS (vpn.powerboxchile.cl)
  ├── nginx  ← expone /scrcpy-ws/ al exterior (puerto 443/wss)
  └── server.js (Node.js :3001)  ← recibe WebSocket, ejecuta scrcpy
          │  adb over WireGuard
          ▼
  Máquina Vending (10.99.0.X:5555)
```

---

## Estructura de archivos del repo

```
server/
├── nginx/
│   └── scrcpy-ws.conf       ← bloque location{} para pegar en nginx
├── scrcpy-ws/
│   ├── server.js            ← servidor Node.js (instalar en VPS)
│   └── scrcpy-ws.service    ← servicio systemd
└── panel/
    └── wg_admin_panel.php   ← subir al webhosting
```

---

## PASO 1 – Clonar/actualizar el repo en el VPS

```bash
# Si es la primera vez:
cd /opt
git clone https://github.com/jorgebarreraa/GatewayOffline-Powerbox2026.git

# Si ya lo tienes clonado, actualizar:
cd /opt/GatewayOffline-Powerbox2026
git pull origin main
```

---

## PASO 2 – Instalar dependencias

```bash
apt update
apt install -y nodejs npm android-tools-adb ffmpeg

# Verificar versiones
node --version    # debe ser >= 16
adb version
ffmpeg -version | head -1
```

> **Nota sobre scrcpy:** `scrcpy` en el VPS headless funciona distinto al desktop.
> El servidor Node.js usa `scrcpy` internamente para conectarse al Android por ADB.
> Instalar con:
> ```bash
> apt install -y scrcpy
> # o si la versión del repo es muy vieja (< 2.0):
> snap install scrcpy
> ```

---

## PASO 3 – Instalar el servidor Node.js

```bash
mkdir -p /opt/scrcpy-ws
cp /opt/GatewayOffline-Powerbox2026/server/scrcpy-ws/server.js /opt/scrcpy-ws/
cd /opt/scrcpy-ws
npm init -y
npm install ws

# Verificar que arranca:
node server.js
# Debe mostrar: [scrcpy-ws] Escuchando en ws://127.0.0.1:3001
# Ctrl+C para detener
```

---

## PASO 4 – Instalar como servicio systemd

```bash
cp /opt/GatewayOffline-Powerbox2026/server/scrcpy-ws/scrcpy-ws.service /etc/systemd/system/

systemctl daemon-reload
systemctl enable scrcpy-ws
systemctl start scrcpy-ws

# Verificar que está corriendo:
systemctl status scrcpy-ws
# Debe mostrar: Active: active (running)
```

Si el servicio falla, ver logs:
```bash
journalctl -u scrcpy-ws -n 50 --no-pager
```

---

## PASO 5 – Configurar nginx

### 5.1 – Copiar el snippet

```bash
cp /opt/GatewayOffline-Powerbox2026/server/nginx/scrcpy-ws.conf /etc/nginx/snippets/scrcpy-ws.conf
```

### 5.2 – Agregar al sitio nginx del VPS

Editar el archivo de configuración del sitio:
```bash
nano /etc/nginx/sites-available/vpn.powerboxchile.cl
# (o donde esté tu config, puede ser /etc/nginx/conf.d/default.conf)
```

Dentro del bloque `server { ... }`, **antes del cierre `}`**, agregar esta línea:
```nginx
server {
    listen 443 ssl;
    server_name vpn.powerboxchile.cl;

    # ... tu config SSL existente ...

    # ← AGREGAR ESTA LÍNEA:
    include snippets/scrcpy-ws.conf;
}
```

### 5.3 – Verificar y recargar

```bash
nginx -t
# Debe mostrar: syntax is ok / test is successful

systemctl reload nginx
```

> **Si ya habías pegado el bloque location{} manualmente en un paso anterior:**
> Busca si hay un bloque `location /scrcpy-ws/` duplicado y elimínalo antes de
> agregar el `include`. Verifica con:
> ```bash
> grep -n "scrcpy-ws" /etc/nginx/sites-available/vpn.powerboxchile.cl
> ```

---

## PASO 6 – Panel PHP en el webhosting

### 6.1 – Editar credenciales

En el archivo `server/panel/wg_admin_panel.php`, ajusta estas líneas:
```php
define('DB_HOST', 'localhost');          // host de tu MySQL
define('DB_NAME', 'nombre_bd');          // nombre de la base de datos
define('DB_USER', 'usuario_bd');         // usuario MySQL
define('DB_PASS', 'contraseña_bd');      // contraseña MySQL
define('VPS_WS_HOST', 'vpn.powerboxchile.cl'); // dominio del VPS (sin https://)
```

### 6.2 – Subir al webhosting

```
Ruta destino: /public_html/gateway-api/wg_admin_panel.php
URL de acceso: https://maquinas.powerboxchile.cl/gateway-api/wg_admin_panel.php
```

---

## PASO 7 – Diagnóstico completo (verificar todo)

Ejecutar en el VPS para ver el estado de cada componente:

```bash
echo "=== Node.js ===" && node --version
echo "=== scrcpy-ws service ===" && systemctl is-active scrcpy-ws
echo "=== Puerto 3001 ===" && ss -tlnp | grep 3001
echo "=== ADB ===" && adb devices
echo "=== nginx test ===" && nginx -t
echo "=== nginx WebSocket proxy ===" && grep -n "scrcpy" /etc/nginx/snippets/scrcpy-ws.conf
```

---

## Flujo completo

```
1. Usuario abre wg_admin_panel.php en el browser
2. PHP consulta la DB → lista máquinas con wg_ip asignada
3. Usuario hace clic en "Escritorio Remoto" en la tarjeta de una máquina
4. Browser abre: wss://vpn.powerboxchile.cl/scrcpy-ws/?device=10.99.0.X
5. nginx hace proxy → 127.0.0.1:3001 (server.js)
6. server.js ejecuta: adb connect 10.99.0.X:5555
7. scrcpy transmite video H.264 → stdout → WebSocket → browser
8. Browser decodifica y muestra en <canvas>
```
