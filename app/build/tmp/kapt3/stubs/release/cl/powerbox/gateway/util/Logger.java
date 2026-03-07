package cl.powerbox.gateway.util;

/**
 * ✅ Logger mejorado con captura en memoria para LogViewer
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u0003\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\r\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0002()B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rJ\u0006\u0010\u0013\u001a\u00020\u0011J\u000e\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u0006J\u001a\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00062\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0018J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\b2\u0006\u0010\u001a\u001a\u00020\u001bJ\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001dJ\b\u0010\u001e\u001a\u0004\u0018\u00010\bJ\u000e\u0010\u001f\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u0006J\u000e\u0010 \u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u001bJ\u0010\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u000bH\u0002J\u000e\u0010#\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rJ\u000e\u0010$\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u0006J\u0018\u0010%\u001a\u00020\u00112\u0006\u0010&\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcl/powerbox/gateway/util/Logger;", "", "()V", "MAX_IN_MEMORY_LOGS", "", "TAG", "", "file", "Ljava/io/File;", "inMemoryLogs", "Ljava/util/concurrent/CopyOnWriteArrayList;", "Lcl/powerbox/gateway/util/Logger$LogEntry;", "listeners", "Lcl/powerbox/gateway/util/Logger$LogListener;", "sdf", "Ljava/text/SimpleDateFormat;", "addListener", "", "listener", "clearInMemoryLogs", "d", "msg", "e", "t", "", "exportLogs", "ctx", "Landroid/content/Context;", "getAllLogs", "", "getLogFile", "i", "init", "notifyListeners", "entry", "removeListener", "w", "write", "text", "level", "LogEntry", "LogListener", "app_release"})
public final class Logger {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "GatewayOffline";
    private static final int MAX_IN_MEMORY_LOGS = 500;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.io.File file;
    @org.jetbrains.annotations.NotNull()
    private static final java.text.SimpleDateFormat sdf = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<cl.powerbox.gateway.util.Logger.LogEntry> inMemoryLogs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<cl.powerbox.gateway.util.Logger.LogListener> listeners = null;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.Logger INSTANCE = null;
    
    private Logger() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void d(@org.jetbrains.annotations.NotNull()
    java.lang.String msg) {
    }
    
    public final void e(@org.jetbrains.annotations.NotNull()
    java.lang.String msg, @org.jetbrains.annotations.Nullable()
    java.lang.Throwable t) {
    }
    
    public final void i(@org.jetbrains.annotations.NotNull()
    java.lang.String msg) {
    }
    
    public final void w(@org.jetbrains.annotations.NotNull()
    java.lang.String msg) {
    }
    
    private final void write(java.lang.String text, java.lang.String level) {
    }
    
    public final void addListener(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.util.Logger.LogListener listener) {
    }
    
    public final void removeListener(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.util.Logger.LogListener listener) {
    }
    
    private final void notifyListeners(cl.powerbox.gateway.util.Logger.LogEntry entry) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<cl.powerbox.gateway.util.Logger.LogEntry> getAllLogs() {
        return null;
    }
    
    public final void clearInMemoryLogs() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.io.File exportLogs(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.io.File getLogFile() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010\u0012\u001a\u00020\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\b\u00a8\u0006\u0016"}, d2 = {"Lcl/powerbox/gateway/util/Logger$LogEntry;", "", "timestamp", "", "level", "message", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getLevel", "()Ljava/lang/String;", "getMessage", "getTimestamp", "component1", "component2", "component3", "copy", "equals", "", "other", "formatted", "hashCode", "", "toString", "app_release"})
    public static final class LogEntry {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String timestamp = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String level = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String message = null;
        
        public LogEntry(@org.jetbrains.annotations.NotNull()
        java.lang.String timestamp, @org.jetbrains.annotations.NotNull()
        java.lang.String level, @org.jetbrains.annotations.NotNull()
        java.lang.String message) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTimestamp() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLevel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String formatted() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final cl.powerbox.gateway.util.Logger.LogEntry copy(@org.jetbrains.annotations.NotNull()
        java.lang.String timestamp, @org.jetbrains.annotations.NotNull()
        java.lang.String level, @org.jetbrains.annotations.NotNull()
        java.lang.String message) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lcl/powerbox/gateway/util/Logger$LogListener;", "", "onNewLog", "", "entry", "Lcl/powerbox/gateway/util/Logger$LogEntry;", "app_release"})
    public static abstract interface LogListener {
        
        public abstract void onNewLog(@org.jetbrains.annotations.NotNull()
        cl.powerbox.gateway.util.Logger.LogEntry entry);
    }
}