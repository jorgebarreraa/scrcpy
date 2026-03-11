package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.StockState
import kotlinx.coroutines.delay

@Dao
interface StockStateDao {

    @Query("SELECT * FROM stock_state")
    fun all(): List<StockState>

    @Query("SELECT * FROM stock_state WHERE productId = :id LIMIT 1")
    fun byId(id: String): StockState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(state: StockState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuspend(state: StockState)

    @Query("UPDATE stock_state SET serverQty = :qty WHERE productId = :id")
    fun updateServerQty(id: String, qty: Int)

    @Query("UPDATE stock_state SET localDelta = 0 WHERE productId = :id")
    fun resetLocalDelta(id: String)

    @Query("DELETE FROM stock_state WHERE productId = :id")
    fun deleteById(id: String)

    @Transaction
    suspend fun ensureAndDeltaSuspend(productId: String, delta: Int, timestamp: Long): StockState {
        android.util.Log.d("GatewayOffline", "📊 ensureAndDelta: productId=$productId, delta=$delta, timestamp=$timestamp")
        
        val existing = byId(productId)
        
        val updated = if (existing == null) {
            android.util.Log.d("GatewayOffline", "📊 Creating NEW state: serverQty=0, localDelta=$delta")
            val newState = StockState(
                productId = productId,
                serverQty = 0,
                localDelta = delta,
                lastSync = timestamp
            )
            upsertSuspend(newState)
            newState
        } else {
            val newDelta = existing.localDelta + delta
            android.util.Log.d("GatewayOffline", "📊 UPDATING: serverQty=${existing.serverQty}, oldDelta=${existing.localDelta}, newDelta=$newDelta")
            val updated = existing.copy(localDelta = newDelta, lastSync = timestamp)
            upsertSuspend(updated)
            updated
        }

        delay(10)

        val verified = byId(productId)
        android.util.Log.d("GatewayOffline", "📊 VERIFIED: productId=$productId, serverQty=${verified?.serverQty}, localDelta=${verified?.localDelta}, effective=${(verified?.serverQty ?: 0) + (verified?.localDelta ?: 0)}")
        
        if (verified == null) {
            android.util.Log.e("GatewayOffline", "❌ CRITICAL: Failed to persist StockState for productId=$productId!")
        }
        
        return verified ?: updated
    }
}
