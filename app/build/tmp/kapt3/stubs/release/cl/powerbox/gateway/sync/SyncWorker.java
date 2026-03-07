package cl.powerbox.gateway.sync;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010\u0011J\u000e\u0010\u0012\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0011J\n\u0010\u0014\u001a\u0004\u0018\u00010\bH\u0002J\n\u0010\u0015\u001a\u0004\u0018\u00010\bH\u0002J\n\u0010\u0016\u001a\u0004\u0018\u00010\bH\u0002J\u000e\u0010\u0017\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u0010\u0011J\u000e\u0010\u0019\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcl/powerbox/gateway/sync/SyncWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "REAL_BASE", "", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "okHttp", "Lokhttp3/OkHttpClient;", "cleanupSyncedStockStates", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "doWork", "Landroidx/work/ListenableWorker$Result;", "getDeviceId", "getStoredAuthHeader", "getStoredBladeAuthHeader", "syncPendingRequests", "", "syncReplenishmentEvents", "app_release"})
public final class SyncWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttp = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String REAL_BASE = "https://gsvden.coffeeji.com";
    
    public SyncWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    private final java.lang.Object syncPendingRequests(kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
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
}