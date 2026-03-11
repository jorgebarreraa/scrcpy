package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_master")
data class StockMaster(
    @PrimaryKey val productId: String,
    val qty: Int,
    val updatedAt: Long
)
