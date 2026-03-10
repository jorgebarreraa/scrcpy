package cl.powerbox.gateway.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ✅ Logger mejorado con captura en memoria para LogViewer
 */
object Logger {
    private const val TAG = "GatewayOffline"
    private const val MAX_IN_MEMORY_LOGS = 500 // Últimos 500 logs en memoria

    @Volatile
    private var file: File? = null

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    // ✅ Lista thread-safe para logs en memoria
    private val inMemoryLogs = CopyOnWriteArrayList<LogEntry>()

    // ✅ Listeners para actualizar UI en tiempo real
    private val listeners = CopyOnWriteArrayList<LogListener>()

    data class LogEntry(
        val timestamp: String,
        val level: String,
        val message: String
    ) {
        fun formatted(): String = "$timestamp [$level] $message"
    }

    interface LogListener {
        fun onNewLog(entry: LogEntry)
    }

    fun init(ctx: Context) {
        if (file != null) return
        val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
        file = File(dir, "gateway.log")
        write("-- LOGGER INIT --", "INFO")
    }

    fun d(msg: String) {
        Log.d(TAG, msg)
        write(msg, "DEBUG")
    }

    fun e(msg: String, t: Throwable? = null) {
        Log.e(TAG, msg, t)
        val fullMsg = "$msg ${t?.let { "\n" + Log.getStackTraceString(it) } ?: ""}"
        write(fullMsg, "ERROR")
    }

    fun i(msg: String) {
        Log.i(TAG, msg)
        write(msg, "INFO")
    }

    fun w(msg: String) {
        Log.w(TAG, msg)
        write(msg, "WARN")
    }

    private fun write(text: String, level: String) {
        try {
            val timestamp = sdf.format(Date())

            // Escribir a archivo
            file?.appendText("$timestamp [$level] $text\n")

            // ✅ Agregar a memoria
            val entry = LogEntry(timestamp, level, text)
            inMemoryLogs.add(entry)

            // ✅ Mantener solo últimos MAX_IN_MEMORY_LOGS
            if (inMemoryLogs.size > MAX_IN_MEMORY_LOGS) {
                inMemoryLogs.removeAt(0)
            }

            // ✅ Notificar listeners
            notifyListeners(entry)

        } catch (_: Throwable) { }
    }

    // ✅ Registrar listener para updates en tiempo real
    fun addListener(listener: LogListener) {
        listeners.add(listener)
    }

    // ✅ Desregistrar listener
    fun removeListener(listener: LogListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(entry: LogEntry) {
        listeners.forEach { it.onNewLog(entry) }
    }

    // ✅ Obtener todos los logs en memoria
    fun getAllLogs(): List<LogEntry> = inMemoryLogs.toList()

    // ✅ Limpiar logs en memoria
    fun clearInMemoryLogs() {
        inMemoryLogs.clear()
    }

    // ✅ Exportar logs a archivo
    fun exportLogs(ctx: Context): File? {
        try {
            val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
            dir.mkdirs()
            val exportFile = File(dir, "gateway_export_${System.currentTimeMillis()}.log")

            exportFile.writeText(buildString {
                appendLine("========================================")
                appendLine("  GATEWAY OFFLINE - LOG EXPORT")
                appendLine("  Fecha: ${sdf.format(Date())}")
                appendLine("========================================")
                appendLine()

                inMemoryLogs.forEach { entry ->
                    appendLine(entry.formatted())
                }
            })

            return exportFile
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting logs", e)
            return null
        }
    }

    // ✅ Obtener archivo de log actual
    fun getLogFile(): File? = file
}
