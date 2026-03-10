#!/bin/bash
# =============================================================
# sync_wireguard_peers.sh
# Corre en el VPS WireGuard cada 1 minuto via cron (como root).
# Llama al proxy PHP en el hosting via HTTPS.
#
# INSTALACIÓN EN EL VPS:
#   chmod +x sync_wireguard_peers.sh
#   cp sync_wireguard_peers.sh /usr/local/bin/
#   crontab -e   (como root: sudo crontab -e)
#   # Agregar:
#   * * * * * /usr/local/bin/sync_wireguard_peers.sh >> /var/log/wg_sync.log 2>&1
# =============================================================

API_URL="https://powerboxchile.cl/gateway-api/wg_sync_api.php"
API_SECRET="pb_wg_s3cr3t_2026"
WG_IFACE="wg0"
WG_CONF="/etc/wireguard/${WG_IFACE}.conf"

# Verificar que corremos como root (wg necesita root)
if [ "$(id -u)" -ne 0 ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: debe ejecutarse como root"
    exit 1
fi

# Obtener peers pendientes (wg_peer_active = 0)
RESPONSE=$(curl -sf --max-time 15 \
    -H "X-WG-Secret: $API_SECRET" \
    "${API_URL}?action=pending")

if [ $? -ne 0 ] || [ -z "$RESPONSE" ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: No se pudo contactar el API proxy"
    exit 1
fi

OK=$(echo "$RESPONSE" | python3 -c "import sys,json; d=json.load(sys.stdin); print('yes' if d.get('ok') else 'no')" 2>/dev/null)
if [ "$OK" != "yes" ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR API: $RESPONSE"
    exit 1
fi

PEERS=$(echo "$RESPONSE" | python3 -c "
import sys, json
d = json.load(sys.stdin)
for p in d.get('peers', []):
    print(p['id'], p.get('source','vending'), p['device_ext_no'], p['wireguard_ip'], p['wg_public_key'])
" 2>/dev/null)

if [ -z "$PEERS" ]; then
    exit 0  # nada que sincronizar
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sincronizando peers WireGuard..."

while read -r id source device_ext_no wireguard_ip wg_public_key; do
    ip_bare="${wireguard_ip%/32}"

    echo "  → [$source] $device_ext_no | IP: $ip_bare | pubkey: ${wg_public_key:0:16}..."

    # ── 1. Agregar al interfaz activo (efecto inmediato, sin reinicio) ──────────
    wg set "$WG_IFACE" \
        peer "$wg_public_key" \
        allowed-ips "${ip_bare}/32"

    if [ $? -ne 0 ]; then
        echo "  ❌ Error en wg set para $device_ext_no"
        continue
    fi

    # ── 2. Persistir en wg0.conf (sobrevive reinicios del VPS) ─────────────────
    # Solo agregar si la clave pública no está ya en el archivo
    if ! grep -qF "$wg_public_key" "$WG_CONF" 2>/dev/null; then
        cat >> "$WG_CONF" <<PEER

[Peer]
# $device_ext_no ($source) — agregado $(date '+%Y-%m-%d %H:%M:%S')
PublicKey = $wg_public_key
AllowedIPs = ${ip_bare}/32
PEER
        echo "  📝 Escrito en $WG_CONF"
    fi

    # ── 3. Marcar como activo en la BD ──────────────────────────────────────────
    curl -sf --max-time 10 \
        -H "X-WG-Secret: $API_SECRET" \
        -H "Content-Type: application/json" \
        -d "{\"action\":\"activate\",\"id\":$id,\"source\":\"$source\"}" \
        "$API_URL" > /dev/null

    echo "  ✅ $device_ext_no → ${ip_bare} activo"

done <<< "$PEERS"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sync completado."


# Obtener peers pendientes (wg_peer_active = 0)
RESPONSE=$(curl -sf \
    -H "X-WG-Secret: $API_SECRET" \
    "${API_URL}?action=pending")

if [ $? -ne 0 ] || [ -z "$RESPONSE" ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: No se pudo contactar el API proxy"
    exit 1
fi

OK=$(echo "$RESPONSE" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('ok','false'))" 2>/dev/null)
if [ "$OK" != "True" ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR API: $RESPONSE"
    exit 1
fi

PEERS=$(echo "$RESPONSE" | python3 -c "
import sys, json
d = json.load(sys.stdin)
for p in d.get('peers', []):
    print(p['id'], p.get('source','vending'), p['device_ext_no'], p['wireguard_ip'], p['wg_public_key'])
" 2>/dev/null)

if [ -z "$PEERS" ]; then
    exit 0  # nada que sincronizar
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sincronizando peers WireGuard..."

while read -r id source device_ext_no wireguard_ip wg_public_key; do
    # Quitar el /32 para el comando wg
    ip_bare="${wireguard_ip%/32}"

    echo "  → Agregando peer [$source]: $device_ext_no | IP: $ip_bare | pubkey: ${wg_public_key:0:12}..."

    # Agregar peer al servidor WireGuard
    wg set "$WG_IFACE" \
        peer "$wg_public_key" \
        allowed-ips "$ip_bare/32" \
        persistent-keepalive 25

    if [ $? -eq 0 ]; then
        # Marcar como activo via API proxy (pasar source para actualizar la tabla correcta)
        curl -sf \
            -H "X-WG-Secret: $API_SECRET" \
            -H "Content-Type: application/json" \
            -d "{\"action\":\"activate\",\"id\":$id,\"source\":\"$source\"}" \
            "$API_URL" > /dev/null
        echo "  ✅ $device_ext_no → $ip_bare activado"
    else
        echo "  ❌ Error agregando $device_ext_no"
    fi
done <<< "$PEERS"

# Persistir config para que sobreviva reinicios del VPS
wg-quick save "$WG_IFACE" 2>/dev/null

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sync completado."
