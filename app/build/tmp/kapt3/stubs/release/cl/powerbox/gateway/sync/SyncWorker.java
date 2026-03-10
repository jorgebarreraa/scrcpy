package cl.powerbox.gateway.sync;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\r\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0010\u001a\u00020\u0011H\u0096@\u00a2\u0006\u0002\u0010\u000fJ\n\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0002J\u000e\u0010\u0014\u001a\u00020\u0015H\u0096@\u00a2\u0006\u0002\u0010\u000fJ\n\u0010\u0016\u001a\u0004\u0018\u00010\u0013H\u0002J\n\u0010\u0017\u001a\u0004\u0018\u00010\u0013H\u0002J\u000e\u0010\u0018\u001a\u00020\u0019H\u0082@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u001a\u001a\u00020\u0019H\u0082@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcl/powerbox/gateway/sync/SyncWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "okHttp", "Lokhttp3/OkHttpClient;", "cleanupSyncedStockStates", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "doWork", "Landroidx/work/ListenableWorker$Result;", "getDeviceId", "", "getForegroundInfo", "Landroidx/work/ForegroundInfo;", "getStoredAuthHeader", "getStoredBladeAuthHeader", "syncPendingRequests", "", "syncReplenishmentEvents", "Companion", "app_release"})
public final class SyncWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttp = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SYNC_CHANNEL_ID = "gateway_sync_channel";
    private static final int SYNC_NOTIF_ID = 2;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.sync.SyncWorker.Companion Companion = null;
    
    public SyncWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getForegroundInfo(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ForegroundInfo> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    /**
     * Sincroniza las solicitudes pendientes enviándolas a todos los paneles de salida configurados.
     * Se elimina de la BD solo si el panel primario (primero) responde con éxito.
     */
    private final java.lang.Object syncPendingRequests(kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Sincroniza los eventos de reposición consolidados enviándolos a todos los paneles configurados.
     * Usa la ruta correcta según el panel destino:
     *  - CoffeeJi → api/coffee/device/replenishSubmit
     *  - Powerbox  → api/replenishment
     */
    private final java.lang.Object syncReplenishmentEvents(kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object cleanupSyncedStockStates(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String getStoredAuthHeader() {
        return null;
    }
    
    private final java.lang.String getStoredBladeAuthHeader() {
        return null;
    }
    
    private final java.lang.String getDeviceId() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcl/powerbox/gateway/sync/SyncWorker$Companion;", "", "()V", "SYNC_CHANNEL_ID", "", "SYNC_NOTIF_ID", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}