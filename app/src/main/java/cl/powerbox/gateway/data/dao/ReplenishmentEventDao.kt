package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.ReplenishmentEvent

@Dao
interface ReplenishmentEventDao {

    @Query("SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    fun all(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    fun getAll(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    suspend fun allUnsent(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    suspend fun pending(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    suspend fun allSentSuspend(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    fun allSent(): List<ReplenishmentEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(event: ReplenishmentEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuspend(event: ReplenishmentEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(events: List<ReplenishmentEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllSuspend(events: List<ReplenishmentEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: ReplenishmentEvent)

    @Query("UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    suspend fun markAsSentSuspend(eventId: String)

    @Query("UPDATE replenishment_events SET sent = 1 WHERE id IN (:ids)")
    suspend fun markSentSuspend(ids: List<String>)

    @Query("UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    fun markAsSent(eventId: String)

    @Query("UPDATE replenishment_events SET sent = 1 WHERE id IN (:ids)")
    fun markSent(ids: List<String>)

    @Query("DELETE FROM replenishment_events WHERE sent = 1 AND createdAt < :timestamp")
    fun deleteOldSent(timestamp: Long): Int

    @Query("DELETE FROM replenishment_events WHERE id = :id")
    fun deleteById(id: String)
}
