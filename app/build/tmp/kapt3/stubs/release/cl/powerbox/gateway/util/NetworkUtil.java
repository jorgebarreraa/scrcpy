package cl.powerbox.gateway.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\nB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\t\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a8\u0006\u000b"}, d2 = {"Lcl/powerbox/gateway/util/NetworkUtil;", "", "()V", "getConnectionType", "Lcl/powerbox/gateway/util/NetworkUtil$ConnectionType;", "context", "Landroid/content/Context;", "isOnline", "", "isOnlineFallback", "ConnectionType", "app_release"})
public final class NetworkUtil {
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.NetworkUtil INSTANCE = null;
    
    private NetworkUtil() {
        super();
    }
    
    /**
     * ✅ CRÍTICO: Verifica conectividad REAL a internet
     * Usa NetworkMonitor que hace ping a servidores externos
     * NO confía solo en NET_CAPABILITY_VALIDATED
     */
    public final boolean isOnline(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Método fallback en caso de error con NetworkMonitor
     */
    private final boolean isOnlineFallback(android.content.Context context) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final cl.powerbox.gateway.util.NetworkUtil.ConnectionType getConnectionType(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcl/powerbox/gateway/util/NetworkUtil$ConnectionType;", "", "(Ljava/lang/String;I)V", "NONE", "WIFI", "MOBILE", "ETHERNET", "UNKNOWN", "app_release"})
    public static enum ConnectionType {
        /*public static final*/ NONE /* = new NONE() */,
        /*public static final*/ WIFI /* = new WIFI() */,
        /*public static final*/ MOBILE /* = new MOBILE() */,
        /*public static final*/ ETHERNET /* = new ETHERNET() */,
        /*public static final*/ UNKNOWN /* = new UNKNOWN() */;
        
        ConnectionType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<cl.powerbox.gateway.util.NetworkUtil.ConnectionType> getEntries() {
            return null;
        }
    }
}