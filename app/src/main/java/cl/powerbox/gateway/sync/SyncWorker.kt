package cl.powerbox.gateway.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkUtil
import cl.powerbox.gateway.util.ServerConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = AppDatabase.get(context)
    private val mapper = jacksonObjectMapper()
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        return try {
            if (!NetworkUtil.isOnline(applicationContext)) {
                Logger.d("SyncWorker: No internet connection, will retry later")
                return Result.retry()
            }

            Logger.d("🔄 SyncWorker: Starting synchronization...")
            ServerConfig.logCurrentConfig(applicationContext)

            val syncedPending = syncPendingRequests()
            val syncedReplenishments = syncReplenishmentEvents()
            cleanupSyncedStockStates()

            Logger.d("✅ SyncWorker: Completed - Pending: $syncedPending, Replenishments: $syncedReplenishments")

            Result.success()
        } catch (e: Exception) {
            Logger.e("❌ SyncWorker: Error during sync", e)
            Result.retry()
        }
    }

    /**
     * Sincroniza las solicitudes pendientes enviándolas a todos los paneles de salida configurados.
     * Se elimina de la BD solo si el panel primario (primero) responde con éxito.
     */
    private suspend fun syncPendingRequests(): Int {
        val pending = withContext(Dispatchers.IO) {
            db.pendingRequestDao().allPending()
        }

        if (pending.isEmpty()) {
            Logger.d("No pending requests to sync")
            return 0
        }

        val outputUrls = ServerConfig.getOutputUrls(applicationContext)
        Logger.d("Syncing ${pending.size} pending requests to ${outputUrls.size} panel(s)...")
        var synced = 0

        for (req in pending) {
            try {
                @Suppress("UNCHECKED_CAST")
                val headersMap = try {
                    mapper.readValue(req.headersJson, Map::class.java) as Map<String, String>
                } catch (_: Throwable) {
                    emptyMap<String, String>()
                }

                val headersBuilder = Headers.Builder()
                headersMap.forEach { (k: String, v: String) ->
                    headersBuilder.add(k, v)
                }

                val requestBody = if (req.body.isNotEmpty()) {
                    req.body.toRequestBody(null)
                } else null

                var primarySuccess = false

                for ((index, base) in outputUrls.withIndex()) {
                    try {
                        val request = Request.Builder()
                            .url("$base/${req.path.trimStart('/')}")
                            .method(req.method, requestBody)
                            .headers(headersBuilder.build())
                            .build()

                        val response = okHttp.newCall(request).execute()
                        val success = response.code in 200..299

                        if (index == 0) primarySuccess = success

                        Logger.d(
                            "${if (success) "✅" else "⚠️"} Synced ${req.path} → $base (${response.code})"
                        )
                        response.close()
                    } catch (e: Exception) {
                        Logger.e("Error syncing pending request to $base: ${req.path}", e)
                    }
                }

                if (primarySuccess) {
                    withContext(Dispatchers.IO) {
                        db.pendingRequestDao().deleteById(req.id)
                    }
                    synced++
                }

            } catch (e: Exception) {
                Logger.e("Error syncing pending request: ${req.path}", e)
            }
        }

        return synced
    }

    /**
     * Sincroniza los eventos de reposición consolidados enviándolos a todos los paneles configurados.
     * Usa la ruta correcta según el panel destino:
     *   - CoffeeJi → api/coffee/device/replenishSubmit
     *   - Powerbox  → api/replenishment
     */
    private suspend fun syncReplenishmentEvents(): Int {
        val reps = withContext(Dispatchers.IO) {
            db.replenishmentEventDao().allUnsent()
        }

        val offlineTx = withContext(Dispatchers.IO) {
            db.offlineTransactionDao().getPendingTransactions()
        }

        if (reps.isEmpty() && offlineTx.isEmpty()) {
            Logger.d("✅ No unsent replenishment events or offline transactions")
            return 0
        }

        Logger.d("🔄 SyncWorker: Found ${reps.size} replenishment events + ${offlineTx.size} offline transactions")

        val allDeltas = mutableMapOf<String, Int>()

        reps.forEach { rep ->
            allDeltas[rep.productId] = (allDeltas[rep.productId] ?: 0) + rep.deltaQty
        }

        offlineTx.forEach { tx ->
            allDeltas[tx.materialId] = (allDeltas[tx.materialId] ?: 0) + tx.replenishQt
        }

        if (allDeltas.isEmpty()) {
            Logger.d("✅ No deltas to sync after consolidation")
            return 0
        }

        Logger.d("📊 Consolidated ${allDeltas.size} materials with deltas")

        val deviceId = getDeviceId()
        if (deviceId.isNullOrEmpty()) {
            Logger.e("❌ Cannot sync: deviceId not available")
            return 0
        }

        val items = mutableListOf<Map<String, Any>>()
        for ((materialId, totalDelta) in allDeltas) {
            items.add(mapOf(
                "id" to materialId,
                "replenishQt" to totalDelta
            ))
            Logger.d("📦 Syncing material $materialId with consolidated delta: $totalDelta")
        }

        val payload = mapOf(
            "deviceId" to deviceId,
            "items" to items
        )

        val authorizationHeader = getStoredAuthHeader() ?: "Basic c2FiZXI6c2FiZXJfc2VjcmV0"
        val bladeAuthHeader = getStoredBladeAuthHeader() ?: ""
        val payloadJson = mapper.writeValueAsString(payload)
        Logger.d("🔍 Consolidated payload: $payloadJson")

        val outputUrls = ServerConfig.getOutputUrls(applicationContext)
        var primarySuccess = false

        for ((index, base) in outputUrls.withIndex()) {
            try {
                // Ruta de reposición según el panel destino
                val replenishPath = if (base.contains("powerbox")) {
                    "api/replenishment"                  // Ruta del panel Powerbox (Laravel)
                } else {
                    "api/coffee/device/replenishSubmit"  // Ruta original CoffeeJi
                }

                Logger.d("📤 SyncWorker: Posting ${items.size} items to $base/$replenishPath")

                val request = Request.Builder()
                    .url("$base/$replenishPath")
                    .post(payloadJson.toRequestBody(null))
                    .header("Authorization", authorizationHeader)
                    .apply {
                        if (bladeAuthHeader.isNotEmpty()) {
                            header("blade-auth", bladeAuthHeader)
                        }
                    }
                    .header("Content-Type", "application/json")
                    .build()

                val response = okHttp.newCall(request).execute()
                val success = response.code in 200..299

                if (index == 0) primarySuccess = success

                if (success) {
                    Logger.d("✅ SyncWorker: ${items.size} items sent to $base (code: ${response.code})")
                } else {
                    val responseBody = response.body?.string() ?: ""
                    Logger.d("⚠️ SyncWorker: $base rejected (code: ${response.code}, body: $responseBody)")
                }

                response.close()
            } catch (e: Exception) {
                Logger.e("❌ SyncWorker: Error syncing replenishments to $base", e)
            }
        }

        return if (primarySuccess) {
            withContext(Dispatchers.IO) {
                reps.forEach { rep ->
                    db.replenishmentEventDao().markAsSentSuspend(rep.id)
                }
                offlineTx.forEach { tx ->
                    db.offlineTransactionDao().markAsSynced(tx.id, System.currentTimeMillis())
                }
            }
            Logger.d("✅ SyncWorker: ${items.size} consolidated items marked as synced")
            reps.size + offlineTx.size
        } else {
            0
        }
    }

    private suspend fun cleanupSyncedStockStates() {
        withContext(Dispatchers.IO) {
            val allSentEvents = db.replenishmentEventDao().allSentSuspend()

            if (allSentEvents.isEmpty()) return@withContext

            Logger.d("📊 Cleanup: Processing ${allSentEvents.size} sent events")

            val syncedDeltas = allSentEvents
                .groupBy { it.productId }
                .mapValues { (_, events) ->
                    events.sumOf { it.deltaQty }
                }

            syncedDeltas.forEach { (productId: String, syncedDelta: Int) ->
                val state = db.stockStateDao().byId(productId)
                if (state != null) {
                    val newServerQty = state.serverQty + syncedDelta
                    db.stockStateDao().updateServerQty(productId, newServerQty)
                    db.stockStateDao().resetLocalDelta(productId)

                    Logger.d("✅ Synced product $productId: serverQty = $newServerQty, localDelta = 0")
                } else {
                    Logger.d("⚠️ No stock state found for $productId during cleanup")
                }
            }

            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            db.replenishmentEventDao().deleteOldSent(weekAgo)
        }
    }

    private fun getStoredAuthHeader(): String? {
        return try {
            val prefs = applicationContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.getString("authorization", null)
        } catch (_: Throwable) {
            null
        }
    }

    private fun getStoredBladeAuthHeader(): String? {
        return try {
            val prefs = applicationContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.getString("blade-auth", null)
        } catch (_: Throwable) {
            null
        }
    }

    private fun getDeviceId(): String? {
        return try {
            val prefs = applicationContext.getSharedPreferences("device", Context.MODE_PRIVATE)
            prefs.getString("deviceId", null)
        } catch (_: Throwable) {
            null
        }
    }
}
