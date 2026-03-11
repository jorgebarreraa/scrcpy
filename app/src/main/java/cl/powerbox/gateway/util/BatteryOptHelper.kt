package cl.powerbox.gateway.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Muestra el diálogo del sistema para excluir la app de la optimización de batería
 * **una sola vez** (la primera vez que se abre la app). Si el usuario acepta, queda
 * en whitelist; si lo rechaza, no volvemos a pedirlo.
 */
object BatteryOptHelper {

    private const val PREFS = "battery_opt_prefs"
    private const val KEY_PROMPTED_ONCE = "prompted_once"

    fun maybeRequestOnce(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        // ¿Ya mostramos el diálogo alguna vez? Entonces no volvemos a molestar.
        if (prefs.getBoolean(KEY_PROMPTED_ONCE, false)) return

        val pm = activity.getSystemService(Activity.POWER_SERVICE) as PowerManager
        val pkg = activity.packageName

        // Marcamos como "ya mostrado" ANTES de lanzar el intent, así evitamos repetir
        // incluso si cierran el diálogo o lo rechazan.
        prefs.edit().putBoolean(KEY_PROMPTED_ONCE, true).apply()

        // Si ya estamos en la whitelist, no hacemos nada.
        if (pm.isIgnoringBatteryOptimizations(pkg)) return

        // Lanzamos el intent del sistema para pedir la exención.
        try {
            val i = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:$pkg"))
            activity.startActivity(i)
        } catch (_: ActivityNotFoundException) {
            // Fallback: abrir la pantalla de ajustes de optimización
            try {
                val i = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                activity.startActivity(i)
            } catch (_: Throwable) {
                // Si ni eso existe, no podemos hacer más (algunos firmwares raros)
            }
        }
    }
}
