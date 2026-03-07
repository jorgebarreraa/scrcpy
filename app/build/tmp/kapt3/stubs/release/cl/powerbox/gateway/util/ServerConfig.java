package cl.powerbox.gateway.util;

/**
 * Gestión de configuración de paneles para el Gateway
 *
 * Permite configurar:
 * - Panel de entrada (fuente de lectura): Panel 1 o Panel 2
 * - Paneles de salida (destinos de escritura): Panel 1, Panel 2, o ambos
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000f\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\u0011J\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\u00152\u0006\u0010\u0010\u001a\u00020\u0011J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0019\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001d\u001a\u00020\u0004J\u0016\u0010\u001e\u001a\u00020\u001b2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020\u0006J\u0016\u0010 \u001a\u00020\u001b2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcl/powerbox/gateway/util/ServerConfig;", "", "()V", "DEFAULT_INPUT_SOURCE", "", "DEFAULT_OUTPUT_PANEL_1", "", "DEFAULT_OUTPUT_PANEL_2", "KEY_INPUT_SOURCE", "", "KEY_OUTPUT_PANEL_1", "KEY_OUTPUT_PANEL_2", "PANEL_1_URL", "PANEL_2_URL", "PREFS_NAME", "getConfigSummary", "ctx", "Landroid/content/Context;", "getInputSource", "getInputSourceUrl", "getOutputUrls", "", "getPrefs", "Landroid/content/SharedPreferences;", "isOutputPanel1Enabled", "isOutputPanel2Enabled", "logCurrentConfig", "", "setInputSource", "panelNumber", "setOutputPanel1Enabled", "enabled", "setOutputPanel2Enabled", "app_release"})
public final class ServerConfig {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PANEL_1_URL = "https://gsvden.coffeeji.com";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PANEL_2_URL = "https://maquinas.powerboxchile.cl";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "server_config";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_INPUT_SOURCE = "input_source";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_OUTPUT_PANEL_1 = "output_panel_1";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_OUTPUT_PANEL_2 = "output_panel_2";
    private static final int DEFAULT_INPUT_SOURCE = 1;
    private static final boolean DEFAULT_OUTPUT_PANEL_1 = true;
    private static final boolean DEFAULT_OUTPUT_PANEL_2 = false;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.ServerConfig INSTANCE = null;
    
    private ServerConfig() {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs(android.content.Context ctx) {
        return null;
    }
    
    /**
     * Obtiene el panel de entrada configurado (1 o 2)
     */
    public final int getInputSource(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return 0;
    }
    
    /**
     * Establece el panel de entrada (1 o 2)
     */
    public final void setInputSource(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, int panelNumber) {
    }
    
    /**
     * Obtiene la URL del panel de entrada configurado
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInputSourceUrl(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    /**
     * Verifica si Panel 1 está habilitado para salida
     */
    public final boolean isOutputPanel1Enabled(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    /**
     * Verifica si Panel 2 está habilitado para salida
     */
    public final boolean isOutputPanel2Enabled(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    /**
     * Habilita/deshabilita Panel 1 para salida
     */
    public final void setOutputPanel1Enabled(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, boolean enabled) {
    }
    
    /**
     * Habilita/deshabilita Panel 2 para salida
     */
    public final void setOutputPanel2Enabled(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, boolean enabled) {
    }
    
    /**
     * Obtiene la lista de URLs habilitadas para salida
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getOutputUrls(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    /**
     * Obtiene un resumen de la configuración actual
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConfigSummary(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    /**
     * Registra la configuración actual en los logs
     */
    public final void logCurrentConfig(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
}