package cl.powerbox.gateway.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

/**
 * Observa cambios de conectividad sin usar CONNECTIVITY_ACTION (sin warnings).
 * Llama a [onOnline] únicamente cuando el dispositivo tiene NET_CAPABILITY_INTERNET.
 *
 * Uso:
 *   val nw = NetWatcher(context) { ...hacer algo cuando vuelve el Internet... }
 *   nw.start()
 *   ...
 *   nw.stop()
 */
class NetWatcher(
    private val ctx: Context,
    private val onOnline: () -> Unit
) {
    /** Guarda la referencia del callback para poder desregistrar. */
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun start() {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Callback común para ambas rutas
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onOnlineSafe(cm)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    onOnlineSafe(cm)
                }
            }
        }

        // API 24+: callback por defecto del sistema (cubre cambios globales)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(cb)
        } else {
            // API 21–23: registrar un NetworkRequest con capacidad de Internet
            val req: NetworkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(req, cb)
        }

        callback = cb

        // Estado inicial (por si ya estaba online antes de registrar)
        onOnlineSafe(cm)
    }

    fun stop() {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        callback?.let { runCatching { cm.unregisterNetworkCallback(it) } }
        callback = null
    }

    /**
     * Invoca onOnline() únicamente si el estado actual realmente tiene INET.
     * Protegido contra NPE/errores del framework.
     */
    private fun onOnlineSafe(cm: ConnectivityManager) {
        try {
            val n = cm.activeNetwork ?: return
            val caps = cm.getNetworkCapabilities(n) ?: return
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                onOnline.invoke()
            }
        } catch (_: Throwable) {
            // Ignorar cualquier error del framework
        }
    }

    companion object {
        /** Helper: estado actual de conectividad (con INET). */
        fun isOnline(ctx: Context): Boolean {
            return try {
                val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val n = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(n) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (_: Throwable) {
                false
            }
        }
    }
}
