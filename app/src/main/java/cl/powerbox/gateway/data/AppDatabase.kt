package cl.powerbox.gateway.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.powerbox.gateway.data.dao.*
import cl.powerbox.gateway.data.entity.*

@Database(
    entities = [
        CachedResponse::class,
        PendingRequest::class,
        Product::class,
        StockMaster::class,
        SaleEvent::class,
        ReplenishmentEvent::class,
        MachineConfig::class,
        StockState::class,
        OfflineTransaction::class  // Added new entity for offline transaction tracking
    ],
    version = 2,  // Incremented from 1 to 2 to force schema migration after annotation changes
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cachedDao(): CachedResponseDao
    abstract fun pendingDao(): PendingRequestDao
    abstract fun productDao(): ProductDao
    abstract fun stockMasterDao(): StockMasterDao
    abstract fun saleDao(): SaleEventDao
    abstract fun replenishmentEventDao(): ReplenishmentEventDao
    abstract fun machineConfigDao(): MachineConfigDao
    abstract fun stockStateDao(): StockStateDao
    abstract fun offlineTransactionDao(): OfflineTransactionDao  // Added DAO accessor

    // ✅ Alias para consistencia
    fun pendingRequestDao() = pendingDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    "gateway_clean.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
