package cl.powerbox.gateway.update

import cl.powerbox.gateway.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Instalador silencioso usando permisos root
 */
class RootInstaller {
    
    companion object {
        private const val PACKAGE_NAME = "cl.powerbox.gateway"
    }
    
    /**
     * Verifica si tenemos acceso root
     */
    suspend fun hasRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            writer.write("id\n")
            writer.flush()
            writer.write("exit\n")
            writer.flush()
            
            val output = reader.readLine() ?: ""
            process.waitFor()
            
            val hasRoot = output.contains("uid=0") // uid=0 significa root
            Logger.d(if (hasRoot) "✅ Acceso root disponible" else "❌ Sin acceso root")
            
            return@withContext hasRoot
        } catch (e: Exception) {
            Logger.e("❌ Error verificando root", e)
            return@withContext false
        }
    }
    
    /**
     * Instala el APK silenciosamente usando pm install con root
     * @param apkFile Archivo APK a instalar
     * @return true si la instalación fue exitosa
     */
    suspend fun installSilently(apkFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!apkFile.exists()) {
                Logger.e("❌ APK no existe: ${apkFile.absolutePath}")
                return@withContext false
            }
            
            Logger.d("🔧 Instalando APK silenciosamente: ${apkFile.absolutePath}")
            
            // Método 1: pm install -r (replace)
            val success = installViaPmInstall(apkFile)
            
            if (success) {
                Logger.d("✅ APK instalado exitosamente")
                
                // Limpiar archivo temporal
                apkFile.delete()
                
                return@withContext true
            } else {
                // Método 2: Intentar con cat (si pm install falla)
                Logger.d("⚠️ pm install falló, intentando método alternativo...")
                return@withContext installViaCat(apkFile)
            }
            
        } catch (e: Exception) {
            Logger.e("❌ Error instalando APK", e)
            return@withContext false
        }
    }
    
    /**
     * Método principal: pm install con root
     */
    private fun installViaPmInstall(apkFile: File): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            // Dar permisos de lectura al APK
            writer.write("chmod 644 ${apkFile.absolutePath}\n")
            writer.flush()
            
            // Instalar con pm install -r (replace)
            writer.write("pm install -r ${apkFile.absolutePath}\n")
            writer.flush()
            writer.write("exit\n")
            writer.flush()
            
            // Leer output
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
                Logger.d("pm output: $line")
            }
            
            val errorOutput = StringBuilder()
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.append(line).append("\n")
                Logger.e("pm error: $line")
            }
            
            process.waitFor()
            
            val success = output.contains("Success") || output.contains("SUCCESS")
            
            if (!success) {
                Logger.e("❌ pm install falló: $output")
                if (errorOutput.isNotEmpty()) {
                    Logger.e("Error stream: $errorOutput")
                }
            }
            
            return success
            
        } catch (e: Exception) {
            Logger.e("❌ Error en pm install", e)
            return false
        }
    }
    
    /**
     * Método alternativo: copiar APK a /data/local/tmp y usar pm install desde ahí
     */
    private fun installViaCat(apkFile: File): Boolean {
        try {
            val tmpPath = "/data/local/tmp/gateway_update.apk"
            
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            // Copiar APK a directorio temporal del sistema
            writer.write("cat ${apkFile.absolutePath} > $tmpPath\n")
            writer.flush()
            
            // Dar permisos
            writer.write("chmod 644 $tmpPath\n")
            writer.flush()
            
            // Instalar
            writer.write("pm install -r $tmpPath\n")
            writer.flush()
            
            // Limpiar
            writer.write("rm $tmpPath\n")
            writer.flush()
            
            writer.write("exit\n")
            writer.flush()
            
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
                Logger.d("cat output: $line")
            }
            
            process.waitFor()
            
            val success = output.contains("Success") || output.contains("SUCCESS")
            
            if (!success) {
                Logger.e("❌ Método alternativo falló: $output")
            }
            
            return success
            
        } catch (e: Exception) {
            Logger.e("❌ Error en método alternativo", e)
            return false
        }
    }
    
    /**
     * Reinicia la aplicación después de actualizar
     */
    suspend fun restartApp() = withContext(Dispatchers.IO) {
        try {
            Logger.d("🔄 Reiniciando aplicación...")
            
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            
            // Matar proceso actual
            writer.write("am force-stop $PACKAGE_NAME\n")
            writer.flush()
            
            // Esperar un momento
            Thread.sleep(1000)
            
            // Iniciar de nuevo
            writer.write("am start -n $PACKAGE_NAME/.GatewayApplication\n")
            writer.flush()
            writer.write("exit\n")
            writer.flush()
            
            process.waitFor()
            
            Logger.d("✅ Aplicación reiniciada")
            
        } catch (e: Exception) {
            Logger.e("❌ Error reiniciando app", e)
        }
    }
}
