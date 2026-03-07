package cl.powerbox.gateway.http

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.CachedResponse
import cl.powerbox.gateway.data.entity.PendingRequest
import cl.powerbox.gateway.data.entity.ReplenishmentEvent
import cl.powerbox.gateway.sync.OfflineTransactionSync
import cl.powerbox.gateway.util.BroadcastHelper
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkMonitor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ProxyResult(val status: Int, val contentType: String, val body: ByteArray)

/**
 * ✅ VERSIÓN FINAL v4.0
 *
 * CARACTERÍSTICAS:
 * - Emulación offline de respuestas GET con stock actualizado
 * - Persistencia garantizada de cambios offline
 * - Sincronización correcta de eventos pendientes
 * - Caché se reescribe inmediatamente después de cambios
 * - Detección online/offline instantánea
 * - Broadcasts a Vending después de cambios
 */
class ProxyHandler(private val ctx: Context) {
    private val db = AppDatabase.get(ctx)
    private val offlineTransactionSync = OfflineTransactionSync(db)
    private val offlineEmulator = OfflineResponseEmulator(db)

    private val ok = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val mapper = jacksonObjectMapper()
    private val REAL_BASE = "https://gsvden.coffeeji.com"

    private fun isStockListEndpoint(path: String): Boolean =
        path.contains("coffee/api/device/listTypeAllMaterial") ||
                path.contains("coffee/api/device/replenishList") ||
                path.contains("coffee/api/device/deviceAllInfo") ||
                path.contains("coffee/api/goods/withoutPage")

    private fun isCriticalOrderEndpoint(path: String): Boolean =
        path.contains("coffee/api/order/genOrder") ||
                path.contains("coffee/api/order/outStockOver") ||
                path.contains("coffee/api/order/produceOver")

    private fun isReplenishPost(path: String): Boolean {
        val p = path.lowercase()
        return p.contains("replenishsubmit") ||
                p.contains("replenish_add") ||
                p.contains("device/addreplenish") ||
                p.contains("device/submitreplenish") ||
                p.contains("replenishment")
    }

