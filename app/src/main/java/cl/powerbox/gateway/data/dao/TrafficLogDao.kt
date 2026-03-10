package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.powerbox.gateway.data.entity.TrafficLog

@Dao
interface TrafficLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TrafficLog)

    /** Últimas 300 entradas ordenadas por timestamp descendente */
    @Query("SELECT * FROM traffic_log ORDER BY timestamp DESC LIMIT 300")
    suspend fun getRecent(): List<TrafficLog>

    /** Eliminar registros más viejos de X milisegundos (limpieza periódica) */
    @Query("DELETE FROM traffic_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM traffic_log")
    suspend fun count(): Int
}
