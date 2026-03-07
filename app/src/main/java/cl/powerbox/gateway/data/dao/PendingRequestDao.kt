package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.PendingRequest

@Dao
interface PendingRequestDao {

    @Query("SELECT * FROM pending_requests ORDER BY createdAt ASC")
    fun allPending(): List<PendingRequest>

    @Query("SELECT * FROM pending_requests ORDER BY createdAt ASC")
    fun getAll(): List<PendingRequest>

    @Query("SELECT * FROM pending_requests WHERE id = :id LIMIT 1")
    fun byId(id: String): PendingRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(request: PendingRequest)

    @Query("DELETE FROM pending_requests WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM pending_requests WHERE createdAt < :timestamp")
    fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM pending_requests")
    fun deleteSynced(): Int

    @Query("SELECT COUNT(*) FROM pending_requests")
    fun count(): Int
}
