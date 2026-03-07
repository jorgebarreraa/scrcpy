package cl.powerbox.gateway.update;

/**
 * Encargado de consultar el servidor de actualizaciones y decidir
 * si hay una versión nueva disponible o no.
 *
 * Usa la versión real instalada (PackageManager),
 * así evitas olvidarte de actualizar constantes manualmente.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006J\b\u0010\u0007\u001a\u00020\bH\u0002J\u0014\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\b0\nH\u0002J\u0010\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcl/powerbox/gateway/update/UpdateChecker;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "checkForUpdate", "Lcl/powerbox/gateway/update/UpdateInfo;", "downloadVersionJson", "", "getCurrentVersionInfo", "Lkotlin/Pair;", "", "parseUpdateInfo", "jsonString", "Companion", "app_release"})
public final class UpdateChecker {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String VERSION_CHECK_URL = "https://powerboxchile.cl/gateway/updates/version.json";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.update.UpdateChecker.Companion Companion = null;
    
    public UpdateChecker(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Consulta el servidor (version.json) y decide:
     * - Si hay una versión nueva → devuelve UpdateInfo
     * - Si ya estás actualizado → devuelve null
     *
     * IMPORTANTE:
     * - Si hay problemas de red o parseo, LANZA una excepción.
     *  Esto permite que el caller distinga entre:
     *  - "No hay actualización"  → null
     *  - "Error de conexión"     → excepción
     */
    @kotlin.jvm.Throws(exceptionClasses = {java.lang.Exception.class})
    @org.jetbrains.annotations.Nullable()
    public final cl.powerbox.gateway.update.UpdateInfo checkForUpdate() throws java.lang.Exception {
        return null;
    }
    
    /**
     * Obtiene la versión actual instalada de la app usando PackageManager.
     * Devuelve Pair<versionCode, versionName>.
     */
    private final kotlin.Pair<java.lang.Integer, java.lang.String> getCurrentVersionInfo() {
        return null;
    }
    
    /**
     * Descarga el contenido de version.json desde el servidor.
     * Si hay error de red o HTTP != 200, lanza Exception.
     */
    @kotlin.jvm.Throws(exceptionClasses = {java.lang.Exception.class})
    private final java.lang.String downloadVersionJson() throws java.lang.Exception {
        return null;
    }
    
    /**
     * Parsea el JSON de version.json a un objeto UpdateInfo.
     */
    private final cl.powerbox.gateway.update.UpdateInfo parseUpdateInfo(java.lang.String jsonString) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcl/powerbox/gateway/update/UpdateChecker$Companion;", "", "()V", "VERSION_CHECK_URL", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}