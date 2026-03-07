package cl.powerbox.gateway.update

import android.content.Context
import cl.powerbox.gateway.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Descarga el APK de actualización
 */
class UpdateDownloader(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    /**
     * Descarga el APK desde la URL especificada
     * @return File del APK descargado, o null si falló
     */
    suspend fun downloadApk(
        apkUrl: String,
        expectedMd5: String? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            Logger.d("📥 Descargando APK desde: $apkUrl")
            
            // Crear directorio temporal
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }
            
            // Limpiar APKs antiguos
            updateDir.listFiles()?.forEach { it.delete() }
            
            val apkFile = File(updateDir, "gateway-update.apk")
            
            val request = Request.Builder()
                .url(apkUrl)
                .addHeader("User-Agent", "GatewayOffline")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Logger.e("❌ Error al descargar APK: HTTP ${response.code}")
                return@withContext null
            }
            
            val body = response.body ?: run {
                Logger.e("❌ Response body vacío")
                return@withContext null
            }
            
            val totalBytes = body.contentLength()
            Logger.d("📦 Tamaño del APK: ${totalBytes / 1024 / 1024} MB")
            
            // Descargar a archivo
            FileOutputStream(apkFile).use { output ->
                val inputStream = body.byteStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var downloadedBytes = 0L
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    
                    // Log progreso cada 10%
                    if (totalBytes > 0) {
                        val progress = (downloadedBytes * 100 / totalBytes).toInt()
                        if (progress % 10 == 0) {
                            Logger.d("⬇️ Descarga: $progress%")
                        }
                    }
                }
            }
            
            Logger.d("✅ APK descargado: ${apkFile.absolutePath}")
            
            // Verificar MD5 si se proporcionó
            if (expectedMd5 != null) {
                val actualMd5 = calculateMd5(apkFile)
                if (actualMd5 != expectedMd5.lowercase()) {
                    Logger.e("❌ MD5 no coincide. Esperado: $expectedMd5, Actual: $actualMd5")
                    apkFile.delete()
                    return@withContext null
                }
                Logger.d("✅ MD5 verificado correctamente")
            }
            
            return@withContext apkFile
            
        } catch (e: Exception) {
            Logger.e("❌ Error al descargar APK", e)
            return@withContext null
        }
    }
    
    /**
     * Calcula el MD5 hash de un archivo
     */
    private fun calculateMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
