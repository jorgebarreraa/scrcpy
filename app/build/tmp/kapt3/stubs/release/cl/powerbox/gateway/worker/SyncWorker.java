package cl.powerbox.gateway.worker;

/**
 * ✅ VERSIÓN CORREGIDA - NO DUPLICA STOCK
 *
 * FIX PRINCIPAL: Después de sincronizar replenishments:
 * 1. NO actualiza serverQty automáticamente
 * 2. Marca eventos como sent PRIMERO
 * 3. LUEGO hace PULL del stock real del servidor
 * 4. Reescribe caches con valores efectivos
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u0000 %2\u00020\u0001:\u0002%&B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u000e\u0010\u0012\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u0012\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0018\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0016\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0015H\u0082@\u00a2\u0006\u0002\u0010\u001fJ\u0016\u0010 \u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0015H\u0082@\u00a2\u0006\u0002\u0010\u001fJ\u001e\u0010!\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00152\u0006\u0010\"\u001a\u00020\u0019H\u0082@\u00a2\u0006\u0002\u0010#J\u0010\u0010$\u001a\u00020\u00172\u0006\u0010\u0016\u001a\u00020\u0017H\u0002R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcl/powerbox/gateway/worker/SyncWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "jsonMT", "Lokhttp3/MediaType;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "ok", "Lokhttp3/OkHttpClient;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getForegroundInfo", "Landroidx/work/ForegroundInfo;", "getJson", "Lcom/fasterxml/jackson/databind/JsonNode;", "path", "", "postJson", "", "body", "", "saveConfig", "", "node", "(Lcom/fasterxml/jackson/databind/JsonNode;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveProducts", "saveStockToServerQtyAndCleanupDeltas", "hadReplenishments", "(Lcom/fasterxml/jackson/databind/JsonNode;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "url", "Companion", "Endpoints", "app_release"})
public final class SyncWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient ok = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    @org.jetbrains.annotations.Nullable()
    private final okhttp3.MediaType jsonMT = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNIQUE_PERIODIC = "gateway_sync_periodic";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNIQUE_KICK_ONE = "gateway_sync_now";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNIQUE_TICKER = "gateway_sync_5m";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TICK = "tick";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SYNC_CHANNEL_ID = "gateway_sync_channel";
    private static final int SYNC_NOTIF_ID = 2;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.worker.SyncWorker.Companion Companion = null;
    
    public SyncWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context appContext, @org.jetbrains.annotations.NotNull()
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
    
    private final java.lang.String url(java.lang.String path) {
        return null;
    }
    
    private final com.fasterxml.jackson.databind.JsonNode getJson(java.lang.String path) {
        return null;
    }
    
    private final boolean postJson(java.lang.String path, byte[] body) {
        return false;
    }
    
    private final java.lang.Object saveProducts(com.fasterxml.jackson.databind.JsonNode node, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * ✅ VERSIÓN CORREGIDA:
     * - Actualiza serverQty con el valor REAL del panel
     * - Si había replenishments sincronizados, resetea localDelta a 0
     * - NO hace cálculos, confía en el valor del servidor
     */
    private final java.lang.Object saveStockToServerQtyAndCleanupDeltas(com.fasterxml.jackson.databind.JsonNode node, boolean hadReplenishments, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object saveConfig(com.fasterxml.jackson.databind.JsonNode node, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0015\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0000\u00a2\u0006\u0002\b\u000fJ\u000e\u0010\u0010\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0011\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0012\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcl/powerbox/gateway/worker/SyncWorker$Companion;", "", "()V", "KEY_TICK", "", "SYNC_CHANNEL_ID", "SYNC_NOTIF_ID", "", "UNIQUE_KICK_ONE", "UNIQUE_PERIODIC", "UNIQUE_TICKER", "enqueueTicker", "", "ctx", "Landroid/content/Context;", "enqueueTicker$app_release", "kick", "schedule", "scheduleEvery5Min", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void schedule(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final void kick(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final void scheduleEvery5Min(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final void enqueueTicker$app_release(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcl/powerbox/gateway/worker/SyncWorker$Endpoints;", "", "()V", "BASE", "", "CONFIG", "PRODUCTS", "REPL_BATCH", "SALES_BATCH", "STOCK", "app_release"})
    public static final class Endpoints {
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BASE = "https://gsvden.coffeeji.com";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String PRODUCTS = "/coffee/api/products";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String STOCK = "/coffee/api/stock";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String CONFIG = "/coffee/api/config";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String SALES_BATCH = "/coffee/api/sales/batch";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String REPL_BATCH = "/api/coffee/device/replenishSubmit";
        @org.jetbrains.annotations.NotNull()
        public static final cl.powerbox.gateway.worker.SyncWorker.Endpoints INSTANCE = null;
        
        private Endpoints() {
            super();
        }
    }
}