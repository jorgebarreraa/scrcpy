package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.powerbox.gateway.data.entity.SaleEvent

@Dao
interface SaleEventDao {
    @Insert
    suspend fun insert(e: SaleEvent)

    @Query("SELECT * FROM sale_event WHERE sent = 0 ORDER BY createdAt ASC")
    suspend fun pending(): List<SaleEvent>

    @Query("UPDATE sale_event SET sent = 1 WHERE id IN (:ids)")
    suspend fun markSent(ids: List<String>)
}
