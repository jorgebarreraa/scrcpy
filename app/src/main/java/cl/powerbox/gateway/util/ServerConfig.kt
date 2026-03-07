package cl.powerbox.gateway.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestión de configuración de paneles para el Gateway
 *
 * Permite configurar:
 * - Panel de entrada (fuente de lectura): Panel 1 o Panel 2
 * - Paneles de salida (destinos de escritura): Panel 1, Panel 2, o ambos
 */
object ServerConfig {

    // URLs de los paneles
    const val PANEL_1_URL = "https://gsvden.coffeeji.com"
    const val PANEL_2_URL = "https://maquinas.powerboxchile.cl"

    // Keys para SharedPreferences
    private const val PREFS_NAME = "server_config"
    private const val KEY_INPUT_SOURCE = "input_source"
    private const val KEY_OUTPUT_PANEL_1 = "output_panel_1"
    private const val KEY_OUTPUT_PANEL_2 = "output_panel_2"

    // Valores por defecto
    private const val DEFAULT_INPUT_SOURCE = 1 // Panel 1
    private const val DEFAULT_OUTPUT_PANEL_1 = true
    private const val DEFAULT_OUTPUT_PANEL_2 = false

    private fun getPrefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ==================== CONEXIÓN ENTRANTE ====================

    /**
     * Obtiene el panel de entrada configurado (1 o 2)
     */
    fun getInputSource(ctx: Context): Int {
        return getPrefs(ctx).getInt(KEY_INPUT_SOURCE, DEFAULT_INPUT_SOURCE)
    }

    /**
     * Establece el panel de entrada (1 o 2)
     */
    fun setInputSource(ctx: Context, panelNumber: Int) {
        require(panelNumber in 1..2) { "Panel number must be 1 or 2" }
        getPrefs(ctx).edit().putInt(KEY_INPUT_SOURCE, panelNumber).apply()
        Logger.i("📥 Input source cambiado a Panel $panelNumber")
    }

    /**
     * Obtiene la URL del panel de entrada configurado
     */
    fun getInputSourceUrl(ctx: Context): String {
        return when (getInputSource(ctx)) {
            1 -> PANEL_1_URL
            2 -> PANEL_2_URL
            else -> PANEL_1_URL
        }
    }

    // ==================== CONEXIÓN SALIENTE ====================

    /**
     * Verifica si Panel 1 está habilitado para salida
     */
    fun isOutputPanel1Enabled(ctx: Context): Boolean {
        return getPrefs(ctx).getBoolean(KEY_OUTPUT_PANEL_1, DEFAULT_OUTPUT_PANEL_1)
    }

    /**
     * Verifica si Panel 2 está habilitado para salida
     */
    fun isOutputPanel2Enabled(ctx: Context): Boolean {
        return getPrefs(ctx).getBoolean(KEY_OUTPUT_PANEL_2, DEFAULT_OUTPUT_PANEL_2)
    }

    /**
     * Habilita/deshabilita Panel 1 para salida
     */
    fun setOutputPanel1Enabled(ctx: Context, enabled: Boolean) {
        getPrefs(ctx).edit().putBoolean(KEY_OUTPUT_PANEL_1, enabled).apply()
        Logger.i("📤 Output Panel 1: ${if (enabled) "HABILITADO" else "DESHABILITADO"}")
    }

    /**
     * Habilita/deshabilita Panel 2 para salida
     */
    fun setOutputPanel2Enabled(ctx: Context, enabled: Boolean) {
        getPrefs(ctx).edit().putBoolean(KEY_OUTPUT_PANEL_2, enabled).apply()
        Logger.i("📤 Output Panel 2: ${if (enabled) "HABILITADO" else "DESHABILITADO"}")
    }

    /**
     * Obtiene la lista de URLs habilitadas para salida
     */
    fun getOutputUrls(ctx: Context): List<String> {
        val urls = mutableListOf<String>()

        if (isOutputPanel1Enabled(ctx)) {
            urls.add(PANEL_1_URL)
        }

        if (isOutputPanel2Enabled(ctx)) {
            urls.add(PANEL_2_URL)
        }

        // Si no hay ninguno habilitado, usar Panel 1 por defecto
        if (urls.isEmpty()) {
            urls.add(PANEL_1_URL)
        }

        return urls
    }

    // ==================== INFORMACIÓN ====================

    /**
     * Obtiene un resumen de la configuración actual
     */
    fun getConfigSummary(ctx: Context): String {
        val inputSource = getInputSource(ctx)
        val outputPanel1 = isOutputPanel1Enabled(ctx)
        val outputPanel2 = isOutputPanel2Enabled(ctx)

        return """
            📥 Entrada: Panel $inputSource (${getInputSourceUrl(ctx)})
            📤 Salida: ${when {
                outputPanel1 && outputPanel2 -> "Panel 1 + Panel 2 (Redundancia)"
                outputPanel1 -> "Panel 1 únicamente"
                outputPanel2 -> "Panel 2 únicamente"
                else -> "Ninguno (se usará Panel 1 por defecto)"
            }}
        """.trimIndent()
    }

    /**
     * Registra la configuración actual en los logs
     */
    fun logCurrentConfig(ctx: Context) {
        Logger.i("═══════════════════════════════════════")
        Logger.i("⚙️  CONFIGURACIÓN DE PANELES")
        Logger.i("═══════════════════════════════════════")
        Logger.i(getConfigSummary(ctx))
        Logger.i("═══════════════════════════════════════")
    }
}
