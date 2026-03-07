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
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import cl.powerbox.gateway.R
import cl.powerbox.gateway.service.GatewayForegroundService
import cl.powerbox.gateway.update.UpdateChecker
import cl.powerbox.gateway.update.UpdateWorker
import cl.powerbox.gateway.util.BatteryOptHelper
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkUtil
import cl.powerbox.gateway.util.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ✅ MainActivity mejorada con LogViewer en tiempo real
 *    y botón de actualización manual segura.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnUpdate: Button

    private lateinit var tvLogs: TextView
    private lateinit var scrollLogs: ScrollView
    private lateinit var btnClearLogs: Button
    private lateinit var btnExportLogs: Button
    private lateinit var tvLogCount: TextView
    private lateinit var tvVersion: TextView

    // ✅ NUEVO: Controles de configuración de paneles
    private lateinit var rgInputSource: RadioGroup
    private lateinit var swOutputPanel1: SwitchCompat
    private lateinit var swOutputPanel2: SwitchCompat

    private val handler = Handler(Looper.getMainLooper())
    private var logCount = 0

    // ✅ Job + Scope propios para corrutinas en la UI
    private val uiJob = SupervisorJob()
    private val uiScope = CoroutineScope(uiJob + Dispatchers.Main)

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
        btnUpdate = findViewById(R.id.btnUpdate)
        tvLogs = findViewById(R.id.tvLogs)
        scrollLogs = findViewById(R.id.scrollLogs)
        btnClearLogs = findViewById(R.id.btnClearLogs)
        btnExportLogs = findViewById(R.id.btnExportLogs)
        tvLogCount = findViewById(R.id.tvLogCount)
        tvVersion = findViewById(R.id.tvVersion)

        // ✅ NUEVO: Referencias a controles de configuración de paneles
        rgInputSource = findViewById(R.id.rgInputSource)
        swOutputPanel1 = findViewById(R.id.swOutputPanel1)
        swOutputPanel2 = findViewById(R.id.swOutputPanel2)

        // 🔄 Obtener versionName dinámicamente desde PackageManager
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersion.text = "Powerbox Gateway v${packageInfo.versionName}"
        } catch (e: Exception) {
            tvVersion.text = "Powerbox Gateway v2.1"
            Logger.w("No se pudo leer la versión del paquete")
        }

        // ✅ NUEVO: Cargar configuración actual de paneles
        loadServerConfig()

        // ✅ NUEVO: Configurar listeners de paneles
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

        // ✅ Botón actualizar (forzar verificación inmediata)
        btnUpdate.setOnClickListener {
            onManualUpdateClick()
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

        // Registrar receiver de estado
        registerReceiver(
            stateReceiver,
            IntentFilter(GatewayForegroundService.ACTION_STATE_CHANGED)
        )

        // ✅ Registrar listener de logs
        Logger.addListener(logListener)

        // Pedir estado actual
        GatewayForegroundService.queryState(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(stateReceiver)

        // ✅ Desregistrar listener de logs
        Logger.removeListener(logListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Cancelar corrutinas asociadas a la Activity de forma segura
        uiJob.cancel()
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
            tvLogs.append(
                android.text.Html.fromHtml(
                    htmlLog,
                    android.text.Html.FROM_HTML_MODE_LEGACY
                )
            )
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
     * ✅ Ejecutar actualización manual desde el botón
     */
    private fun onManualUpdateClick() {
        // 1) Verificar conexión a internet
        if (!NetworkUtil.isOnline(this)) {
            val msg = "No hay conexión con el servidor de actualizaciones."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            Logger.w("⚠️ $msg")
            return
        }

        // 2) Verificar si ya hay una actualización en curso
        if (isUpdateInProgress()) {
            val msg = "Actualización en curso, espere a que termine este proceso."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            Logger.d("⏳ $msg")
            return
        }

        // 3) Ejecutar verificación
        btnUpdate.isEnabled = false
        Logger.i("🔵 Usuario solicitó actualización manual")

        uiScope.launch {
            try {
                Logger.d("🔍 [Manual] Verificando actualizaciones en el servidor...")

                val updateInfo = withContext(Dispatchers.IO) {
                    UpdateChecker(this@MainActivity).checkForUpdate()
                }

                if (updateInfo == null) {
                    val msg = "No hay actualizaciones disponibles. Tienes la última versión instalada."
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                    Logger.i("ℹ️ $msg")
                } else {
                    val msg = "Actualización disponible, iniciando la descarga."
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                    Logger.i("⬇️ $msg Versión: ${updateInfo.versionName}")

                    try {
                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val request = OneTimeWorkRequestBuilder<UpdateWorker>()
                            .setConstraints(constraints)
                            .build()

                        WorkManager.getInstance(this@MainActivity)
                            .enqueueUniqueWork(
                                MANUAL_UPDATE_WORK_NAME,
                                ExistingWorkPolicy.KEEP,
                                request
                            )

                        Logger.d("📦 UpdateWorker encolado para actualización manual")
                    } catch (e: Exception) {
                        Logger.e("❌ Error encolando UpdateWorker manual", e)
                        Toast.makeText(
                            this@MainActivity,
                            "❌ Error al iniciar la actualización",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Logger.e("❌ Error en verificación manual de actualización", e)
                val msg = "No hay conexión con el servidor de actualizaciones."
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
            } finally {
                btnUpdate.isEnabled = true
            }
        }
    }

    /**
     * ✅ Verifica si hay un UpdateWorker en ejecución
     */
    private fun isUpdateInProgress(): Boolean {
        return try {
            val workManager = WorkManager.getInstance(this)

            val autoUpdates = workManager
                .getWorkInfosForUniqueWork(AUTO_UPDATE_WORK_NAME)
                .get()

            val manualUpdates = workManager
                .getWorkInfosForUniqueWork(MANUAL_UPDATE_WORK_NAME)
                .get()

            val autoBusy = autoUpdates.any { info ->
                info.state == WorkInfo.State.RUNNING
            }

            val manualBusy = manualUpdates.any { info ->
                info.state == WorkInfo.State.RUNNING ||
                        info.state == WorkInfo.State.ENQUEUED
            }

            autoBusy || manualBusy
        } catch (e: Exception) {
            Logger.e("Error verificando estado de actualización en curso", e)
            false
        }
    }

    // ==================== FUNCIONES DE CONFIGURACIÓN DE PANELES ====================

    /**
     * ✅ Cargar configuración actual de paneles desde SharedPreferences
     */
    private fun loadServerConfig() {
        // Cargar configuración de entrada
        val inputSource = ServerConfig.getInputSource(this)
        when (inputSource) {
            1 -> rgInputSource.check(R.id.rbInputPanel1)
            2 -> rgInputSource.check(R.id.rbInputPanel2)
        }

        // Cargar configuración de salida
        swOutputPanel1.isChecked = ServerConfig.isOutputPanel1Enabled(this)
        swOutputPanel2.isChecked = ServerConfig.isOutputPanel2Enabled(this)

        // Log de configuración cargada
        ServerConfig.logCurrentConfig(this)
    }

    /**
     * ✅ Configurar listeners para los controles de paneles
     */
    private fun setupServerConfigListeners() {
        // Listener para cambio de fuente de entrada
        rgInputSource.setOnCheckedChangeListener { _, checkedId ->
            val panelNumber = when (checkedId) {
                R.id.rbInputPanel1 -> 1
                R.id.rbInputPanel2 -> 2
                else -> 1
            }
            ServerConfig.setInputSource(this, panelNumber)
            Toast.makeText(
                this,
                "📥 Fuente de lectura: Panel $panelNumber",
                Toast.LENGTH_SHORT
            ).show()
            ServerConfig.logCurrentConfig(this)
        }

        // Listener para Panel 1 de salida
        swOutputPanel1.setOnCheckedChangeListener { _, isChecked ->
            ServerConfig.setOutputPanel1Enabled(this, isChecked)

            if (!isChecked && !swOutputPanel2.isChecked) {
                Toast.makeText(
                    this,
                    "⚠️ Al menos un panel debe estar activo para salida. Se usará Panel 1 por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 Panel 1 habilitado para salida" else "📤 Panel 1 deshabilitado para salida",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ServerConfig.logCurrentConfig(this)
        }

        // Listener para Panel 2 de salida
        swOutputPanel2.setOnCheckedChangeListener { _, isChecked ->
            ServerConfig.setOutputPanel2Enabled(this, isChecked)

            if (!isChecked && !swOutputPanel1.isChecked) {
                Toast.makeText(
                    this,
                    "⚠️ Al menos un panel debe estar activo para salida. Se usará Panel 1 por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 Panel 2 habilitado para salida" else "📤 Panel 2 deshabilitado para salida",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ServerConfig.logCurrentConfig(this)
        }
    }

    companion object {
        private const val AUTO_UPDATE_WORK_NAME = "gateway_auto_update"
        private const val MANUAL_UPDATE_WORK_NAME = "gateway_manual_update"
    }
}
