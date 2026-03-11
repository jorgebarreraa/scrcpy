package cl.powerbox.gateway.http

import cl.powerbox.gateway.data.AppDatabase
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Emula respuestas de endpoints GET en modo offline con stock actualizado
 */
class OfflineResponseEmulator(private val db: AppDatabase) {

    private val TAG = "OfflineResponseEmulator"

    /**
     * Emula la respuesta de replenishList con stock actualizado
     */
    suspend fun emulateReplenishList(deviceId: String, cachedResponse: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Emulating replenishList for device: $deviceId")
                val jsonResponse = JSONObject(cachedResponse)
                val dataArray = jsonResponse.getJSONArray("data")

                for (i in 0 until dataArray.length()) {
                    val material = dataArray.getJSONObject(i)
                    val materialId = material.optString("materialId", "")
                    if (materialId.isEmpty()) continue

                    val stockState = db.stockStateDao().byId(materialId)

                    if (stockState != null) {
                        val effectiveStock = stockState.serverQty + stockState.localDelta
                        material.put("residueQty", effectiveStock)

                        Log.d(TAG, "📦 Material $materialId: residueQty=$effectiveStock (server=${stockState.serverQty} + delta=${stockState.localDelta})")
                    } else {
                        // Si no hay estado local, usar el valor del cache
                        val cachedQty = material.optInt("residueQty", 0)
                        Log.d(TAG, "📦 Material $materialId: usando cache residueQty=$cachedQty")
                    }
                }

                val result = jsonResponse.toString()
                Log.d(TAG, "✅ Emulated replenishList response ready")
                return@withContext result

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error emulating replenishList", e)
                return@withContext cachedResponse
            }
        }
    }

    /**
     * Emula la respuesta de listTypeAllMaterial con stock actualizado
     */
    suspend fun emulateListTypeAllMaterial(deviceId: String, cachedResponse: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Emulating listTypeAllMaterial for device: $deviceId")
                val jsonResponse = JSONObject(cachedResponse)
                val dataArray = jsonResponse.getJSONArray("data")

                for (i in 0 until dataArray.length()) {
                    val material = dataArray.getJSONObject(i)
                    val materialId = material.optString("id", "")
                    if (materialId.isEmpty()) continue

                    val stockState = db.stockStateDao().byId(materialId)

                    if (stockState != null && material.has("residueQty")) {
                        val effectiveStock = stockState.serverQty + stockState.localDelta
                        material.put("residueQty", effectiveStock)
                        Log.d(TAG, "📦 Material $materialId: updated residueQty=$effectiveStock")
                    }
                }

                return@withContext jsonResponse.toString()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error emulating listTypeAllMaterial", e)
                return@withContext cachedResponse
            }
        }
    }

    /**
     * Emula la respuesta de deviceAllInfo (sin modificaciones de stock)
     */
    suspend fun emulateDeviceAllInfo(cachedResponse: String): String {
        return cachedResponse
    }

    /**
     * Emula la respuesta de withoutPage (sin modificaciones de stock)
     */
    suspend fun emulateWithoutPage(cachedResponse: String): String {
        return cachedResponse
    }
}