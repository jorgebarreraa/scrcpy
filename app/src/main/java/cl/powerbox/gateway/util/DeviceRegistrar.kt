package cl.powerbox.gateway.util

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Registra la máquina vending en la BD remota de powerboxchile.cl.
 *
 * - Extrae deviceNo, deviceExtNo, nombre y tipo desde la respuesta de deviceAllInfo.
 * - Obtiene la IP pública IPv4 del dispositivo.
 * - Hace UPSERT en la BD remota via PHP endpoint.
 * - Evita duplicados: la BD usa (device_no, device_ext_no) como clave única.
 * - Actualiza la IP si cambió desde la última vez.
 */
object DeviceRegistrar {

    private const val PREFS_NAME = "gateway_device"
    private const val KEY_EXT_NO = "device_ext_no"
    private const val KEY_DEVICE_NO = "device_no"
    private const val API_URL = "https://powerboxchile.cl/gateway-api/register_device.php"

    private val mapper = jacksonObjectMapper()
    private val ok = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    /** Obtiene la IP pública IPv4 del dispositivo. */
    private suspend fun getPublicIp(): String? = withContext(Dispatchers.IO) {
        // Intento 1: api4.my-ip.io fuerza IPv4
        try {
            val resp = ok.newCall(Request.Builder().url("https://api4.my-ip.io/ip.json").build()).execute()
            val ip = mapper.readTree(resp.body?.string() ?: "").path("ip").asText()
            if (ip.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) return@withContext ip
        } catch (_: Exception) { }
        // Intento 2: ipify
        try {
            val resp = ok.newCall(Request.Builder().url("https://api.ipify.org?format=text").build()).execute()
            val ip = resp.body?.string()?.trim() ?: ""
            if (ip.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) return@withContext ip
        } catch (_: Exception) { }
        null
    }

    /**
     * Parsea la respuesta JSON de deviceAllInfo, obtiene la IP pública y registra
     * (o actualiza) el dispositivo en la BD remota.
     * Se llama en background — no bloquea la respuesta a la máquina vending.
     */
    suspend fun registerFromDeviceAllInfo(ctx: Context, responseBody: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val root = mapper.readTree(responseBody)
            if (root.path("code").asInt() != 200) return@withContext

            val deviceInfo = root.path("data").path("deviceInfo")
            val deviceType = root.path("data").path("deviceType")

            val deviceNo = deviceInfo.path("deviceNo").asText().ifBlank { return@withContext }
            val deviceExtNo = deviceInfo.path("deviceExtNo").asText().ifBlank { return@withContext }
            val deviceName = deviceInfo.path("name").asText()
            val typeName = deviceType.path("name").asText()
            val status = deviceInfo.path("status").asInt(1)

            // Guardar localmente para la UI
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EXT_NO, deviceExtNo)
                .putString(KEY_DEVICE_NO, deviceNo)
                .apply()

            val publicIp = getPublicIp()
            if (publicIp == null) {
                Logger.w("DeviceRegistrar: no se pudo obtener IP pública")
                return@withContext
            }

            Logger.i("🌍 Registrando dispositivo $deviceExtNo ($deviceNo) — IP pública: $publicIp")

            val payload = mapper.writeValueAsString(
                mapOf(
                    "device_no" to deviceNo,
                    "device_ext_no" to deviceExtNo,
                    "device_name" to deviceName,
                    "device_type" to typeName,
                    "public_ip" to publicIp,
                    "status" to status
                )
            )

            val request = Request.Builder()
                .url(API_URL)
                .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = ok.newCall(request).execute()
            if (response.isSuccessful) {
                Logger.i("✅ BD remota actualizada: $deviceExtNo → $publicIp")
            } else {
                Logger.w("⚠️ Error al registrar en BD remota: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            Logger.e("DeviceRegistrar error", e)
        }
    }

    /** Devuelve el N° externo del dispositivo guardado localmente. */
    fun getDeviceExtNo(ctx: Context): String? =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_EXT_NO, null)
}
