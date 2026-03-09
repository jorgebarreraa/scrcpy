#!/bin/bash
# ============================================================
# setup-server.sh — Configuración inicial del servidor WireGuard
# Ejecutar UNA SOLA VEZ en el VPS/servidor
# Compatible con Ubuntu 20.04+ / Debian
# ============================================================

set -e

WG_IFACE="wg0"
WG_PORT="51820"
WG_SUBNET="10.99.0.0/24"
WG_SERVER_IP="10.99.0.1/24"

echo "=== [1/5] Instalando WireGuard ==="
apt-get update -qq
apt-get install -y wireguard wireguard-tools

echo "=== [2/5] Generando claves del servidor ==="
cd /etc/wireguard
umask 077
wg genkey | tee server_private.key | wg pubkey > server_public.key
SERVER_PRIV=$(cat server_private.key)
SERVER_PUB=$(cat server_public.key)

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "  CLAVE PÚBLICA DEL SERVIDOR (copiar en register.php):"
echo "  $SERVER_PUB"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

echo "=== [3/5] Creando /etc/wireguard/${WG_IFACE}.conf ==="
cat > /etc/wireguard/${WG_IFACE}.conf << EOF
[Interface]
Address    = ${WG_SERVER_IP}
ListenPort = ${WG_PORT}
PrivateKey = ${SERVER_PRIV}

# Habilitar routing (máquinas pueden salir a internet por el VPS)
PostUp   = iptables -A FORWARD -i ${WG_IFACE} -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i ${WG_IFACE} -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

# Los peers (máquinas vending) se agregan automáticamente via register.php
# usando: wg set wg0 peer <pubkey> allowed-ips 10.99.0.X/32
EOF

echo "=== [4/5] Habilitando IP forwarding ==="
echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
sysctl -p

echo "=== [5/5] Iniciando WireGuard ==="
systemctl enable wg-quick@${WG_IFACE}
systemctl start  wg-quick@${WG_IFACE}

echo ""
echo "=== Configuración sudo para PHP ==="
echo "# Agregar en /etc/sudoers (sudo visudo):"
echo "www-data ALL=(ALL) NOPASSWD: /usr/bin/wg, /usr/bin/wg-quick"
echo ""
echo "=== Estado ==="
wg show

echo ""
echo "✅ Servidor WireGuard listo en ${WG_SERVER_IP} puerto ${WG_PORT}/udp"
echo "   Abre el puerto en el firewall: ufw allow ${WG_PORT}/udp"
