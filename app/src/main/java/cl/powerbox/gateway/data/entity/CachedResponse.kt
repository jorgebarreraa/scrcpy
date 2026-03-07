package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_response")  // ✅ Nombre correcto
data class CachedResponse(
    @PrimaryKey
    val key: String,
    val path: String,
    val method: String,
    val bodyHash: String,
    val contentType: String,
    val bytes: ByteArray,
    val cachedAt: Long = System.currentTimeMillis(),
    val lastHitAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CachedResponse
        if (key != other.key) return false
        if (path != other.path) return false
        if (method != other.method) return false
        if (bodyHash != other.bodyHash) return false
        if (contentType != other.contentType) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (cachedAt != other.cachedAt) return false
        if (lastHitAt != other.lastHitAt) return false
        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + bodyHash.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + cachedAt.hashCode()
        result = 31 * result + lastHitAt.hashCode()
        return result
    }
}
