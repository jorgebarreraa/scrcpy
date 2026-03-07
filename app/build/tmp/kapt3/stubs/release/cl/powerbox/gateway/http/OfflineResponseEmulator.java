package cl.powerbox.gateway.http;

/**
 * Emula respuestas de endpoints GET en modo offline con stock actualizado
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\fJ\u001e\u0010\r\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\u000e\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcl/powerbox/gateway/http/OfflineResponseEmulator;", "", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "(Lcl/powerbox/gateway/data/AppDatabase;)V", "TAG", "", "emulateDeviceAllInfo", "cachedResponse", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "emulateListTypeAllMaterial", "deviceId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "emulateReplenishList", "emulateWithoutPage", "app_release"})
public final class OfflineResponseEmulator {
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "OfflineResponseEmulator";
    
    public OfflineResponseEmulator(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.AppDatabase db) {
        super();
    }
    
    /**
     * Emula la respuesta de replenishList con stock actualizado
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object emulateReplenishList(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @org.jetbrains.annotations.NotNull()
    java.lang.String cachedResponse, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Emula la respuesta de listTypeAllMaterial con stock actualizado
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object emulateListTypeAllMaterial(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @org.jetbrains.annotations.NotNull()
    java.lang.String cachedResponse, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Emula la respuesta de deviceAllInfo (sin modificaciones de stock)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object emulateDeviceAllInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String cachedResponse, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Emula la respuesta de withoutPage (sin modificaciones de stock)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object emulateWithoutPage(@org.jetbrains.annotations.NotNull()
    java.lang.String cachedResponse, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
}