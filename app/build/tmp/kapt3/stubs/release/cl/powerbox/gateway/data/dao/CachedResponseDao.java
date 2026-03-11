package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\b\bg\u0018\u00002\u00020\u0001J\u0012\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\b\u0010\u0006\u001a\u00020\u0007H\'J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0016\u0010\n\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\fH\'J\u0010\u0010\r\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000fH\'J\u0016\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0011\u001a\u00020\u0007H\'J\b\u0010\u0012\u001a\u00020\u000fH\'J\u0018\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u000fH\'J\u0010\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u0003H\'\u00a8\u0006\u0017"}, d2 = {"Lcl/powerbox/gateway/data/dao/CachedResponseDao;", "", "byKey", "Lcl/powerbox/gateway/data/entity/CachedResponse;", "key", "", "count", "", "deleteByKey", "", "deleteByKeys", "keys", "", "deleteOlderThan", "olderThanMs", "", "lruKeys", "limit", "totalBytes", "touch", "ts", "upsert", "e", "app_release"})
@androidx.room.Dao()
public abstract interface CachedResponseDao {
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.CachedResponse e);
    
    @androidx.room.Query(value = "SELECT * FROM cached_response WHERE key = :key LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract cl.powerbox.gateway.data.entity.CachedResponse byKey(@org.jetbrains.annotations.NotNull()
    java.lang.String key);
    
    @androidx.room.Query(value = "UPDATE cached_response SET lastHitAt = :ts WHERE key = :key")
    public abstract void touch(@org.jetbrains.annotations.NotNull()
    java.lang.String key, long ts);
    
    @androidx.room.Query(value = "DELETE FROM cached_response WHERE cachedAt < :olderThanMs")
    public abstract int deleteOlderThan(long olderThanMs);
    
    @androidx.room.Query(value = "SELECT key FROM cached_response ORDER BY lastHitAt ASC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<java.lang.String> lruKeys(int limit);
    
    @androidx.room.Query(value = "DELETE FROM cached_response WHERE key IN (:keys)")
    public abstract int deleteByKeys(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> keys);
    
    @androidx.room.Query(value = "SELECT IFNULL(SUM(LENGTH(bytes)), 0) FROM cached_response")
    public abstract long totalBytes();
    
    @androidx.room.Query(value = "DELETE FROM cached_response WHERE key = :key")
    public abstract void deleteByKey(@org.jetbrains.annotations.NotNull()
    java.lang.String key);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM cached_response")
    public abstract int count();
}