package cl.powerbox.gateway.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import cl.powerbox.gateway.sync.SyncScheduler
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetWatcher
import cl.powerbox.gateway.wireguard.WireGuardManager
import cl.powerbox.gateway.worker.CleanupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GatewayForegroundService : VpnService() {

    companion object {
        private const val CHANNEL_ID = "gateway_offline_channel"
        private const val NOTIF_ID = 1
        private const val PREFS = "gateway_prefs"
        private const val KEY_RUNNING = "running"

        const val ACTION_STATE_CHANGED = "cl.powerbox.gateway.STATE_CHANGED"
        const val EXTRA_RUNNING = "running"

        fun start(ctx: Context) {
            val dpCtx = ctx.applicationContext.createDeviceProtectedStorageContext()
            dpCtx.startForegroundService(Intent(dpCtx, GatewayForegroundService::class.java))
        }

        fun stop(ctx: Context) {
            val dpCtx = ctx.applicationContext.createDeviceProtectedStorageContext()
            dpCtx.stopService(Intent(dpCtx, GatewayForegroundService::class.java))
        }

        fun isRunning(ctx: Context): Boolean =
            ctx.applicationContext.createDeviceProtectedStorageContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_RUNNING, false)

        fun queryState(ctx: Context) {
            val dp = ctx.applicationContext.createDeviceProtectedStorageContext()
            val i = Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_RUNNING, isRunning(dp))
            dp.sendBroadcast(i)
        }

        private fun setRunning(ctx: Context, value: Boolean) {
            val dp = ctx.applicationContext.createDeviceProtectedStorageContext()
            dp.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_RUNNING, value).apply()
            dp.sendBroadcast(Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_RUNNING, value))
        }
    }

    private var netWatcher: NetWatcher? = null
    private val main = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        val dp = applicationContext.createDeviceProtectedStorageContext()
        Logger.init(dp)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification("Gateway Activo"))

        // El servidor HTTP ya se inició en GatewayApplication
        // Este servicio solo mantiene el proceso vivo y gestiona workers

        if (netWatcher == null) {
            initializeServices()
        }

        return START_STICKY
    }

    private fun initializeServices() {
        try {
            setRunning(this, true)

            // Programar sincronización periódica
            SyncScheduler.schedulePeriodicSync(applicationContext)

            // Programar limpieza
            CleanupWorker.schedule(applicationContext)

            // Iniciar túnel WireGuard
            CoroutineScope(Dispatchers.IO).launch {
                WireGuardManager.setup(this@GatewayForegroundService)
            }

            // Iniciar NetWatcher para detectar cuando vuelva internet
            netWatcher = NetWatcher(applicationContext) {
                Logger.d("NetWatcher: volvió internet → kick Sync")
                SyncScheduler.syncNow(applicationContext)
            }.also { it.start() }

            Logger.i("🌐 Gateway activo - escuchando en 127.0.0.1:9090")

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification("Servidor activo en 127.0.0.1:9090"))

        } catch (t: Throwable) {
            Logger.e("Gateway service initialization failed", t)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification("Error iniciando servicios (ver logs)"))
        }
    }

    override fun onDestroy() {
        try { netWatcher?.stop() } catch (_: Throwable) {}
        netWatcher = null
        CoroutineScope(Dispatchers.IO).launch {
            try { WireGuardManager.stop() } catch (_: Throwable) {}
        }

        setRunning(this, false)
        super.onDestroy()

        Logger.d("GatewayForegroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Powerbox Gateway",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(ch)
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Powerbox Gateway")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
}
