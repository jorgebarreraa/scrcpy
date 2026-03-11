package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.powerbox.gateway.data.entity.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    suspend fun all(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<Product>)
}
