package cl.powerbox.gateway.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.ServerConfig
import cl.powerbox.gateway.wireguard.WireGuardManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class HeartbeatWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private fun panelPrefix(url: String, outgoing: Boolean): String {
        val isOriginal = url.contains("coffeeji.com")
        val isPowerbox = url.contains("powerboxchile.cl")
        return when {
            outgoing && isOriginal  -> "[EPO]"
            outgoing && isPowerbox  -> "[EPP]"
            !outgoing && isOriginal -> "[SPO]"
            !outgoing && isPowerbox -> "[SPP]"
            else                    -> if (outgoing) "[EP?]" else "[SP?]"
        }
    }

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val outputUrls = ServerConfig.getOutputUrls(ctx)

        val prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val authHeader = prefs.getString("authorization", null)
        val bladeAuth = prefs.getString("blade-auth", null)

        if (authHeader == null) {
            Logger.d("[CFG] HeartbeatWorker: sin token almacenado, omitiendo heartbeat")
            return Result.success()
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        // Incluir estado WireGuard en el heartbeat
        val dao         = AppDatabase.get(ctx).machineConfigDao()
        val wgIp        = dao.getValue(WireGuardManager.KEY_WG_ASSIGNED_IP) ?: ""
        val wgConnected = WireGuardManager.isConnected()
        val bodyJson    = """{"wg_ip":"$wgIp","wg_connected":$wgConnected}"""
        val body        = bodyJson.toRequestBody("application/json".toMediaType())

        outputUrls.forEach { baseUrl ->
            val url = "$baseUrl/api/coffee/api/device/heartbeat"
            val t0 = System.currentTimeMillis()
            try {
                Logger.d("${panelPrefix(url, true)} ❤️ HEARTBEAT POST $url")

                val reqBuilder = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .post(body)

                bladeAuth?.let { reqBuilder.addHeader("blade-auth", it) }

                val response = client.newCall(reqBuilder.build()).execute()
                val latency = System.currentTimeMillis() - t0
                val responseBody = response.body?.string()?.let {
                    if (it.length > 300) it.take(300) + "..." else it
                } ?: ""

                Logger.d("${panelPrefix(url, false)} ❤️ HEARTBEAT ${response.code} | latency: ${latency}ms | body: $responseBody")
                response.close()
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - t0
                Logger.e("${panelPrefix(url, false)} ❤️ HEARTBEAT FALLO $url | latency: ${latency}ms | ${e.message}")
            }
        }

        return Result.success()
    }
}
