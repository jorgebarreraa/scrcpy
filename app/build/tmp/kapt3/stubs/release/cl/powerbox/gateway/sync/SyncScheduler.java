package cl.powerbox.gateway.sync;

/**
 * ✅ Gestor de sincronización con activación instantánea
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcl/powerbox/gateway/sync/SyncScheduler;", "", "()V", "UNIQUE_NOW", "", "UNIQUE_PERIODIC", "schedulePeriodicSync", "", "context", "Landroid/content/Context;", "syncNow", "app_release"})
public final class SyncScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNIQUE_NOW = "gateway_sync_now";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNIQUE_PERIODIC = "gateway_sync_periodic";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.sync.SyncScheduler INSTANCE = null;
    
    private SyncScheduler() {
        super();
    }
    
    /**
     * Sincronización instantánea (expedited work)
     * Se ejecuta inmediatamente al volver online
     */
    public final void syncNow(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Sincronización periódica cada 15 minutos
     */
    public final void schedulePeriodicSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}