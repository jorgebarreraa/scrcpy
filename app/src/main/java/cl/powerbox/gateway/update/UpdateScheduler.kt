package cl.powerbox.gateway.update

import android.content.Context
import androidx.work.*
import cl.powerbox.gateway.util.Logger
import java.util.concurrent.TimeUnit

/**
 * Programador de verificaciones automáticas de actualización
 */
object UpdateScheduler {
    
    private const val UPDATE_WORK_NAME = "gateway_auto_update"
    
    /**
     * Configura la verificación automática periódica
     * Por defecto: cada 1 horas
     */
    fun scheduleUpdateCheck(context: Context, intervalHours: Long = 1) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // Solo con internet
                .setRequiresBatteryNotLow(false)                // No esperar batería
                .setRequiresCharging(false)                     // No esperar carga
                .build()
            
            val updateRequest = PeriodicWorkRequestBuilder<UpdateWorker>(
                intervalHours, TimeUnit.HOURS,
                15, TimeUnit.MINUTES  // Ventana de flex: puede ejecutarse hasta 15 min antes/después
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // No reemplazar si ya existe
                updateRequest
            )
            
            Logger.d("✅ Auto-actualización programada (cada $intervalHours horas)")
            
        } catch (e: Exception) {
            Logger.e("❌ Error programando auto-actualización", e)
        }
    }
    
    /**
     * Ejecuta una verificación inmediata (para testing)
     */
    fun checkNow(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val immediateRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(immediateRequest)
            
            Logger.d("🔍 Verificación inmediata de actualización solicitada")
            
        } catch (e: Exception) {
            Logger.e("❌ Error ejecutando verificación inmediata", e)
        }
    }
    
    /**
     * Cancela la verificación automática
     */
    fun cancelUpdateCheck(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(UPDATE_WORK_NAME)
            Logger.d("⏸️ Auto-actualización cancelada")
        } catch (e: Exception) {
            Logger.e("❌ Error cancelando auto-actualización", e)
        }
    }
    
    /**
     * Verifica si la auto-actualización está activa
     */
    fun isUpdateScheduled(context: Context): Boolean {
        return try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(UPDATE_WORK_NAME)
                .get()
            
            workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            Logger.e("Error verificando estado de auto-actualización", e)
            false
        }
    }
}
