package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_requests")  // ✅ Nombre correcto (plural)
data class PendingRequest(
    @PrimaryKey
    val id: String,
    val path: String,
    val method: String,
    val headersJson: String,
    val body: ByteArray,
    val clientOrderId: String?,
    val createdAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PendingRequest
        if (id != other.id) return false
        if (path != other.path) return false
        if (method != other.method) return false
        if (headersJson != other.headersJson) return false
        if (!body.contentEquals(other.body)) return false
        if (clientOrderId != other.clientOrderId) return false
        if (createdAt != other.createdAt) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headersJson.hashCode()
        result = 31 * result + body.contentHashCode()
        result = 31 * result + (clientOrderId?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
