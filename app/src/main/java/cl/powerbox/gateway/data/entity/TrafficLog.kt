package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de tráfico completo entre Vending ↔ Gateway ↔ Paneles web.
 *
 * direction:
 *   "VENDING→GW"  = petición recibida desde la app vending
 *   "GW→CJ"       = reenvío al panel CoffeeJi (gsvden.coffeeji.com)
 *   "GW→PB"       = reenvío al panel Powerbox (maquinas.powerboxchile.cl)
 */
@Entity(tableName = "traffic_log")
data class TrafficLog(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val direction: String,
    val method: String,
    val path: String,
    val requestBody: String?,
    val responseStatus: Int,
    val responseBody: String?,
    val durationMs: Long,
    val isOnline: Boolean
)
