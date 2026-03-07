package cl.powerbox.gateway.update;

/**
 * Programador de verificaciones automáticas de actualización
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bJ\u0018\u0010\f\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcl/powerbox/gateway/update/UpdateScheduler;", "", "()V", "UPDATE_WORK_NAME", "", "cancelUpdateCheck", "", "context", "Landroid/content/Context;", "checkNow", "isUpdateScheduled", "", "scheduleUpdateCheck", "intervalHours", "", "app_release"})
public final class UpdateScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UPDATE_WORK_NAME = "gateway_auto_update";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.update.UpdateScheduler INSTANCE = null;
    
    private UpdateScheduler() {
        super();
    }
    
    /**
     * Configura la verificación automática periódica
     * Por defecto: cada 1 horas
     */
    public final void scheduleUpdateCheck(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long intervalHours) {
    }
    
    /**
     * Ejecuta una verificación inmediata (para testing)
     */
    public final void checkNow(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Cancela la verificación automática
     */
    public final void cancelUpdateCheck(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Verifica si la auto-actualización está activa
     */
    public final boolean isUpdateScheduled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
}