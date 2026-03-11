package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0014\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u000e\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\'J\u000e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0016\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\fH\'J\u0016\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\n2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\f0\u0003H\'J\u001c\u0010\u001b\u001a\u00020\n2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\f0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\u0004H\'J\u0016\u0010 \u001a\u00020\n2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u001c\u0010\"\u001a\u00020\n2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010#\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0014\u00a8\u0006$"}, d2 = {"Lcl/powerbox/gateway/data/dao/ReplenishmentEventDao;", "", "all", "", "Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;", "allSent", "allSentSuspend", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "allUnsent", "deleteById", "", "id", "", "deleteOldSent", "", "timestamp", "", "getAll", "insert", "e", "(Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsSent", "eventId", "markAsSentSuspend", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markSent", "ids", "markSentSuspend", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pending", "upsert", "event", "upsertAll", "events", "upsertAllSuspend", "upsertSuspend", "app_release"})
@androidx.room.Dao()
public abstract interface ReplenishmentEventDao {
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> all();
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> getAll();
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object allUnsent(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pending(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object allSentSuspend(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> allSent();
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent event);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertSuspend(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent event, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> events);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertAllSuspend(@org.jetbrains.annotations.NotNull()
    java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> events, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent e, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsSentSuspend(@org.jetbrains.annotations.NotNull()
    java.lang.String eventId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id IN (:ids)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSentSuspend(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> ids, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    public abstract void markAsSent(@org.jetbrains.annotations.NotNull()
    java.lang.String eventId);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id IN (:ids)")
    public abstract void markSent(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> ids);
    
    @androidx.room.Query(value = "DELETE FROM replenishment_events WHERE sent = 1 AND createdAt < :timestamp")
    public abstract int deleteOldSent(long timestamp);
    
    @androidx.room.Query(value = "DELETE FROM replenishment_events WHERE id = :id")
    public abstract void deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
}