    private fun hashKey(method: String, path: String, body: ByteArray?): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(method.uppercase().toByteArray())
        md.update(path.toByteArray())
        if (body != null) md.update(body)
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun extractDeviceId(path: String): String? {
        return try {
            val regex = Regex("[?&]deviceId=([^&]+)")
            regex.find(path)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun handle(
        path: String,
        method: String,
        headers: Map<String, String>,
        body: ByteArray?
    ): ProxyResult {
        return try {
            storeAuthHeaders(headers)
            storeDeviceId(body)

            if (!NetworkMonitor.isOnline()) {
                Logger.d("📍 Proxy: online=false ${method.uppercase()} $path")
                return handleOffline(path, method, headers, body)
            }

            val upper = method.uppercase()

            Logger.d("🔧 Proxy: online=true $upper $path")

            return if (upper == "POST" && isReplenishPost(path)) {
                val okResp = tryForwardAndReturn(method, path, headers, body)
                if (okResp.code in 200..299) {
                    val affectedIds = applyReplenishmentLocally(path, body ?: ByteArray(0))
                    rewriteAllStockCachesWithEffectiveValues()

                    if (affectedIds.isNotEmpty()) {
                        BroadcastHelper.notifyStockChanged(ctx, affectedIds)
                    }
                }
                ProxyResult(okResp.code, "application/json", okResp.body?.bytes() ?: ByteArray(0))
            } else {
                handleOfflineOrForward(upper, path, headers, body)
            }

        } catch (e: Exception) {
            Logger.e("❌ ProxyHandler error", e)
            ProxyResult(500, "text/plain", "Error: ${e.message}".toByteArray())
        }
    }

    // ==================== MODO OFFLINE ====================
    private suspend fun handleOffline(path: String, method: String, headers: Map<String, String>, body: ByteArray?): ProxyResult {
        try {
            // 1. RELLENOS OFFLINE
            if (method.uppercase() == "POST" && isReplenishPost(path)) {
                try {
                    val node = mapper.readTree(body ?: ByteArray(0))
                    val arr = when {
                        node.has("items") && node.get("items").isArray -> node.get("items")
                        node.has("list") && node.get("list").isArray -> node.get("list")
                        node.has("data") && node.get("data").isArray -> node.get("data")
                        node.isArray -> node
                        else -> null
                    }

                    arr?.forEach { item ->
                        val materialId = when {
                            item.has("productId") -> item.get("productId").asText()
                            item.has("id") -> item.get("id").asText()
                            item.has("materialId") -> item.get("materialId").asText()
                            else -> null
                        } ?: return@forEach

                        val qty = when {
                            item.has("replenishQt") -> item.get("replenishQt").asInt(0)
                            item.has("deltaQty") -> item.get("deltaQty").asInt(0)
                            item.has("qty") -> item.get("qty").asInt(0)
                            else -> 0
                        }

                        if (qty != 0) {
                            offlineTransactionSync.recordReplenishment(
                                deviceId = "local",
                                materialId = materialId,
                                quantity = qty
                            )
                            Logger.d("📥 Recording offline replenishment: $materialId, qty=$qty")
                        }
                    }
                } catch (t: Throwable) {
                    Logger.e("Error processing offline replenishment payload", t)
                }

                val affectedIds = applyReplenishmentLocally(path, body ?: ByteArray(0))
                rewriteAllStockCachesWithEffectiveValues()

                if (affectedIds.isNotEmpty()) {
                    BroadcastHelper.notifyStockChanged(ctx, affectedIds)
                }

                return successCritical(path)
            }

            // 2. ÓRDENES OFFLINE
            if (method.uppercase() == "POST" && isCriticalOrderEndpoint(path)) {
                enqueuePending(path, method, headers, body)
                val affectedIds = applyOrderStockDecrementLocally(path, body ?: ByteArray(0))
                rewriteAllStockCachesWithEffectiveValues()

                if (affectedIds.isNotEmpty()) {
                    BroadcastHelper.notifyStockChanged(ctx, affectedIds)
                }

                return successCritical(path)
            }

            // 3. CONSULTAS OFFLINE - Now using offline emulator
            if (method.uppercase() == "GET") {
                val cached = withContext(Dispatchers.IO) { db.cachedDao().byKey(hashKey(method.uppercase(), path, body)) }
                if (cached != null) {
                    withContext(Dispatchers.IO) { db.cachedDao().touch(cached.key, System.currentTimeMillis()) }

                    val finalBytes = if (isStockListEndpoint(path)) {
                        val deviceId = extractDeviceId(path) ?: "local"
                        val cachedStr = String(cached.bytes, Charsets.UTF_8)

                        val emulatedResponse = when {
                            path.contains("replenishList") -> {
                                offlineEmulator.emulateReplenishList(deviceId, cachedStr)
                            }
                            path.contains("listTypeAllMaterial") -> {
                                offlineEmulator.emulateListTypeAllMaterial(deviceId, cachedStr)
                            }
                            path.contains("deviceAllInfo") -> {
                                offlineEmulator.emulateDeviceAllInfo(cachedStr)
                            }
                            path.contains("withoutPage") -> {
                                offlineEmulator.emulateWithoutPage(cachedStr)
                            }
                            else -> cachedStr
                        }

                        Logger.d("🎭 Emulated offline response for: ${path.substringAfterLast("/")}")
                        emulatedResponse.toByteArray(Charsets.UTF_8)
                    } else {
                        cached.bytes
                    }

                    return ProxyResult(200, cached.contentType, finalBytes)
                }
            }

            return ProxyResult(503, "application/json", """{"error":"offline","note":"No cache available"}""".toByteArray())

        } catch (t: Throwable) {
            Logger.e("⚠️ Proxy OFFLINE ERROR: $path", t)
            return ProxyResult(503, "application/json", """{"error":"offline"}""".toByteArray())
        }
    }

    private suspend fun handleOfflineOrForward(
        upper: String,
        path: String,
        headers: Map<String, String>,
        body: ByteArray?
    ): ProxyResult {
        if (upper == "POST" && isCriticalOrderEndpoint(path)) {
            val okResp = tryForwardAndReturn(upper, path, headers, body)
            if (okResp.code in 200..299) {
                val affectedIds = applyOrderStockDecrementLocally(path, body ?: ByteArray(0))
                rewriteAllStockCachesWithEffectiveValues()
                if (affectedIds.isNotEmpty()) {
                    BroadcastHelper.notifyStockChanged(ctx, affectedIds)
                }
            }
            val bytes = okResp.body?.bytes() ?: """{"success":true}""".toByteArray()
            val ct = okResp.header("Content-Type") ?: "application/json"
            return ProxyResult(okResp.code, ct, bytes)
        }

        val resp = tryForwardAndReturn(upper, path, headers, body)
        val bytes = resp.body?.bytes() ?: ByteArray(0)
        val contentType = resp.header("Content-Type") ?: "application/json"

        if (upper == "GET" && isStockListEndpoint(path) && contentType.contains("json", true)) {
            try {
                val responseStr = String(bytes, Charsets.UTF_8)
                Logger.d("═══════════════════════════════════════════════════════════")
                Logger.d("📥 RESPONSE BODY FOR: $upper ${path.substringAfterLast("/")}")
                Logger.d("═══════════════════════════════════════════════════════════")
                Logger.d(responseStr)
                Logger.d("═══════════════════════════════════════════════════════════")
            } catch (e: Exception) {
                Logger.e("Error logging response", e)
            }
        }

        if (contentType.contains("json", true)) {
            if (upper == "GET" && isStockListEndpoint(path)) {
                updateServerStockQuantitiesCarefully(bytes)
            }

            val bytesToCache = if (upper == "GET" && isStockListEndpoint(path)) {
                applyEffectiveValuesToResponse(bytes)
            } else {
                bytes
            }

            withContext(Dispatchers.IO) {
                db.cachedDao().upsert(
                    CachedResponse(
                        key = hashKey(upper, path, body),
                        path = path,
                        method = upper,
                        bodyHash = hashKey(upper, path, body),
                        contentType = contentType,
                        bytes = bytesToCache
                    )
                )
            }
        }

        val finalBytes = if (upper == "GET" && contentType.contains("json", true) && isStockListEndpoint(path)) {
            applyEffectiveValuesToResponse(bytes)
        } else {
            bytes
        }

        return ProxyResult(resp.code, contentType, finalBytes)
    }

    // ==================== HELPERS ====================

    private suspend fun tryForwardAndReturn(
        method: String,
        path: String,
        headers: Map<String, String>,
        body: ByteArray?
    ): okhttp3.Response {
        val url = REAL_BASE + "/" + path.trimStart('/')
        val builder = Request.Builder().url(url)

        headers.forEach { (name, value) ->
            if (name.lowercase() !in listOf("host", "connection", "content-length")) {
                builder.addHeader(name, value)
            }
        }

        val reqBody: RequestBody? = body?.toRequestBody(null)
        val req = builder.method(method, reqBody).build()
        return ok.newCall(req).execute()
    }

    private suspend fun enqueuePending(
        path: String,
        method: String,
        headers: Map<String, String>,
        body: ByteArray?
    ) {
        try {
            val headersMap = headers.mapKeys { it.key.lowercase() }
            val pr = PendingRequest(
                id = UUID.randomUUID().toString(),
                path = path,
                method = method,
                headersJson = mapper.writeValueAsString(headersMap),
                body = body ?: ByteArray(0),
                clientOrderId = null,
                createdAt = System.currentTimeMillis()
            )
            withContext(Dispatchers.IO) { db.pendingRequestDao().insert(pr) }
            Logger.d("📥 Enqueued pending: $path")
        } catch (t: Throwable) {
            Logger.e("Error enqueuing pending request", t)
        }
    }

    private suspend fun updateServerStockQuantitiesCarefully(src: ByteArray) {
        try {
            val root = mapper.readTree(src)
            val now = System.currentTimeMillis()

            fun processNode(node: JsonNode) {
                val id = when {
                    node.has("productId") -> node.get("productId").asText()
                    node.has("id") -> node.get("id").asText()
                    node.has("materialId") -> node.get("materialId").asText()
                    else -> null
                } ?: return

                val qtyField = listOf("qty", "quantity", "stockNum", "materialNum", "stock", "remainNum")
                    .firstOrNull { node.has(it) } ?: return

                val serverQty = node.get(qtyField).asInt(0)

                kotlinx.coroutines.runBlocking {
                    withContext(Dispatchers.IO) {
                        val existing = db.stockStateDao().byId(id)
                        if (existing == null) {
                            db.stockStateDao().upsert(
                                cl.powerbox.gateway.data.entity.StockState(
                                    productId = id,
                                    serverQty = serverQty,
                                    localDelta = 0,
                                    lastSync = now
                                )
                            )
                        } else if (existing.localDelta == 0) {
                            db.stockStateDao().updateServerQty(id, serverQty)
                        }
                    }
                }
            }

            if (root.isArray) {
                root.forEach { processNode(it) }
            } else if (root.isObject) {
                when {
                    root.has("materials") && root.get("materials").isArray ->
                        root.get("materials").forEach { processNode(it) }
                    root.has("data") && root.get("data").isObject ->
                        processNode(root.get("data"))
                    root.has("data") && root.get("data").isArray ->
                        root.get("data").forEach { processNode(it) }
                    else -> processNode(root)
                }
            }
        } catch (t: Throwable) {
            Logger.e("Error updating server stock quantities", t)
        }
    }

    private suspend fun rewriteAllStockCachesWithEffectiveValues() {
        try {
            withContext(Dispatchers.IO) {
                val stockEndpoints = listOf(
                    "coffee/api/device/listTypeAllMaterial",
                    "coffee/api/device/deviceAllInfo",
                    "coffee/api/device/replenishList",
                    "coffee/api/goods/withoutPage"
                )

                stockEndpoints.forEach { endpoint ->
                    val key = hashKey("GET", endpoint, null)
                    val cached = db.cachedDao().byKey(key)

                    if (cached != null && cached.contentType.contains("json", true)) {
                        val updatedBytes = applyEffectiveValuesToResponse(cached.bytes)
                        db.cachedDao().upsert(cached.copy(bytes = updatedBytes))
                    }
                }
            }
        } catch (t: Throwable) {
            Logger.e("Error rewriting stock caches", t)
        }
    }

    private fun applyEffectiveValuesToResponse(src: ByteArray): ByteArray {
        return try {
            val root = mapper.readTree(src)
            val now = System.currentTimeMillis()

            fun applyOnNode(node: JsonNode) {
                val id = when {
                    node.has("productId") -> node.get("productId").asText()
                    node.has("id") -> node.get("id").asText()
                    node.has("materialId") -> node.get("materialId").asText()
                    else -> null
                } ?: return

                val qtyField = listOf("qty", "quantity", "stockNum", "materialNum", "stock", "remainNum")
                    .firstOrNull { node.has(it) } ?: return

                val state = kotlinx.coroutines.runBlocking {
                    withContext(Dispatchers.IO) {
                        db.stockStateDao().byId(id)
                    }
                }
                val effectiveQty = if (state != null) {
                    (state.serverQty + state.localDelta).coerceAtLeast(0)
                } else {
                    node.get(qtyField).asInt(0)
                }

                if (node is ObjectNode) {
                    node.put(qtyField, effectiveQty)
                    node.put("gatewayTs", now)
                    if (state != null && state.localDelta != 0) {
                        node.put("localDelta", state.localDelta)
                    }
                }
            }

            if (root.isArray) {
                root.forEach { applyOnNode(it) }
            } else if (root.isObject) {
                when {
                    root.has("materials") && root.get("materials").isArray ->
                        root.get("materials").forEach { applyOnNode(it) }
                    root.has("data") && root.get("data").isObject ->
                        applyOnNode(root.get("data"))
                    root.has("data") && root.get("data").isArray ->
                        root.get("data").forEach { applyOnNode(it) }
                    else -> applyOnNode(root)
                }
            }

            mapper.writeValueAsBytes(root)
        } catch (t: Throwable) {
            Logger.e("Error applying effective values", t)
            src
        }
    }

    private suspend fun applyReplenishmentLocally(path: String, body: ByteArray): List<String> {
        val affectedIds = mutableListOf<String>()

        try {
            val bodyStr = String(body, Charsets.UTF_8)
            Logger.d("═══════════════════════════════════════════════════════════")
            Logger.d("📤 REPLENISH REQUEST - Path: ${path.substringAfterLast("/")}")
            Logger.d("═══════════════════════════════════════════════════════════")
            Logger.d(bodyStr)
            Logger.d("═══════════════════════════════════════════════════════════")

            val node = mapper.readTree(body)
            val now = System.currentTimeMillis()
            val createdAt = node.get("createdAt")?.asLong() ?: now

            val arr = when {
                node.has("items") && node.get("items").isArray -> node.get("items")
                node.has("list") && node.get("list").isArray -> node.get("list")
                node.has("data") && node.get("data").isArray -> node.get("data")
                node.isArray -> node
                else -> null
            } ?: return affectedIds

            val toSave = mutableListOf<ReplenishmentEvent>()

            arr.forEachIndexed { _, item ->
                val id = when {
                    item.has("productId") -> item.get("productId").asText()
                    item.has("id") -> item.get("id").asText()
                    item.has("materialId") -> item.get("materialId").asText()
                    else -> null
                } ?: return@forEachIndexed

                val delta = when {
                    item.has("replenishQt") -> item.get("replenishQt").asDouble(0.0).toInt()
                    item.has("deltaQty") -> item.get("deltaQty").asInt(0)
                    item.has("qty") -> item.get("qty").asInt(0)
                    item.has("quantity") -> item.get("quantity").asInt(0)
                    item.has("num") -> item.get("num").asInt(0)
                    item.has("addNum") -> item.get("addNum").asInt(0)
                    else -> 0
                }

                if (delta == 0) return@forEachIndexed

                Logger.d("✅ Processing replenishment: id=$id, delta=$delta")

                val event = ReplenishmentEvent(
                    id = UUID.randomUUID().toString(),
                    productId = id,
                    deltaQty = delta,
                    createdAt = createdAt,
                    sent = false
                )
                toSave += event

                withContext(Dispatchers.IO) {
                    val updatedState = db.stockStateDao().ensureAndDeltaSuspend(id, delta, createdAt)
                    Logger.d("💾 Delta actualizado: id=$id, serverQty=${updatedState.serverQty}, localDelta=${updatedState.localDelta}, effective=${updatedState.serverQty + updatedState.localDelta}")
                }

                affectedIds.add(id)
            }

            if (toSave.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    db.replenishmentEventDao().upsertAllSuspend(toSave)
                    Logger.d("✅ Guardados ${toSave.size} eventos de relleno")
                }
            }
        } catch (t: Throwable) {
            Logger.e("applyReplenishmentLocally error path=$path", t)
        }

        return affectedIds
    }

    private suspend fun applyOrderStockDecrementLocally(path: String, body: ByteArray): List<String> {
        val affectedIds = mutableListOf<String>()

        try {
            val node = mapper.readTree(body)
            val now = System.currentTimeMillis()

            val products = when {
                node.has("products") && node.get("products").isArray -> node.get("products")
                node.has("items") && node.get("items").isArray -> node.get("items")
                node.has("materials") && node.get("materials").isArray -> node.get("materials")
                node.has("goodsList") && node.get("goodsList").isArray -> node.get("goodsList")
                else -> null
            }

            products?.forEach { item ->
                val productId = when {
                    item.has("productId") -> item.get("productId").asText()
                    item.has("materialId") -> item.get("materialId").asText()
                    item.has("goodsId") -> item.get("goodsId").asText()
                    item.has("id") -> item.get("id").asText()
                    else -> null
                } ?: return@forEach

                val quantity = when {
                    item.has("quantity") -> item.get("quantity").asInt(1)
                    item.has("num") -> item.get("num").asInt(1)
                    item.has("count") -> item.get("count").asInt(1)
                    else -> 1
                }

                val delta = -quantity
                withContext(Dispatchers.IO) {
                    db.stockStateDao().ensureAndDeltaSuspend(productId, delta, now)
                }

                affectedIds.add(productId)
                Logger.d("📉 STOCK DECREASED: $productId, delta=$delta")
            }

        } catch (t: Throwable) {
            Logger.e("applyOrderStockDecrementLocally error path=$path", t)
        }

        return affectedIds
    }

    private fun successCritical(path: String): ProxyResult {
        val ctJson = "application/json"
        return if (path.contains("genOrder")) {
            val orderId = "LOCAL-${UUID.randomUUID()}"
            val body = """{"code":200,"success":true,"data":{"orderId":"$orderId"}}""".toByteArray()
            ProxyResult(200, ctJson, body)
        } else {
            val body = """{"code":200,"success":true,"data":true}""".toByteArray()
            ProxyResult(200, ctJson, body)
        }
    }

    private fun storeAuthHeaders(headers: Map<String, String>) {
        try {
            val prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val editor = prefs.edit()

            headers["authorization"]?.let { editor.putString("authorization", it) }
            headers["Authorization"]?.let { editor.putString("authorization", it) }
            headers["blade-auth"]?.let { editor.putString("blade-auth", it) }

            editor.apply()
            Logger.d("✅ Auth headers stored for sync")
        } catch (_: Throwable) {
            // Silent fail
        }
    }

    private fun storeDeviceId(body: ByteArray?) {
        if (body == null) return
        try {
            val json = mapper.readTree(body)
            val deviceId = json.get("deviceId")?.asText()
            if (deviceId != null) {
                val prefs = ctx.getSharedPreferences("device", Context.MODE_PRIVATE)
                prefs.edit().putString("deviceId", deviceId).apply()
                Logger.d("✅ Device ID stored: $deviceId")
            }
        } catch (_: Throwable) {
            // Silent fail
        }
    }
}