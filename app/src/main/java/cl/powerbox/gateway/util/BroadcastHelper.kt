package cl.powerbox.gateway.util

import android.content.Context
import android.content.Intent

/**
 * ✅ Sistema de broadcast para notificar cambios al Vending Machine
 *
 * Eventos:
 * - Cambio de estado online/offline
 * - Cambio de stock (relleno/venta)
 * - Sincronización completada
 */
object BroadcastHelper {

    // Actions
    const val ACTION_ONLINE_STATUS = "cl.powerbox.gateway.ONLINE_STATUS_CHANGED"
    const val ACTION_STOCK_CHANGED = "cl.powerbox.gateway.STOCK_CHANGED"
    const val ACTION_SYNC_COMPLETE = "cl.powerbox.gateway.SYNC_COMPLETE"

    // Extras
    const val EXTRA_IS_ONLINE = "is_online"
    const val EXTRA_PRODUCT_IDS = "product_ids"
    const val EXTRA_SYNC_SUCCESS = "sync_success"

    /**
     * Notifica cambio de estado online/offline
     */
    fun notifyOnlineStatusChanged(context: Context, isOnline: Boolean) {
        val intent = Intent(ACTION_ONLINE_STATUS).apply {
            putExtra(EXTRA_IS_ONLINE, isOnline)
            setPackage("com.yj.coffeemachines") // Vending package
        }
        context.sendBroadcast(intent)
        Logger.d("📢 Broadcast: Online=${isOnline}")
    }

    /**
     * Notifica cambio de stock (después de relleno o venta)
     */
    fun notifyStockChanged(context: Context, productIds: List<String>) {
        val intent = Intent(ACTION_STOCK_CHANGED).apply {
            putStringArrayListExtra(EXTRA_PRODUCT_IDS, ArrayList(productIds))
            setPackage("com.yj.coffeemachines")
        }
        context.sendBroadcast(intent)
        Logger.d("📢 Broadcast: Stock changed for ${productIds.size} products")
    }

    /**
     * Notifica que la sincronización completó
     */
    fun notifySyncComplete(context: Context, success: Boolean) {
        val intent = Intent(ACTION_SYNC_COMPLETE).apply {
            putExtra(EXTRA_SYNC_SUCCESS, success)
            setPackage("com.yj.coffeemachines")
        }
        context.sendBroadcast(intent)
        Logger.d("📢 Broadcast: Sync complete (success=$success)")
    }
}
