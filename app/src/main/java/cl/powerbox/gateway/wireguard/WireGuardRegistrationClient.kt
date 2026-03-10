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
 * Campos alineados con la tabla MySQL `vending_machines`:
 *   device_no      → android_id (identificador único del hardware)
 *   device_ext_no  → machine_number visible (ej: "E00731")
 *   device_name    → nickname / nombre de la máquina
 *
 * POST /gateway-api/register_wireguard.php
 * Body: { device_no, device_ext_no, device_name, public_key }
 * Response: { wireguard_ip, server_public_key, server_endpoint, dns }
 */
object WireGuardRegistrationClient {

    data class RegistrationRequest(
        val device_no: String,       // android_id → PRIMARY identifier en vending_machines
        val device_ext_no: String,   // machine_number visible (E00731, 001, etc.)
        val device_name: String,     // nombre / nickname de la máquina
        val public_key: String       // clave pública WireGuard (Curve25519, base64)
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RegistrationResponse(
        val wireguard_ip: String,       // IP asignada en el túnel, e.g. "10.99.0.5/32"
        val server_public_key: String,  // clave pública del servidor WireGuard
        val server_endpoint: String,    // host:puerto del servidor, e.g. "vpn.powerboxchile.cl:51820"
        val dns: String = "1.1.1.1"
    )

    private val mapper = jacksonObjectMapper()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun register(
        registerUrl: String,
        request: RegistrationRequest
    ): RegistrationResponse? {
        val json = mapper.writeValueAsString(request)
        val body = json.toRequestBody("application/json".toMediaType())

        Logger.d("[WG] POST $registerUrl | device=${request.device_ext_no} (${request.device_no.take(8)}...)")

        return try {
            val response = client.newCall(
                Request.Builder().url(registerUrl).post(body).build()
            ).execute()

            val bodyStr = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val result = mapper.readValue(bodyStr, RegistrationResponse::class.java)
                Logger.i("[WG] ✅ Registro exitoso → IP=${result.wireguard_ip} | endpoint=${result.server_endpoint}")
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
