package cl.powerbox.gateway.wireguard

import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP para registrar la máquina en el servidor WireGuard.
 *
 * El servidor debe exponer:
 *   POST /api/wireguard/register
 *   Body: { machine_number, nickname, android_id, public_key }
 *   Response: { assigned_ip, server_public_key, server_endpoint, dns }
 */
object WireGuardRegistrationClient {

    data class RegistrationRequest(
        val machine_number: String,
        val nickname: String,
        val android_id: String,
        val public_key: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RegistrationResponse(
        val assigned_ip: String,        // e.g. "10.8.0.5/32"
        val server_public_key: String,
        val server_endpoint: String,    // e.g. "vpn.powerboxchile.cl:51820"
        val dns: String = "1.1.1.1"
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ErrorResponse(val error: String = "unknown")

    private val mapper = jacksonObjectMapper()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * @param serverBaseUrl URL base del servidor, e.g. "https://vpn.powerboxchile.cl"
     * @return RegistrationResponse en éxito, null en fallo
     */
    fun register(
        serverBaseUrl: String,
        request: RegistrationRequest
    ): RegistrationResponse? {
        val url  = "$serverBaseUrl/api/wireguard/register"
        val json = mapper.writeValueAsString(request)
        val body = json.toRequestBody("application/json".toMediaType())

        Logger.d("[WG] POST $url | machine=${request.machine_number}")

        return try {
            val response = client.newCall(
                Request.Builder().url(url).post(body).build()
            ).execute()

            val bodyStr = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val result = mapper.readValue(bodyStr, RegistrationResponse::class.java)
                Logger.i("[WG] ✅ Registro exitoso → IP=${result.assigned_ip}")
                result
            } else {
                Logger.e("[WG] ❌ Registro HTTP ${response.code} → $bodyStr")
                null
            }
        } catch (e: Exception) {
            Logger.e("[WG] ❌ Error de red en registro: ${e.message}")
            null
        }
    }
}
