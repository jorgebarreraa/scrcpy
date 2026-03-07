package cl.powerbox.gateway.sync

import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.OfflineTransaction
import cl.powerbox.gateway.data.entity.ReplenishmentItem
import cl.powerbox.gateway.data.entity.ServerReplenishmentPayload
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class OfflineTransactionSync(private val database: AppDatabase) {

    companion object {
        private const val TAG = "OfflineTransactionSync"
    }

    suspend fun recordReplenishment(
        deviceId: String,
        materialId: String,
        quantity: Int
    ) {
        withContext(Dispatchers.IO) {
            val transaction = OfflineTransaction(
                id = java.util.UUID.randomUUID().toString(),
                deviceId = deviceId,
                materialId = materialId,
                replenishQt = quantity,
                operationType = "replenish",
                createdAt = System.currentTimeMillis(),
                synced = false
            )
            database.offlineTransactionDao().insert(transaction)
            Log.d(TAG, "✅ Recorded offline replenishment: materialId=$materialId, qty=$quantity, synced=false")
        }
    }

    suspend fun recordConsumption(
        deviceId: String,
        materialId: String,
        quantity: Int
    ) {
        withContext(Dispatchers.IO) {
            val transaction = OfflineTransaction(
                id = UUID.randomUUID().toString(),
                deviceId = deviceId,
                materialId = materialId,
                replenishQt = -quantity,
                operationType = "consume",
                createdAt = System.currentTimeMillis(),
                synced = false
            )
            database.offlineTransactionDao().insert(transaction)
            Log.d(TAG, "Recorded offline consumption: materialId=$materialId, qty=$quantity")
        }
    }

    suspend fun buildSyncPayload(deviceId: String): ServerReplenishmentPayload? {
        return withContext(Dispatchers.IO) {
            val pendingTransactions = database.offlineTransactionDao().getPendingTransactions()
            
            if (pendingTransactions.isEmpty()) {
                Log.d(TAG, "No pending transactions to sync")
                return@withContext null
            }

            // Agrupa por materialId y suma las cantidades (en caso de múltiples operaciones)
            val aggregatedByMaterial = pendingTransactions
                .groupBy { it.materialId }
                .mapValues { (_, transactions) ->
                    transactions.sumOf { it.replenishQt }
                }

            val items = aggregatedByMaterial.map { (materialId, totalQty) ->
                ReplenishmentItem(
                    id = materialId,
                    replenishQt = totalQty
                )
            }

            ServerReplenishmentPayload(
                deviceId = deviceId,
                items = items
            ).also {
                Log.d(TAG, "Built sync payload with ${items.size} items to sync")
            }
        }
    }

    suspend fun markTransactionsAsSynced() {
        withContext(Dispatchers.IO) {
            val pendingTransactions = database.offlineTransactionDao().getPendingTransactions()
            val timestamp = System.currentTimeMillis()
            
            pendingTransactions.forEach { transaction ->
                database.offlineTransactionDao().markAsSynced(transaction.id, timestamp)
                Log.d(TAG, "Marked transaction ${transaction.id} as synced")
            }
            
            Log.d(TAG, "Marked ${pendingTransactions.size} transactions as synced")
        }
    }

    suspend fun cleanOldSyncedTransactions(olderThanDays: Int = 7) {
        withContext(Dispatchers.IO) {
            val oneWeekAgoMs = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000)
            database.offlineTransactionDao().deleteOldSyncedTransactions(oneWeekAgoMs)
            Log.d(TAG, "Cleaned synced transactions older than $olderThanDays days")
        }
    }
}
