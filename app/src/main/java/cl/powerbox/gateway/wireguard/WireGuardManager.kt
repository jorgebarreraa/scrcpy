package cl.powerbox.gateway.wireguard

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.MachineConfig
import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.InetNetwork
import com.wireguard.config.Peer
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * Gestor del túnel WireGuard.
 *
 * Flujo de primera instalación (desatendida):
 *  1. Genera keypair y guarda private_key en machine_config
 *  2. Envía public_key al servidor de registro
 *  3. Recibe IP asignada + config del servidor
 *  4. Construye config WireGuard y levanta el túnel VPN
 *
 * En reinicios posteriores:
 *  - Lee config guardada en machine_config → levanta túnel directamente
 */
object WireGuardManager {

    // Keys en machine_config
    private const val KEY_WG_PRIVATE_KEY      = "wg_private_key"
    private const val KEY_WG_PUBLIC_KEY       = "wg_public_key"
    const val KEY_WG_ASSIGNED_IP              = "wg_assigned_ip"
    private const val KEY_WG_SERVER_PUBKEY    = "wg_server_public_key"
    private const val KEY_WG_SERVER_ENDPOINT  = "wg_server_endpoint"
    private const val KEY_WG_DNS              = "wg_dns"
    private const val KEY_WG_REGISTERED       = "wg_registered"
    // URL fija del endpoint de registro (mismo hosting que register_device.php)
    private const val WG_REGISTER_URL = "https://powerboxchile.cl/gateway-api/register_wireguard.php"
    const val KEY_WG_SERVER_BASE_URL  = "wg_server_base_url" // ya no se usa para registro, solo para referencia

    private const val DEVICE_REGISTER_URL = "https://powerboxchile.cl/gateway-api/register_device.php"
    private const val TUNNEL_NAME = "powerbox0"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val jsonMapper = jacksonObjectMapper()

    private var backend: GoBackend? = null
    private var tunnel: PowerboxTunnel? = null

    /** Clase interna que implementa la interfaz Tunnel de wireguard-android */
    private class PowerboxTunnel : Tunnel {
        @Volatile var state: Tunnel.State = Tunnel.State.DOWN

        override fun getName() = TUNNEL_NAME
        override fun onStateChange(newState: Tunnel.State) {
            state = newState
            Logger.d("[WG] Tunnel state → $newState")
        }
    }

    /**
     * Punto de entrada principal. Llamar desde GatewayForegroundService.
     * Gestiona todo el ciclo: generación de claves → registro → inicio de túnel.
     */
    suspend fun setup(ctx: Context) = withContext(Dispatchers.IO) {
        try {
            Logger.d("[WG] Iniciando setup WireGuard...")

            val db  = AppDatabase.get(ctx)
            val dao = db.machineConfigDao()

            // 1. Obtener o generar keypair
            val privateKeyStr = dao.getValue(KEY_WG_PRIVATE_KEY)
            val keyPair: KeyPair = if (privateKeyStr != null) {
                KeyPair(com.wireguard.crypto.Key.fromBase64(privateKeyStr))
            } else {
                val kp = KeyPair()
                val now = System.currentTimeMillis()
                dao.upsertAll(listOf(
                    MachineConfig(KEY_WG_PRIVATE_KEY, kp.privateKey.toBase64(), now),
                    MachineConfig(KEY_WG_PUBLIC_KEY,  kp.publicKey.toBase64(),  now)
                ))
                Logger.i("[WG] ✅ Keypair generado → pub=${kp.publicKey.toBase64().take(12)}...")
                kp
            }

            // 2. Verificar si ya está registrado
            val isRegistered = dao.getValue(KEY_WG_REGISTERED) == "true"

            if (!isRegistered) {
                Logger.d("[WG] Máquina no registrada → iniciando registro en servidor")
                register(ctx, keyPair)
            }

            // 3. Intentar levantar el túnel con la config guardada
            startTunnel(ctx, keyPair)

        } catch (e: Exception) {
            Logger.e("[WG] Error en setup: ${e.message}", e)
        }
    }

    /**
     * Pre-registra la máquina en register_device.php para que register_wireguard.php
     * pueda encontrarla en vending_machines. Esto permite el registro desatendido
     * sin esperar que la máquina vending se conecte y envíe deviceAllInfo.
     */
    private suspend fun ensureDeviceRegistered(identity: MachineIdentity.Identity) {
        try {
            val payload = jsonMapper.writeValueAsString(
                mapOf(
                    "device_no"     to identity.androidId,
                    "device_ext_no" to identity.machineNumber,
                    "device_name"   to identity.nickname,
                    "device_type"   to "Gateway",
                    "public_ip"     to "",
                    "status"        to 1
                )
            )
            val req = Request.Builder()
                .url(DEVICE_REGISTER_URL)
                .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
            val resp = httpClient.newCall(req).execute()
            Logger.d("[WG] Pre-registro device → HTTP ${resp.code}")
        } catch (e: Exception) {
            Logger.w("[WG] Pre-registro device falló (se intentará igual WG): ${e.message}")
        }
    }

