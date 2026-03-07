package cl.powerbox.gateway

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import cl.powerbox.gateway.http.HttpServer
import cl.powerbox.gateway.sync.SyncScheduler
import cl.powerbox.gateway.update.UpdateScheduler
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkMonitor
import cl.powerbox.gateway.worker.CleanupWorker

class GatewayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.d("🚀 GatewayApplication starting...")

        try {
            // 1. Inicializar WorkManager PRIMERO
            initializeWorkManager()

            // 2. Iniciar NetworkMonitor
            NetworkMonitor.get(this).startMonitoring()

            // 3. Iniciar HTTP Server
            HttpServer(this).start()

            // 4. Programar sincronización periódica
            SyncScheduler.schedulePeriodicSync(this)

            // 5. Programar limpieza periódica
            CleanupWorker.schedule(this)
            // 6. Programar auto-actualización (cada 1 horas)
            UpdateScheduler.scheduleUpdateCheck(this, intervalHours = 1)

            Logger.d("✅ GatewayApplication initialized successfully")
        } catch (e: Exception) {
            Logger.e("❌ Error initializing GatewayApplication", e)
        }
    }

    /**
     * Inicializa WorkManager manualmente
     */
    private fun initializeWorkManager() {
        try {
            if (!WorkManager.isInitialized()) {
                val config = Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .build()

                WorkManager.initialize(this, config)
                Logger.d("✅ WorkManager initialized")
            }
        } catch (e: Exception) {
            Logger.e("Error initializing WorkManager", e)
        }
    }
}
