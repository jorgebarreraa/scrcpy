package cl.powerbox.gateway.util;

/**
 * Observa cambios de conectividad sin usar CONNECTIVITY_ACTION (sin warnings).
 * Llama a [onOnline] únicamente cuando el dispositivo tiene NET_CAPABILITY_INTERNET.
 *
 * Uso:
 *  val nw = NetWatcher(context) { ...hacer algo cuando vuelve el Internet... }
 *  nw.start()
 *  ...
 *  nw.stop()
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB\u001b\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0006\u0010\r\u001a\u00020\u0006J\u0006\u0010\u000e\u001a\u00020\u0006R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcl/powerbox/gateway/util/NetWatcher;", "", "ctx", "Landroid/content/Context;", "onOnline", "Lkotlin/Function0;", "", "(Landroid/content/Context;Lkotlin/jvm/functions/Function0;)V", "callback", "Landroid/net/ConnectivityManager$NetworkCallback;", "onOnlineSafe", "cm", "Landroid/net/ConnectivityManager;", "start", "stop", "Companion", "app_release"})
public final class NetWatcher {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<kotlin.Unit> onOnline = null;
    
    /**
     * Guarda la referencia del callback para poder desregistrar.
     */
    @org.jetbrains.annotations.Nullable()
    private android.net.ConnectivityManager.NetworkCallback callback;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.NetWatcher.Companion Companion = null;
    
    public NetWatcher(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOnline) {
        super();
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
    
    /**
     * Invoca onOnline() únicamente si el estado actual realmente tiene INET.
     * Protegido contra NPE/errores del framework.
     */
    private final void onOnlineSafe(android.net.ConnectivityManager cm) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcl/powerbox/gateway/util/NetWatcher$Companion;", "", "()V", "isOnline", "", "ctx", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Helper: estado actual de conectividad (con INET).
         */
        public final boolean isOnline(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
            return false;
        }
    }
}