    private suspend fun register(ctx: Context, keyPair: KeyPair) {
        val identity = MachineIdentity.get(ctx)

        // Asegurar que la máquina exista en vending_machines antes de registrar WireGuard
        ensureDeviceRegistered(identity)

        val response = WireGuardRegistrationClient.register(
            registerUrl = WG_REGISTER_URL,
            request = WireGuardRegistrationClient.RegistrationRequest(
                device_no     = identity.androidId,       // android_id → device_no en MySQL
                device_ext_no = identity.machineNumber,   // E00731, 001, etc.
                device_name   = identity.nickname,        // nombre visible
                public_key    = keyPair.publicKey.toBase64()
            )
        )

        if (response != null) {
            val dao = AppDatabase.get(ctx).machineConfigDao()
            val now = System.currentTimeMillis()
            dao.upsertAll(listOf(
                MachineConfig(KEY_WG_ASSIGNED_IP,     response.wireguard_ip,       now),
                MachineConfig(KEY_WG_SERVER_PUBKEY,   response.server_public_key,  now),
                MachineConfig(KEY_WG_SERVER_ENDPOINT, response.server_endpoint,    now),
                MachineConfig(KEY_WG_DNS,             response.dns,                now),
                MachineConfig(KEY_WG_REGISTERED,      "true",                      now)
            ))
            Logger.i("[WG] ✅ Registrado → IP=${response.wireguard_ip} | endpoint=${response.server_endpoint}")
        }
    }

    private suspend fun startTunnel(ctx: Context, keyPair: KeyPair) {
        val dao          = AppDatabase.get(ctx).machineConfigDao()
        val assignedIp   = dao.getValue(KEY_WG_ASSIGNED_IP)
        val serverPubKey = dao.getValue(KEY_WG_SERVER_PUBKEY)
        val endpoint     = dao.getValue(KEY_WG_SERVER_ENDPOINT)
        val dns          = dao.getValue(KEY_WG_DNS) ?: "1.1.1.1"

        if (assignedIp == null || serverPubKey == null || endpoint == null) {
            Logger.e("[WG] ❌ Config incompleta para levantar túnel (sin registro aún)")
            return
        }

        // Solicitar permiso VPN via root si no está concedido
        ensureVpnPermissionViaRoot(ctx)

        val wgInterface = Interface.Builder()
            .parsePrivateKey(keyPair.privateKey.toBase64())
            .addAddress(InetNetwork.parse(assignedIp))
            .addDnsServer(InetAddress.getByName(dns))
            .build()

        val peer = Peer.Builder()
            .parsePublicKey(serverPubKey)
            .parseEndpoint(endpoint)
            .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
            .setPersistentKeepalive(25)
            .build()

        val config = Config.Builder()
            .setInterface(wgInterface)
            .addPeer(peer)
            .build()

        if (backend == null) {
            backend = GoBackend(ctx)
        }

        val t = PowerboxTunnel().also { tunnel = it }
        backend!!.setState(t, Tunnel.State.UP, config)
        Logger.i("[WG] 🔒 Túnel WireGuard activo | IP=$assignedIp | server=$endpoint")
    }

    /** Usa root para conceder permiso VPN sin interacción del usuario */
    private fun ensureVpnPermissionViaRoot(ctx: Context) {
        try {
            val pkg = ctx.packageName
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c",
                "appops set $pkg ACTIVATE_VPN allow"
            ))
            proc.waitFor()
            Logger.d("[WG] Permiso VPN concedido via root")
        } catch (e: Exception) {
            Logger.e("[WG] No se pudo conceder permiso VPN via root: ${e.message}")
        }
    }

    fun isConnected(): Boolean = tunnel?.state == Tunnel.State.UP

    suspend fun stop() = withContext(Dispatchers.IO) {
        try {
            val t = tunnel ?: return@withContext
            backend?.setState(t, Tunnel.State.DOWN, null)
            Logger.i("[WG] Túnel detenido")
        } catch (e: Exception) {
            Logger.e("[WG] Error deteniendo túnel: ${e.message}")
        }
    }

    /** Retorna un resumen del estado para mostrar en la UI */
    suspend fun getStatusSummary(ctx: Context): String {
        val dao = AppDatabase.get(ctx).machineConfigDao()
        val registered = dao.getValue(KEY_WG_REGISTERED) == "true"
        val ip         = dao.getValue(KEY_WG_ASSIGNED_IP) ?: "—"
        val endpoint   = dao.getValue(KEY_WG_SERVER_ENDPOINT) ?: "—"
        val connected  = isConnected()
        return buildString {
            appendLine("WireGuard")
            appendLine("  Estado: ${if (connected) "CONECTADO" else if (registered) "REGISTRADO" else "SIN REGISTRAR"}")
            appendLine("  IP túnel: $ip")
            appendLine("  Servidor: $endpoint")
        }
    }
}
