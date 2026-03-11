package cl.powerbox.gateway.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\u0006\u0010\u000b\u001a\u00020\nJ\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&J\b\u0010\u0010\u001a\u00020\u0011H&J\b\u0010\u0012\u001a\u00020\u0013H&J\b\u0010\u0014\u001a\u00020\u0015H&\u00a8\u0006\u0017"}, d2 = {"Lcl/powerbox/gateway/data/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "cachedDao", "Lcl/powerbox/gateway/data/dao/CachedResponseDao;", "machineConfigDao", "Lcl/powerbox/gateway/data/dao/MachineConfigDao;", "offlineTransactionDao", "Lcl/powerbox/gateway/data/dao/OfflineTransactionDao;", "pendingDao", "Lcl/powerbox/gateway/data/dao/PendingRequestDao;", "pendingRequestDao", "productDao", "Lcl/powerbox/gateway/data/dao/ProductDao;", "replenishmentEventDao", "Lcl/powerbox/gateway/data/dao/ReplenishmentEventDao;", "saleDao", "Lcl/powerbox/gateway/data/dao/SaleEventDao;", "stockMasterDao", "Lcl/powerbox/gateway/data/dao/StockMasterDao;", "stockStateDao", "Lcl/powerbox/gateway/data/dao/StockStateDao;", "Companion", "app_release"})
@androidx.room.Database(entities = {cl.powerbox.gateway.data.entity.CachedResponse.class, cl.powerbox.gateway.data.entity.PendingRequest.class, cl.powerbox.gateway.data.entity.Product.class, cl.powerbox.gateway.data.entity.StockMaster.class, cl.powerbox.gateway.data.entity.SaleEvent.class, cl.powerbox.gateway.data.entity.ReplenishmentEvent.class, cl.powerbox.gateway.data.entity.MachineConfig.class, cl.powerbox.gateway.data.entity.StockState.class, cl.powerbox.gateway.data.entity.OfflineTransaction.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile cl.powerbox.gateway.data.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.data.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.CachedResponseDao cachedDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.PendingRequestDao pendingDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.ProductDao productDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.StockMasterDao stockMasterDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.SaleEventDao saleDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.ReplenishmentEventDao replenishmentEventDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.MachineConfigDao machineConfigDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.StockStateDao stockStateDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.OfflineTransactionDao offlineTransactionDao();
    
    @org.jetbrains.annotations.NotNull()
    public final cl.powerbox.gateway.data.dao.PendingRequestDao pendingRequestDao() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcl/powerbox/gateway/data/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcl/powerbox/gateway/data/AppDatabase;", "get", "ctx", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final cl.powerbox.gateway.data.AppDatabase get(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
            return null;
        }
    }
}