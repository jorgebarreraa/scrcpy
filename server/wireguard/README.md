# WireGuard — Guía de Setup Completo

## Arquitectura de red

```
Internet
    │
    ▼
[VPS/Servidor]
 eth0: IP pública
 wg0:  10.99.0.1/24    ← servidor WireGuard
    │
    │  túnel WireGuard (UDP 51820)
    │  cifrado Curve25519 + ChaCha20
    │
    ├── Máquina 001 (E00731) → 10.99.0.2/32
    ├── Máquina 002          → 10.99.0.3/32
    ├── Máquina 003          → 10.99.0.4/32
    └── ... hasta 253 máquinas
```

> **¿Por qué 10.99.0.x y no 10.0.0.x?**
> Las máquinas vending están en una LAN local (10.0.0.x).
> Si usáramos el mismo rango para WireGuard habría conflicto de rutas.
> El rango 10.99.0.0/24 está separado y no colisiona con ninguna red local típica.

---

## Paso 1 — Configurar el servidor WireGuard

```bash
chmod +x setup-server.sh
sudo ./setup-server.sh
```

Esto:
- Instala WireGuard
- Genera keypair del servidor en `/etc/wireguard/`
- Crea `/etc/wireguard/wg0.conf` con IP `10.99.0.1`
- Habilita IP forwarding
- Abre el servicio en el boot

**Anotar la clave pública** que aparece en pantalla.

---

## Paso 2 — Configurar register.php

Editar `register.php` y completar:

```php
define('DB_HOST',    'localhost');
define('DB_NAME',    'powerboxchile_ips');
define('DB_USER',    'tu_usuario_mysql');
define('DB_PASS',    'tu_password_mysql');

define('WG_SERVER_PUBKEY',   'PEGA_AQUI_LA_CLAVE_DEL_PASO_1');
define('WG_SERVER_ENDPOINT', 'tu-servidor.powerboxchile.cl:51820');
```

Copiar al servidor:
```bash
cp register.php /var/www/html/api/wireguard/register.php
```

Agregar permisos sudo para PHP:
```bash
sudo visudo
# Agregar esta línea:
www-data ALL=(ALL) NOPASSWD: /usr/bin/wg, /usr/bin/wg-quick
```

---

## Paso 3 — Configurar cada máquina Android (una vez por instalación)

En la app Gateway, insertar en la tabla `machine_config` de Room:

| key | value |
|-----|-------|
| `wg_server_base_url` | `https://maquinas.powerboxchile.cl` |
| `machine_number` | `E00731` (el device_ext_no de la máquina) |
| `machine_nickname` | `CASAPRUEBA` |

**Opción A — Via ADB (instalación inicial):**
```bash
# Abrir una shell en el dispositivo y ejecutar:
adb shell
# En el dispositivo:
sqlite3 /data/data/cl.powerbox.gateway/databases/gateway_clean.db \
  "INSERT OR REPLACE INTO machine_config VALUES ('wg_server_base_url','https://maquinas.powerboxchile.cl',$(date +%s)000);"
sqlite3 /data/data/cl.powerbox.gateway/databases/gateway_clean.db \
  "INSERT OR REPLACE INTO machine_config VALUES ('machine_number','E00731',$(date +%s)000);"
sqlite3 /data/data/cl.powerbox.gateway/databases/gateway_clean.db \
  "INSERT OR REPLACE INTO machine_config VALUES ('machine_nickname','CASAPRUEBA',$(date +%s)000);"
```

**Opción B — Endpoint HTTP local (recomendada para producción):**
La app ya tiene un servidor HTTP en `127.0.0.1:9090`. Se puede agregar
un endpoint de configuración en `HttpServer.kt` para setear estos valores
via POST desde una herramienta de provisioning.

---

## Paso 4 — Verificar conexión

En el servidor, ver los peers conectados:
```bash
sudo wg show
```

Salida esperada:
```
interface: wg0
  public key: <server_pubkey>
  listening port: 51820

peer: <pubkey_maquina_001>
  endpoint: 186.10.139.115:XXXXX
  allowed ips: 10.99.0.2/32
  latest handshake: X seconds ago
  transfer: X MiB received, X MiB sent

peer: <pubkey_maquina_002>
  ...
```

En la base de datos MySQL:
```sql
SELECT device_ext_no, device_name, wireguard_ip, wg_public_key, last_seen, status
FROM vending_machines
ORDER BY wireguard_ip;
```

---

## Conectarse a una máquina via scrcpy/ADB por WireGuard

Una vez el túnel está activo:
```bash
# En tu laptop (también conectada al servidor WireGuard)
adb connect 10.99.0.2:5555   # IP WireGuard de la máquina
scrcpy
```

Esto funciona desde CUALQUIER red del mundo, sin exponer puertos en la máquina.
