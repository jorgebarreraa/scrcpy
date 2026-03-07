package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "offline_transactions")
data class OfflineTransaction(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val deviceId: String,
    val materialId: String,
    val replenishQt: Int,
    val operationType: String, // "replenish" or "consume"
    val createdAt: Long,
    val synced: Boolean = false,
    val syncedAt: Long? = null
)

// DTO that matches server API structure
data class ServerReplenishmentPayload(
    @JsonProperty("deviceId")
    val deviceId: String,
    @JsonProperty("items")
    val items: List<ReplenishmentItem>
)

data class ReplenishmentItem(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("replenishQt")
    val replenishQt: Int
)
