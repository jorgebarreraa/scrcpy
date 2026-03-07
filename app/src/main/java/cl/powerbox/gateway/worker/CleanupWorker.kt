package cl.powerbox.gateway.worker

import android.content.Context
import androidx.work.*
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CleanupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    // Ajusta estos límites a tu gusto
    private val TTL_DAYS = 14                   // borrar >14 días
    private val MAX_BYTES: Long = 900L * 1024 * 1024 // 900 MB

    private val db = AppDatabase.get(applicationContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val ttlMs = now - TimeUnit.DAYS.toMillis(TTL_DAYS.toLong())

            // 1) TTL
            val delTtl = db.cachedDao().deleteOlderThan(ttlMs)
            Logger.d("CleanupWorker: TTL deleted=$delTtl")

            // 2) Cap de tamaño
            var total = db.cachedDao().totalBytes()
            if (total > MAX_BYTES) {
                // borra en bloques de 100 LRU hasta quedar por debajo
                while (total > MAX_BYTES) {
                    val keys = db.cachedDao().lruKeys(100)
                    if (keys.isEmpty()) break
                    db.cachedDao().deleteByKeys(keys)
                    total = db.cachedDao().totalBytes()
                }
                Logger.d("CleanupWorker: size trimmed to ${total / (1024 * 1024)} MB")
            }
            Result.success()
        } catch (t: Throwable) {
            Logger.e("CleanupWorker error", t)
            Result.retry()
        }
    }

    companion object {
        fun schedule(ctx: Context) {
            // corre 1 vez al día, sin requerir red
            val req = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                "gateway_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }
    }
}
