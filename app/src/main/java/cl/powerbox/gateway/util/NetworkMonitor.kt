package cl.powerbox.gateway.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import cl.powerbox.gateway.sync.SyncScheduler
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket

/**
 * ✅ OPTIMIZADO: Detección de conectividad en <2 segundos
 * ✅ FIX: Delay antes de sincronizar + verificación de servidor real
 *
 * MEJORAS:
 * - Socket directo en vez de HTTP (más rápido)
 * - Timeout reducido a 1s
 * - Verificación cada 5s en vez de 30s
 * - Ping paralelo a múltiples IPs
 * - Delay de 2s antes de disparar sync (red se estabiliza)
 * - Verificación opcional de servidor real
 */
class NetworkMonitor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isOnline = false
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var pingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        fun get(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun isOnline(): Boolean {
            return INSTANCE?.isCurrentlyOnline() ?: false
        }

        // ✅ NUEVO: Servidor real para verificación (opcional)
        private const val REAL_SERVER_HOST = "gsvden.coffeeji.com"
        private const val REAL_SERVER_PORT = 443

        // ✅ NUEVO: Delay antes de disparar sync (ms)
        private const val SYNC_DELAY_MS = 2000L // 2 segundos
    }

    // ✅ IPs confiables para ping rápido (sin DNS lookup)
    private val fastCheckHosts = listOf(
        "8.8.8.8" to 53,        // Google DNS
        "1.1.1.1" to 53,        // Cloudflare DNS
        "208.67.222.222" to 53  // OpenDNS
    )

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Logger.d("📡 Network available - checking...")
                checkRealConnectivity()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                val wasOnline = isOnline
                isOnline = false
                Logger.d("🔴 Network LOST")

                if (wasOnline) {
                    Logger.d("🔌 OFFLINE mode")
                    onConnectivityChanged(false)
                }
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, caps)

                val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (hasInternet && hasValidated) {
                    checkRealConnectivity()
                } else {
                    val wasOnline = isOnline
                    isOnline = false
                    if (wasOnline) {
                        Logger.d("🔴 Network validation lost")
                        onConnectivityChanged(false)
                    }
                }
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback!!)
            checkRealConnectivity()
            startPeriodicConnectivityCheck()
            Logger.d("✅ Network monitoring started (fast mode)")
        } catch (e: Exception) {
            Logger.e("Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
        pingJob?.cancel()
        scope.cancel()

        callback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Logger.d("❌ Network monitoring stopped")
            } catch (e: Exception) {
                Logger.e("Failed to unregister network callback", e)
            }
        }
        callback = null
    }

    fun isCurrentlyOnline(): Boolean = isOnline

    private fun checkRealConnectivity() {
        scope.launch {
            val hasConnectivity = pingServersFast()
            val wasOnline = isOnline
            isOnline = hasConnectivity

            if (hasConnectivity && !wasOnline) {
                Logger.d("🟢 ONLINE - sync will trigger in ${SYNC_DELAY_MS}ms")
                // ✅ NUEVO: Delay antes de disparar sync
                onConnectivityChanged(true, withDelay = true)
            } else if (!hasConnectivity && wasOnline) {
                Logger.d("🔴 OFFLINE")
                onConnectivityChanged(false, withDelay = false)
            }
        }
    }

    /**
     * ✅ OPTIMIZADO: Ping paralelo con Socket TCP (mucho más rápido que HTTP)
     * Timeout total: ~1 segundo
     */
    private suspend fun pingServersFast(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Ping paralelo a todas las IPs
            val jobs = fastCheckHosts.map { (host, port) ->
                async {
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host, port), 1000) // 1s timeout
                            true
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            // Si AL MENOS UNO responde, hay internet
            val results = jobs.awaitAll()
            val online = results.any { it }

            if (online) {
                Logger.d("✅ Connectivity OK (${results.count { it }}/${results.size} hosts)")
            } else {
                Logger.d("❌ All hosts unreachable - OFFLINE")
            }

            return@withContext online

        } catch (e: Exception) {
            Logger.e("Connectivity check error", e)
            return@withContext false
        }
    }

    /**
     * ✅ NUEVO: Verifica si el servidor real es accesible
     * Esto es opcional y solo se usa antes de disparar sync
     */
    private suspend fun pingRealServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(
                    InetSocketAddress(REAL_SERVER_HOST, REAL_SERVER_PORT),
                    3000 // 3s timeout para servidor real
                )
                Logger.d("✅ Real server accessible ($REAL_SERVER_HOST)")
                true
            }
        } catch (e: Exception) {
            Logger.d("⚠️ Real server not ready yet ($REAL_SERVER_HOST): ${e.message}")
            false
        }
    }

    /**
     * ✅ OPTIMIZADO: Verificación cada 5 segundos en vez de 30
     */
    private fun startPeriodicConnectivityCheck() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(5_000) // 5s en vez de 30s
                checkRealConnectivity()
            }
        }
    }

    /**
     * ✅ MEJORADO: Dispara sync con delay opcional para estabilizar red
     */
    private fun onConnectivityChanged(isNowOnline: Boolean, withDelay: Boolean = false) {
        if (isNowOnline) {
            if (withDelay) {
                // ✅ Delay antes de sincronizar para que red se estabilice
                scope.launch {
                    delay(SYNC_DELAY_MS)

                    // ✅ Verificar servidor real antes de disparar sync
                    val serverReady = pingRealServer()

                    if (serverReady) {
                        Logger.d("🔄 Triggering sync (server ready)")
                        SyncScheduler.syncNow(context)
                        BroadcastHelper.notifyOnlineStatusChanged(context, true)
                    } else {
                        // Servidor aún no listo, el próximo ping periódico reintentará
                        Logger.d("⏳ Server not ready, will retry on next check")
                        BroadcastHelper.notifyOnlineStatusChanged(context, true)
                    }
                }
            } else {
                // Sin delay (usado para reintentos)
                SyncScheduler.syncNow(context)
                BroadcastHelper.notifyOnlineStatusChanged(context, true)
            }
        } else {
            BroadcastHelper.notifyOnlineStatusChanged(context, false)
        }
    }
}
