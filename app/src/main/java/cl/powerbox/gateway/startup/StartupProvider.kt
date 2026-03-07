package cl.powerbox.gateway.startup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.SystemClock
import cl.powerbox.gateway.service.GatewayForegroundService

class StartupProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val app = context?.applicationContext ?: return true
        val dpCtx = app.createDeviceProtectedStorageContext()

        cl.powerbox.gateway.util.Logger.init(dpCtx)
        cl.powerbox.gateway.util.Logger.d("StartupProvider.onCreate -> starting service")

        try {
            GatewayForegroundService.start(dpCtx)
            cl.powerbox.gateway.util.Logger.d("StartupProvider -> startForegroundService OK")
        } catch (t: Throwable) {
            cl.powerbox.gateway.util.Logger.e("StartupProvider immediate start failed", t)
        }

        try {
            val am = dpCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getService(
                dpCtx, 2,
                Intent(dpCtx, GatewayForegroundService::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1500L,
                pi
            )
            cl.powerbox.gateway.util.Logger.d("StartupProvider -> Alarm fallback (+1.5s)")
        } catch (t: Throwable) {
            cl.powerbox.gateway.util.Logger.e("StartupProvider alarm failed", t)
        }

        return true
    }

    override fun query(u: Uri, p: Array<out String>?, s: String?, a: Array<out String>?, o: String?): Cursor? = null
    override fun getType(u: Uri): String? = null
    override fun insert(u: Uri, v: ContentValues?): Uri? = null
    override fun delete(u: Uri, s: String?, a: Array<out String>?): Int = 0
    override fun update(u: Uri, v: ContentValues?, s: String?, a: Array<out String>?): Int = 0
}
