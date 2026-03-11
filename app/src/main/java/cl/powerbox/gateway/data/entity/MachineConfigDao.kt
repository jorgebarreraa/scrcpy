package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.powerbox.gateway.data.entity.MachineConfig

@Dao
interface MachineConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<MachineConfig>)

    @Query("SELECT * FROM machine_config")
    suspend fun all(): List<MachineConfig>

    @Query("SELECT value FROM machine_config WHERE key = :key LIMIT 1")
    suspend fun getValue(key: String): String?
}
