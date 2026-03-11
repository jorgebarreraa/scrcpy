package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0012\u0010\u0005\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\'J\b\u0010\b\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0010\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u000eH\'J\b\u0010\u000f\u001a\u00020\tH\'J\u000e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0010\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u0004H\'\u00a8\u0006\u0013"}, d2 = {"Lcl/powerbox/gateway/data/dao/PendingRequestDao;", "", "allPending", "", "Lcl/powerbox/gateway/data/entity/PendingRequest;", "byId", "id", "", "count", "", "deleteById", "", "deleteOlderThan", "timestamp", "", "deleteSynced", "getAll", "insert", "request", "app_release"})
@androidx.room.Dao()
public abstract interface PendingRequestDao {
    
    @androidx.room.Query(value = "SELECT * FROM pending_requests ORDER BY createdAt ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.PendingRequest> allPending();
    
    @androidx.room.Query(value = "SELECT * FROM pending_requests ORDER BY createdAt ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.PendingRequest> getAll();
    
    @androidx.room.Query(value = "SELECT * FROM pending_requests WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract cl.powerbox.gateway.data.entity.PendingRequest byId(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void insert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.PendingRequest request);
    
    @androidx.room.Query(value = "DELETE FROM pending_requests WHERE id = :id")
    public abstract void deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
    
    @androidx.room.Query(value = "DELETE FROM pending_requests WHERE createdAt < :timestamp")
    public abstract int deleteOlderThan(long timestamp);
    
    @androidx.room.Query(value = "DELETE FROM pending_requests")
    public abstract int deleteSynced();
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM pending_requests")
    public abstract int count();
}