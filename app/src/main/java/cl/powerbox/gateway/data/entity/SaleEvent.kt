package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_event")
data class SaleEvent(
    @PrimaryKey val id: String,          // UUID local
    val serverOrderId: String?,          // cuando online
    val clientOrderId: String?,          // idempotencia
    val productId: String,
    val qty: Int,
    val price: Long,                     // en centavos
    val createdAt: Long,
    val sent: Boolean                    // 0/1
)
