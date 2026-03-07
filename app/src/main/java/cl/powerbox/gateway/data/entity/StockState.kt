package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa el stock "efectivo":
 *   effective = serverQty + localDelta
 * serverQty: último valor proveniente del panel (pull).
 * localDelta: acumulado local (ventas negativas, repones positivas) aún no reflejado en el panel.
 */
@Entity(tableName = "stock_state")
data class StockState(
    @PrimaryKey 
    val productId: String,
    val serverQty: Int,
    val localDelta: Int,
    val lastSync: Long  // ⬅️ Cambiado de updatedAt a lastSync
) {
    fun effective(): Int = serverQty + localDelta
}
