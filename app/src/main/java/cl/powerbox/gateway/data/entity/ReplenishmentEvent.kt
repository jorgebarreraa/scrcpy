package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replenishment_events")  // ✅ Nombre correcto (plural)
data class ReplenishmentEvent(
    @PrimaryKey
    val id: String,
    val productId: String,
    val deltaQty: Int,
    val createdAt: Long,
    val sent: Boolean
)
