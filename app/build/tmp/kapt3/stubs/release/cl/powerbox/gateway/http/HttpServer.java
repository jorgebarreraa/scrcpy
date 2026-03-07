package cl.powerbox.gateway.http;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\u0016\u0010\u0013\u001a\u00020\u00122\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015H\u0002J\u0016\u0010\u0017\u001a\u00020\u00122\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u0015H\u0002J\u0016\u0010\u001a\u001a\u00020\u00122\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0015H\u0002J\b\u0010\u001d\u001a\u00020\u0012H\u0002J\u0006\u0010\u001e\u001a\u00020\u001fJ\u0006\u0010 \u001a\u00020!J\u0006\u0010\"\u001a\u00020!R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\nR\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcl/powerbox/gateway/http/HttpServer;", "", "ctx", "Landroid/content/Context;", "(Landroid/content/Context;)V", "dateFormat", "Ljava/text/SimpleDateFormat;", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "getDb", "()Lcl/powerbox/gateway/data/AppDatabase;", "db$delegate", "Lkotlin/Lazy;", "engine", "Lio/ktor/server/engine/ApplicationEngine;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "buildDebugPanelHtml", "", "buildPendingRequestsHtml", "requests", "", "Lcl/powerbox/gateway/data/entity/PendingRequest;", "buildReplenishmentsHtml", "events", "Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;", "buildStockHtml", "stockList", "Lcl/powerbox/gateway/data/entity/StockState;", "buildSystemStatusHtml", "isRunning", "", "start", "", "stop", "app_release"})
public final class HttpServer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.Nullable()
    private io.ktor.server.engine.ApplicationEngine engine;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    @org.jetbrains.annotations.NotNull()
    private final java.text.SimpleDateFormat dateFormat = null;
    
    public HttpServer(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    private final cl.powerbox.gateway.data.AppDatabase getDb() {
        return null;
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
    
    public final boolean isRunning() {
        return false;
    }
    
    private final java.lang.String buildDebugPanelHtml() {
        return null;
    }
    
    private final java.lang.String buildSystemStatusHtml() {
        return null;
    }
    
    private final java.lang.String buildStockHtml(java.util.List<cl.powerbox.gateway.data.entity.StockState> stockList) {
        return null;
    }
    
    private final java.lang.String buildPendingRequestsHtml(java.util.List<cl.powerbox.gateway.data.entity.PendingRequest> requests) {
        return null;
    }
    
    private final java.lang.String buildReplenishmentsHtml(java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> events) {
        return null;
    }
}