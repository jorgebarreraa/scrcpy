#!/bin/bash
# =============================================================
# sync_wireguard_peers.sh
# Corre en el VPS WireGuard cada 1 minuto via cron.
# Lee MySQL → busca peers con wg_peer_active=0 → ejecuta wg set
#
# INSTALACIÓN EN EL VPS:
#   chmod +x sync_wireguard_peers.sh
#   cp sync_wireguard_peers.sh /usr/local/bin/
#   crontab -e
#   # Agregar:
#   * * * * * /usr/local/bin/sync_wireguard_peers.sh >> /var/log/wg_sync.log 2>&1
# =============================================================

DB_HOST="localhost"
DB_NAME="powerboxchile_ips"
DB_USER="powerboxchile_ips"
DB_PASS='@Playstation9875!'
WG_IFACE="wg0"

# Obtener peers pendientes (wg_peer_active = 0, tienen public_key)
PEERS=$(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "
    SELECT id, device_ext_no, wireguard_ip, wg_public_key
    FROM vending_machines
    WHERE wg_public_key IS NOT NULL
      AND wireguard_ip IS NOT NULL
      AND wg_peer_active = 0;
")

if [ -z "$PEERS" ]; then
    exit 0  # nada que sincronizar
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sincronizando peers WireGuard..."

while IFS=$'\t' read -r id device_ext_no wireguard_ip wg_public_key; do
    # Quitar el /32 para el comando wg
    ip_bare="${wireguard_ip%/32}"

    echo "  → Agregando peer: $device_ext_no | IP: $ip_bare | pubkey: ${wg_public_key:0:12}..."

    # Agregar peer al servidor WireGuard
    wg set "$WG_IFACE" \
        peer "$wg_public_key" \
        allowed-ips "$ip_bare/32" \
        persistent-keepalive 25

    if [ $? -eq 0 ]; then
        # Marcar como activo en MySQL
        mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
            UPDATE vending_machines SET wg_peer_active = 1 WHERE id = $id;
        "
        echo "  ✅ $device_ext_no → $ip_bare activado"
    else
        echo "  ❌ Error agregando $device_ext_no"
    fi
done <<< "$PEERS"

# Persistir config para que sobreviva reinicios del VPS
wg-quick save "$WG_IFACE" 2>/dev/null

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Sync completado."
