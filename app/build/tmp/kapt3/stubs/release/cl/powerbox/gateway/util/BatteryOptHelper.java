package cl.powerbox.gateway.util;

/**
 * Muestra el diálogo del sistema para excluir la app de la optimización de batería
 * **una sola vez** (la primera vez que se abre la app). Si el usuario acepta, queda
 * en whitelist; si lo rechaza, no volvemos a pedirlo.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcl/powerbox/gateway/util/BatteryOptHelper;", "", "()V", "KEY_PROMPTED_ONCE", "", "PREFS", "maybeRequestOnce", "", "activity", "Landroid/app/Activity;", "app_release"})
public final class BatteryOptHelper {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "battery_opt_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PROMPTED_ONCE = "prompted_once";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.BatteryOptHelper INSTANCE = null;
    
    private BatteryOptHelper() {
        super();
    }
    
    public final void maybeRequestOnce(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
}