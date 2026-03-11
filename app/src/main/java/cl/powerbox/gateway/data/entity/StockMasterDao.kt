package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.powerbox.gateway.data.entity.StockMaster

@Dao
interface StockMasterDao {
    @Query("SELECT * FROM stock_master")
    suspend fun all(): List<StockMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<StockMaster>)
}
