package cl.powerbox.gateway.http;

/**
 * ✅ VERSIÓN FINAL v4.0
 *
 * CARACTERÍSTICAS:
 * - Emulación offline de respuestas GET con stock actualizado
 * - Persistencia garantizada de cambios offline
 * - Sincronización correcta de eventos pendientes
 * - Caché se reescribe inmediatamente después de cambios
 * - Detección online/offline instantánea
 * - Broadcasts a Vending después de cambios
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0010\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J$\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u00132\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010\u0017J$\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00140\u00132\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010\u0017J<\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0002\u0010\u001eJ\u0012\u0010\u001f\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0015\u001a\u00020\u0014H\u0002JB\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u00132\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0002\u0010\u001eJ<\u0010\"\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0086@\u00a2\u0006\u0002\u0010\u001eJ<\u0010$\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0002\u0010\u001eJ<\u0010%\u001a\u00020#2\u0006\u0010&\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0002\u0010\u001eJ\"\u0010\'\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0002J\u0010\u0010(\u001a\u00020)2\u0006\u0010\u0015\u001a\u00020\u0014H\u0002J\u0010\u0010*\u001a\u00020)2\u0006\u0010\u0015\u001a\u00020\u0014H\u0002J\u0010\u0010+\u001a\u00020)2\u0006\u0010\u0015\u001a\u00020\u0014H\u0002JR\u0010,\u001a\u00020\u001a2\u0006\u0010-\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\b\u0010.\u001a\u0004\u0018\u00010\u00142\u0006\u0010/\u001a\u0002002\b\u00101\u001a\u0004\u0018\u00010\u00142\u0006\u00102\u001a\u0002032\u0006\u00104\u001a\u00020)H\u0082@\u00a2\u0006\u0002\u00105J\u000e\u00106\u001a\u00020\u001aH\u0082@\u00a2\u0006\u0002\u00107J\u001c\u00108\u001a\u00020\u001a2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001dH\u0002J\u0012\u00109\u001a\u00020\u001a2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010H\u0002J\u0010\u0010:\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u0014H\u0002J\u001a\u0010;\u001a\u00020\u00142\u0006\u0010<\u001a\u00020\u00142\b\b\u0002\u0010=\u001a\u000200H\u0002JD\u0010>\u001a\u00020!2\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u001d2\b\u0010\u0016\u001a\u0004\u0018\u00010\u00102\u0006\u0010?\u001a\u00020\u0014H\u0082@\u00a2\u0006\u0002\u0010@J\u0016\u0010A\u001a\u00020\u001a2\u0006\u0010\u0011\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010BR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006C"}, d2 = {"Lcl/powerbox/gateway/http/ProxyHandler;", "", "ctx", "Landroid/content/Context;", "(Landroid/content/Context;)V", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "offlineEmulator", "Lcl/powerbox/gateway/http/OfflineResponseEmulator;", "offlineTransactionSync", "Lcl/powerbox/gateway/sync/OfflineTransactionSync;", "ok", "Lokhttp3/OkHttpClient;", "applyEffectiveValuesToResponse", "", "src", "applyOrderStockDecrementLocally", "", "", "path", "body", "(Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "applyReplenishmentLocally", "enqueuePending", "", "method", "headers", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractDeviceId", "forwardToAllOutputs", "Lokhttp3/Response;", "handle", "Lcl/powerbox/gateway/http/ProxyResult;", "handleOffline", "handleOfflineOrForward", "upper", "hashKey", "isCriticalOrderEndpoint", "", "isReplenishPost", "isStockListEndpoint", "logTraffic", "direction", "requestBody", "responseStatus", "", "responseBody", "durationMs", "", "isOnline", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "rewriteAllStockCachesWithEffectiveValues", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "storeAuthHeaders", "storeDeviceId", "successCritical", "truncate", "s", "max", "tryForwardAndReturn", "baseUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;[BLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateServerStockQuantitiesCarefully", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class ProxyHandler {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.sync.OfflineTransactionSync offlineTransactionSync = null;
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.http.OfflineResponseEmulator offlineEmulator = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient ok = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    
    public ProxyHandler(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    private final boolean isStockListEndpoint(java.lang.String path) {
        return false;
    }
    
    private final boolean isCriticalOrderEndpoint(java.lang.String path) {
        return false;
    }
    
    private final boolean isReplenishPost(java.lang.String path) {
        return false;
    }
    
    private final java.lang.String hashKey(java.lang.String method, java.lang.String path, byte[] body) {
        return null;
    }
    
    private final java.lang.String extractDeviceId(java.lang.String path) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object handle(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String method, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> headers, @org.jetbrains.annotations.Nullable()
    byte[] body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super cl.powerbox.gateway.http.ProxyResult> $completion) {
        return null;
    }
    
    private final java.lang.Object logTraffic(java.lang.String direction, java.lang.String method, java.lang.String path, java.lang.String requestBody, int responseStatus, java.lang.String responseBody, long durationMs, boolean isOnline, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String truncate(java.lang.String s, int max) {
        return null;
    }
    
    private final java.lang.Object handleOffline(java.lang.String path, java.lang.String method, java.util.Map<java.lang.String, java.lang.String> headers, byte[] body, kotlin.coroutines.Continuation<? super cl.powerbox.gateway.http.ProxyResult> $completion) {
        return null;
    }
    
    private final java.lang.Object handleOfflineOrForward(java.lang.String upper, java.lang.String path, java.util.Map<java.lang.String, java.lang.String> headers, byte[] body, kotlin.coroutines.Continuation<? super cl.powerbox.gateway.http.ProxyResult> $completion) {
        return null;
    }
    
    /**
     * Reenvía la petición a todos los destinos de salida configurados
     * Retorna la lista de respuestas (una por cada destino)
     */
    private final java.lang.Object forwardToAllOutputs(java.lang.String method, java.lang.String path, java.util.Map<java.lang.String, java.lang.String> headers, byte[] body, kotlin.coroutines.Continuation<? super java.util.List<okhttp3.Response>> $completion) {
        return null;
    }
    
    /**
     * Reenvía una petición a un servidor específico
     */
    private final java.lang.Object tryForwardAndReturn(java.lang.String method, java.lang.String path, java.util.Map<java.lang.String, java.lang.String> headers, byte[] body, java.lang.String baseUrl, kotlin.coroutines.Continuation<? super okhttp3.Response> $completion) {
        return null;
    }
    
    private final java.lang.Object enqueuePending(java.lang.String path, java.lang.String method, java.util.Map<java.lang.String, java.lang.String> headers, byte[] body, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object updateServerStockQuantitiesCarefully(byte[] src, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object rewriteAllStockCachesWithEffectiveValues(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final byte[] applyEffectiveValuesToResponse(byte[] src) {
        return null;
    }
    
    private final java.lang.Object applyReplenishmentLocally(java.lang.String path, byte[] body, kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    private final java.lang.Object applyOrderStockDecrementLocally(java.lang.String path, byte[] body, kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    private final cl.powerbox.gateway.http.ProxyResult successCritical(java.lang.String path) {
        return null;
    }
    
    private final void storeAuthHeaders(java.util.Map<java.lang.String, java.lang.String> headers) {
    }
    
    private final void storeDeviceId(byte[] body) {
    }
}