package cl.powerbox.gateway.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.powerbox.gateway.R
import cl.powerbox.gateway.util.Logger
import kotlinx.coroutines.delay

/**
 * Worker que verifica actualizaciones periódicamente
 * Se ejecuta cada 6 horas en background
 */
class UpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val updateChecker = UpdateChecker(context)
    private val updateDownloader = UpdateDownloader(context)
    private val rootInstaller = RootInstaller()
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "gateway_updates"
        private const val NOTIFICATION_ID = 999
    }
    
    override suspend fun doWork(): Result {
        try {
            Logger.d("═══════════════════════════════════════════════════════")
            Logger.d("🔍 Iniciando verificación de actualizaciones...")
            Logger.d("═══════════════════════════════════════════════════════")
            
            // 1. Verificar si hay acceso root
            if (!rootInstaller.hasRootAccess()) {
                Logger.e("❌ Sin acceso root - Auto-actualización deshabilitada")
                return Result.success()
            }
            
            // 2. Verificar si hay actualización disponible
            val updateInfo = updateChecker.checkForUpdate()
            
            if (updateInfo == null) {
                Logger.d("✅ No hay actualizaciones disponibles")
                return Result.success()
            }
            
            Logger.d("═══════════════════════════════════════════════════════")
            Logger.d("🆕 NUEVA VERSIÓN DISPONIBLE")
            Logger.d("   Versión: ${updateInfo.versionName} (${updateInfo.versionCode})")
            Logger.d("   URL: ${updateInfo.apkUrl}")
            Logger.d("   Obligatoria: ${updateInfo.mandatory}")
            Logger.d("   Changelog: ${updateInfo.changelog}")
            Logger.d("═══════════════════════════════════════════════════════")
            
            // 3. Mostrar notificación de inicio
            showNotification("Descargando actualización v${updateInfo.versionName}...")
            
            // 4. Descargar APK
            val apkFile = updateDownloader.downloadApk(updateInfo.apkUrl, updateInfo.md5)
            
            if (apkFile == null) {
                Logger.e("❌ Error al descargar APK")
                showNotification("Error al descargar actualización", isError = true)
                return Result.retry()
            }
            
            Logger.d("✅ APK descargado correctamente: ${apkFile.absolutePath}")
            
            // 5. Mostrar notificación de instalación
            showNotification("Instalando actualización v${updateInfo.versionName}...")
            
            // 6. Instalar silenciosamente
            val installed = rootInstaller.installSilently(apkFile)
            
            if (!installed) {
                Logger.e("❌ Error al instalar APK")
                showNotification("Error al instalar actualización", isError = true)
                return Result.retry()
            }
            
            Logger.d("✅ APK instalado exitosamente")
            
            // 7. Notificación de éxito
            showNotification("Actualización instalada v${updateInfo.versionName}", isSuccess = true)
            
            // 8. Esperar un momento antes de reiniciar
            delay(2000)
            
            // 9. Reiniciar app para aplicar cambios
            Logger.d("🔄 Reiniciando aplicación para aplicar actualización...")
            rootInstaller.restartApp()
            
            Logger.d("═══════════════════════════════════════════════════════")
            Logger.d("✅ ACTUALIZACIÓN COMPLETADA EXITOSAMENTE")
            Logger.d("═══════════════════════════════════════════════════════")
            
            return Result.success()
            
        } catch (e: Exception) {
            Logger.e("❌ Error en UpdateWorker", e)
            showNotification("Error en actualización automática", isError = true)
            return Result.retry()
        }
    }
    
    /**
     * Muestra notificación del progreso de actualización
     */
    private fun showNotification(message: String, isError: Boolean = false, isSuccess: Boolean = false) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Crear canal de notificación (Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Actualizaciones del Gateway",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notificaciones de actualización automática"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val icon = when {
                isError -> android.R.drawable.ic_dialog_alert
                isSuccess -> android.R.drawable.ic_menu_upload
                else -> android.R.drawable.stat_sys_download
            }
            
            val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle("Gateway PowerBox")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(isSuccess || isError)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            
        } catch (e: Exception) {
            Logger.e("Error mostrando notificación", e)
        }
    }
}
