package cl.powerbox.gateway.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import cl.powerbox.gateway.R
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.service.GatewayForegroundService
import cl.powerbox.gateway.util.BatteryOptHelper
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.ServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ✅ MainActivity con LogViewer en tiempo real y panel de diagnóstico de paneles
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvLogs: TextView
    private lateinit var scrollLogs: ScrollView
    private lateinit var btnClearLogs: Button
    private lateinit var btnExportLogs: Button
    private lateinit var tvLogCount: TextView
    private lateinit var tvVersion: TextView

    // Controles de configuración de paneles
    private lateinit var rgInputSource: RadioGroup
    private lateinit var swOutputPanel1: SwitchCompat
    private lateinit var swOutputPanel2: SwitchCompat

    // Diagnóstico de paneles
    private lateinit var tvConfigSummary: TextView
    private lateinit var tvPendingCount: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var logCount = 0

    // Refresca el diagnóstico cada 15 segundos mientras la app está visible
    private val diagnosticRunnable = object : Runnable {
        override fun run() {
            refreshDiagnostics()
            handler.postDelayed(this, 15_000L)
        }
    }

    // ✅ Listener para nuevos logs
    private val logListener = object : Logger.LogListener {
        override fun onNewLog(entry: Logger.LogEntry) {
            handler.post {
                appendLogToUI(entry)
            }
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == GatewayForegroundService.ACTION_STATE_CHANGED) {
                val running = intent.getBooleanExtra(GatewayForegroundService.EXTRA_RUNNING, false)
                renderState(running)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a vistas
        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        tvLogs = findViewById(R.id.tvLogs)
        scrollLogs = findViewById(R.id.scrollLogs)
        btnClearLogs = findViewById(R.id.btnClearLogs)
        btnExportLogs = findViewById(R.id.btnExportLogs)
        tvLogCount = findViewById(R.id.tvLogCount)
        tvVersion = findViewById(R.id.tvVersion)

        // Referencias a controles de configuración de paneles
        rgInputSource = findViewById(R.id.rgInputSource)
        swOutputPanel1 = findViewById(R.id.swOutputPanel1)
        swOutputPanel2 = findViewById(R.id.swOutputPanel2)

        // Referencias al panel de diagnóstico
        tvConfigSummary = findViewById(R.id.tvConfigSummary)
        tvPendingCount = findViewById(R.id.tvPendingCount)

        // ✅ Configurar versión dinámica desde BuildConfig
        tvVersion.text = "Powerbox Gateway v${cl.powerbox.gateway.BuildConfig.VERSION_NAME}"

        // ✅ Cargar configuración actual de paneles
        loadServerConfig()

        // ✅ Configurar listeners de paneles
        setupServerConfigListeners()

        // Botones de control del servicio
        btnStart.setOnClickListener {
            GatewayForegroundService.start(this)
            Logger.i("Usuario inició el servicio")
        }

        btnStop.setOnClickListener {
            GatewayForegroundService.stop(this)
            Logger.i("Usuario detuvo el servicio")
        }

        // ✅ Botón limpiar logs
        btnClearLogs.setOnClickListener {
            showClearLogsDialog()
        }

        // ✅ Botón exportar logs
        btnExportLogs.setOnClickListener {
            exportLogs()
        }

        // Estado inicial
        renderState(GatewayForegroundService.isRunning(this))

        // ✅ Cargar logs existentes
        loadExistingLogs()

        // 🔋 Pedir exención de batería
        BatteryOptHelper.maybeRequestOnce(this)
    }

    override fun onResume() {
        super.onResume()

        // Registrar receiver de estado (Android 14+ requiere flag de exportación)
        ContextCompat.registerReceiver(
            this,
            stateReceiver,
            IntentFilter(GatewayForegroundService.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // ✅ Registrar listener de logs
        Logger.addListener(logListener)

        // Pedir estado actual
        GatewayForegroundService.queryState(this)

        // ✅ Iniciar refresco periódico del diagnóstico
        handler.post(diagnosticRunnable)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(stateReceiver)

        // ✅ Desregistrar listener de logs
        Logger.removeListener(logListener)

        // Detener refresco periódico
        handler.removeCallbacks(diagnosticRunnable)
    }

    private fun renderState(running: Boolean) {
        tvStatus.text = if (running) {
            "🟢 Servicio: EN EJECUCIÓN"
        } else {
            "🔴 Servicio: DETENIDO"
        }
        btnStart.isEnabled = !running
        btnStop.isEnabled = running
    }

    /**
     * ✅ Cargar logs existentes al abrir la app
     */
    private fun loadExistingLogs() {
        tvLogs.text = ""
        logCount = 0

        val logs = Logger.getAllLogs()
        if (logs.isEmpty()) {
            tvLogs.text = "📋 Esperando eventos...\n"
            tvLogCount.text = "0 eventos"
        } else {
            logs.forEach { entry ->
                appendLogToUI(entry)
            }
        }
    }

    /**
     * ✅ Agregar log a la UI con colores según nivel
     */
    private fun appendLogToUI(entry: Logger.LogEntry) {
        val color = when (entry.level) {
            "ERROR" -> "#F44336"  // Rojo
            "WARN" -> "#FF9800"   // Naranja
            "INFO" -> "#4CAF50"   // Verde
            "DEBUG" -> "#2196F3"  // Azul
            else -> "#B0BEC5"     // Gris
        }

        val htmlLog = "<font color='$color'>${entry.formatted()}</font><br>"

        runOnUiThread {
            tvLogs.append(android.text.Html.fromHtml(htmlLog, android.text.Html.FROM_HTML_MODE_LEGACY))
            logCount++
            tvLogCount.text = "$logCount eventos"

            // ✅ Auto-scroll al final
            scrollLogs.post {
                scrollLogs.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    /**
     * ✅ Diálogo de confirmación para limpiar logs
     */
    private fun showClearLogsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Limpiar Logs")
            .setMessage("¿Deseas eliminar todos los logs de la pantalla?")
            .setPositiveButton("Limpiar") { _, _ ->
                clearLogs()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ Limpiar logs de la UI y memoria
     */
    private fun clearLogs() {
        Logger.clearInMemoryLogs()
        tvLogs.text = "📋 Logs limpiados. Esperando nuevos eventos...\n"
        logCount = 0
        tvLogCount.text = "0 eventos"
        Logger.i("Logs limpiados por el usuario")
        Toast.makeText(this, "✅ Logs limpiados", Toast.LENGTH_SHORT).show()
    }

    /**
     * ✅ Exportar logs a archivo .log
     */
    private fun exportLogs() {
        val file = Logger.exportLogs(this)

        if (file != null && file.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Exportar logs"))

                Logger.i("Logs exportados: ${file.name}")
                Toast.makeText(
                    this,
                    "✅ Logs exportados\n${file.name}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Logger.e("Error compartiendo logs", e)
                Toast.makeText(
                    this,
                    "❌ Error al compartir logs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "❌ Error al exportar logs",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * ✅ Cargar configuración actual de paneles desde SharedPreferences
     */
    private fun loadServerConfig() {
        val inputSource = ServerConfig.getInputSource(this)
        when (inputSource) {
            1 -> rgInputSource.check(R.id.rbInputPanel1)
            2 -> rgInputSource.check(R.id.rbInputPanel2)
        }

        swOutputPanel1.isChecked = ServerConfig.isOutputPanel1Enabled(this)
        swOutputPanel2.isChecked = ServerConfig.isOutputPanel2Enabled(this)

        ServerConfig.logCurrentConfig(this)
    }

    /**
     * ✅ Configurar listeners para los controles de paneles
     */
    private fun setupServerConfigListeners() {
        rgInputSource.setOnCheckedChangeListener { _, checkedId ->
            val panelNumber = when (checkedId) {
                R.id.rbInputPanel1 -> 1
                R.id.rbInputPanel2 -> 2
                else -> 1
            }
            ServerConfig.setInputSource(this, panelNumber)
            val panelName = if (panelNumber == 1) "CoffeeJi" else "Powerbox"
            Toast.makeText(
                this,
                "📥 Lectura: $panelName",
                Toast.LENGTH_SHORT
            ).show()
            ServerConfig.logCurrentConfig(this)
            refreshDiagnostics()
        }

        swOutputPanel1.setOnCheckedChangeListener { _, isChecked ->
            ServerConfig.setOutputPanel1Enabled(this, isChecked)

            if (!isChecked && !swOutputPanel2.isChecked) {
                Toast.makeText(
                    this,
                    "⚠️ Al menos un panel debe estar activo. Se usará CoffeeJi por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 CoffeeJi habilitado para salida" else "📤 CoffeeJi deshabilitado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ServerConfig.logCurrentConfig(this)
            refreshDiagnostics()
        }

        swOutputPanel2.setOnCheckedChangeListener { _, isChecked ->
            ServerConfig.setOutputPanel2Enabled(this, isChecked)

            if (!isChecked && !swOutputPanel1.isChecked) {
                Toast.makeText(
                    this,
                    "⚠️ Al menos un panel debe estar activo. Se usará CoffeeJi por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 Powerbox habilitado para salida" else "📤 Powerbox deshabilitado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ServerConfig.logCurrentConfig(this)
            refreshDiagnostics()
        }
    }

    /**
     * ✅ Actualiza el panel de diagnóstico con la configuración actual y los pendientes de sincronización.
     * Consulta la BD en un hilo de IO y actualiza la UI en el hilo principal.
     */
    private fun refreshDiagnostics() {
        tvConfigSummary.text = ServerConfig.getConfigSummary(this)

        lifecycleScope.launch {
            val pendingRequests = withContext(Dispatchers.IO) {
                AppDatabase.get(applicationContext).pendingRequestDao().count()
            }
            val pendingReplenishments = withContext(Dispatchers.IO) {
                AppDatabase.get(applicationContext).replenishmentEventDao().allUnsent().size
            }

            val total = pendingRequests + pendingReplenishments
            tvPendingCount.text = when {
                total == 0 -> "✅ Sin pendientes"
                else -> "⏳ $total pendiente(s)"
            }
        }
    }
}
