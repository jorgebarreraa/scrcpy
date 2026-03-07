package cl.powerbox.gateway.update

import android.content.Context
import android.os.Build
import cl.powerbox.gateway.util.Logger
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Información de actualización obtenida desde version.json
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val mandatory: Boolean,
    val minRequiredVersion: Int,
    val md5: String? = null
)

/**
 * Encargado de consultar el servidor de actualizaciones y decidir
 * si hay una versión nueva disponible o no.
 *
 * Usa la versión real instalada (PackageManager),
 * así evitas olvidarte de actualizar constantes manualmente.
 */
class UpdateChecker(private val context: Context) {

    /**
     * Consulta el servidor (version.json) y decide:
     * - Si hay una versión nueva → devuelve UpdateInfo
     * - Si ya estás actualizado → devuelve null
     *
     * IMPORTANTE:
     * - Si hay problemas de red o parseo, LANZA una excepción.
     *   Esto permite que el caller distinga entre:
     *   - "No hay actualización"  → null
     *   - "Error de conexión"     → excepción
     */
    @Throws(Exception::class)
    fun checkForUpdate(): UpdateInfo? {
        val (currentCode, currentName) = getCurrentVersionInfo()

        Logger.d(
            "🔍 Verificando actualización... " +
                    "(actual: code=$currentCode, name=$currentName)"
        )

        val jsonString = downloadVersionJson()
        val updateInfo = parseUpdateInfo(jsonString)

        Logger.d(
            "📦 Versión remota: code=${updateInfo.versionCode}, " +
                    "name=${updateInfo.versionName}, minRequired=${updateInfo.minRequiredVersion}"
        )

        return if (updateInfo.versionCode > currentCode) {
            // Hay una versión nueva disponible
            Logger.d(
                "✅ Nueva versión disponible: ${updateInfo.versionName} " +
                        "(code=${updateInfo.versionCode}) > actual (code=$currentCode)"
            )
            updateInfo
        } else {
            // Ya está actualizado
            Logger.d(
                "✅ App ya está actualizada. " +
                        "Actual: $currentName (code=$currentCode), " +
                        "remota: ${updateInfo.versionName} (code=${updateInfo.versionCode})"
            )
            null
        }
    }

    /**
     * Obtiene la versión actual instalada de la app usando PackageManager.
     * Devuelve Pair<versionCode, versionName>.
     */
    private fun getCurrentVersionInfo(): Pair<Int, String> {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            val name = pInfo.versionName ?: "N/A"
            Pair(code, name)
        } catch (e: Exception) {
            Logger.e("Error obteniendo versión instalada de la app", e)
            // En caso extremo, devolvemos algo neutro (0, "N/A")
            Pair(0, "N/A")
        }
    }

    /**
     * Descarga el contenido de version.json desde el servidor.
     * Si hay error de red o HTTP != 200, lanza Exception.
     */
    @Throws(Exception::class)
    private fun downloadVersionJson(): String {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(VERSION_CHECK_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
            }

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Error HTTP al consultar versión: $code")
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val builder = StringBuilder()
            reader.useLines { lines ->
                lines.forEach { line ->
                    builder.append(line)
                }
            }

            val result = builder.toString()
            Logger.d("🌐 Respuesta version.json: $result")
            result
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Parsea el JSON de version.json a un objeto UpdateInfo.
     */
    private fun parseUpdateInfo(jsonString: String): UpdateInfo {
        val json = JSONObject(jsonString)

        return UpdateInfo(
            versionCode = json.getInt("versionCode"),
            versionName = json.getString("versionName"),
            apkUrl = json.getString("apkUrl"),
            changelog = json.optString("changelog", ""),
            mandatory = json.optBoolean("mandatory", false),
            minRequiredVersion = json.optInt("minRequiredVersion", 0),
            md5 = if (json.has("md5")) json.getString("md5") else null
        )
    }

    companion object {
        // URL fija donde está tu version.json
        private const val VERSION_CHECK_URL =
            "https://powerboxchile.cl/gateway/updates/version.json"
    }
}
