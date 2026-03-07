package cl.powerbox.gateway.sync

import android.content.Context
import androidx.work.*
import cl.powerbox.gateway.worker.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * ✅ Gestor de sincronización con activación instantánea
 */
object SyncScheduler {

    private const val UNIQUE_NOW = "gateway_sync_now"
    private const val UNIQUE_PERIODIC = "gateway_sync_periodic"

    /**
     * Sincronización instantánea (expedited work)
     * Se ejecuta inmediatamente al volver online
     */
    fun syncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_NOW, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Sincronización periódica cada 15 minutos
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
