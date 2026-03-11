package cl.powerbox.gateway.update;

/**
 * Instalador silencioso usando permisos root
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0005J\u0016\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0010\u0010\n\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u000e\u0010\f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u000f"}, d2 = {"Lcl/powerbox/gateway/update/RootInstaller;", "", "()V", "hasRootAccess", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "installSilently", "apkFile", "Ljava/io/File;", "(Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "installViaCat", "installViaPmInstall", "restartApp", "", "Companion", "app_release"})
public final class RootInstaller {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PACKAGE_NAME = "cl.powerbox.gateway";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.update.RootInstaller.Companion Companion = null;
    
    public RootInstaller() {
        super();
    }
    
    /**
     * Verifica si tenemos acceso root
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object hasRootAccess(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Instala el APK silenciosamente usando pm install con root
     * @param apkFile Archivo APK a instalar
     * @return true si la instalación fue exitosa
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object installSilently(@org.jetbrains.annotations.NotNull()
    java.io.File apkFile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Método principal: pm install con root
     */
    private final boolean installViaPmInstall(java.io.File apkFile) {
        return false;
    }
    
    /**
     * Método alternativo: copiar APK a /data/local/tmp y usar pm install desde ahí
     */
    private final boolean installViaCat(java.io.File apkFile) {
        return false;
    }
    
    /**
     * Reinicia la aplicación después de actualizar
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object restartApp(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcl/powerbox/gateway/update/RootInstaller$Companion;", "", "()V", "PACKAGE_NAME", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}