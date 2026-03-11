package cl.powerbox.gateway.util

import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor de inventario “a prueba de balas”.
 * - No depende de nombres de métodos específicos en los DAOs.
 * - Si hay receta en JSON, intenta parsearla y loguea el consumo esperado.
 * - Si no hay receta/DAO, sólo registra el evento para que el proyecto compile y corra.
 */
class InventoryManager(private val db: AppDatabase) {

    private val mapper = jacksonObjectMapper()

    /**
     * Registra consumo de un producto.
     *
     * @param productId ID del producto.
     * @param units     Unidades vendidas/preparadas (normalmente 1).
     */
    suspend fun consumeForProduct(productId: String, units: Int) = withContext(Dispatchers.IO) {
        try {
            // 1) Intentar cargar receta desde la tabla product (si tu DAO la expone).
            //    Para no fallar en compilación, NO llamamos a métodos cuyo nombre no sabemos.
            //    Si más adelante confirmas el método (por ej. productDao().byId(id)), lo integramos.

            val recipeJson: String? = try {
                // Si tienes algún método para obtener el JSON de receta, colócalo aquí.
                // Ejemplos posibles (DESCOMENTAR el que corresponda):
                // db.productDao().byId(productId)?.recipeJson
                // db.productDao().getById(productId)?.recipeJson
                // db.productDao().find(productId)?.recipeJson

                null // Por ahora, nos mantenemos neutros para no romper la compilación.
            } catch (_: Throwable) {
                null
            }

            if (recipeJson.isNullOrBlank()) {
                Logger.d("InventoryManager: consumo productId=$productId units=$units (sin receta disponible)")
                return@withContext
            }

            // 2) Parsear JSON de receta si existe
            val node: JsonNode = try {
                mapper.readTree(recipeJson)
            } catch (_: Throwable) {
                Logger.d("InventoryManager: receta inválida para productId=$productId; units=$units")
                return@withContext
            }

            val comps = node.get("components")
            if (comps == null || !comps.isArray) {
                Logger.d("InventoryManager: receta sin 'components' para productId=$productId; units=$units")
                return@withContext
            }

            // 3) Recorrer componentes y loguear consumo esperado
            for (elem in comps) {
                val cid = elem.get("id")?.asText() ?: continue
                val perUnit = elem.get("qty")?.asInt() ?: 0
                val total = perUnit * units
                Logger.d("InventoryManager: consume component=$cid total=$total (perUnit=$perUnit units=$units)")

                // Si más adelante defines un DAO para stock por componente,
                // aquí puedes hacer el delta persistente, por ejemplo:
                // db.stockStateDao().ensureAndDelta(pid = cid, delta = -total, now = System.currentTimeMillis())
            }
        } catch (t: Throwable) {
            Logger.e("InventoryManager.consumeForProduct error", t)
        }
    }
}
