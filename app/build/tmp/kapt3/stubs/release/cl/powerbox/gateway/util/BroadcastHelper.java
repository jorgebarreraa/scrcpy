package cl.powerbox.gateway.util;

/**
 * ✅ Sistema de broadcast para notificar cambios al Vending Machine
 *
 * Eventos:
 * - Cambio de estado online/offline
 * - Cambio de stock (relleno/venta)
 * - Sincronización completada
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u001c\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00040\u0012J\u0016\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\u000fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcl/powerbox/gateway/util/BroadcastHelper;", "", "()V", "ACTION_ONLINE_STATUS", "", "ACTION_STOCK_CHANGED", "ACTION_SYNC_COMPLETE", "EXTRA_IS_ONLINE", "EXTRA_PRODUCT_IDS", "EXTRA_SYNC_SUCCESS", "notifyOnlineStatusChanged", "", "context", "Landroid/content/Context;", "isOnline", "", "notifyStockChanged", "productIds", "", "notifySyncComplete", "success", "app_release"})
public final class BroadcastHelper {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_ONLINE_STATUS = "cl.powerbox.gateway.ONLINE_STATUS_CHANGED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOCK_CHANGED = "cl.powerbox.gateway.STOCK_CHANGED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SYNC_COMPLETE = "cl.powerbox.gateway.SYNC_COMPLETE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_IS_ONLINE = "is_online";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_PRODUCT_IDS = "product_ids";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_SYNC_SUCCESS = "sync_success";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.BroadcastHelper INSTANCE = null;
    
    private BroadcastHelper() {
        super();
    }
    
    /**
     * Notifica cambio de estado online/offline
     */
    public final void notifyOnlineStatusChanged(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean isOnline) {
    }
    
    /**
     * Notifica cambio de stock (después de relleno o venta)
     */
    public final void notifyStockChanged(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> productIds) {
    }
    
    /**
     * Notifica que la sincronización completó
     */
    public final void notifySyncComplete(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean success) {
    }
}