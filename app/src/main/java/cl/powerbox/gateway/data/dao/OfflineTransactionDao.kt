package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.powerbox.gateway.data.entity.OfflineTransaction

@Dao
interface OfflineTransactionDao {
    @Insert
    suspend fun insert(transaction: OfflineTransaction)

    @Insert
    suspend fun insertAll(transactions: List<OfflineTransaction>)

    @Update
    suspend fun update(transaction: OfflineTransaction)

    @Query("SELECT * FROM offline_transactions WHERE synced = 0 ORDER BY createdAt ASC")
    suspend fun getPendingTransactions(): List<OfflineTransaction>

    @Query("SELECT * FROM offline_transactions WHERE synced = 1 ORDER BY syncedAt DESC LIMIT 100")
    suspend fun getSyncedTransactions(): List<OfflineTransaction>

    @Query("UPDATE offline_transactions SET synced = 1, syncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM offline_transactions WHERE synced = 0")
    suspend fun countPending(): Int

    @Query("SELECT * FROM offline_transactions WHERE synced = 0 ORDER BY createdAt ASC LIMIT 100")
    suspend fun getPendingForDebug(): List<OfflineTransaction>

    @Query("DELETE FROM offline_transactions WHERE synced = 1 AND syncedAt < :olderThanTimestamp")
    suspend fun deleteOldSyncedTransactions(olderThanTimestamp: Long)

    @Delete
    suspend fun delete(transaction: OfflineTransaction)

    @Query("DELETE FROM offline_transactions")
    suspend fun deleteAll()
}
