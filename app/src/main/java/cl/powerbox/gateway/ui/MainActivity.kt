package cl.powerbox.gateway.ui

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
import cl.powerbox.gateway.update.RootInstaller
import cl.powerbox.gateway.update.UpdateChecker
import cl.powerbox.gateway.update.UpdateDownloader
import cl.powerbox.gateway.util.BatteryOptHelper
import cl.powerbox.gateway.util.DeviceRegistrar
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
    private lateinit var btnCheckUpdate: Button
    private lateinit var tvDeviceExtNo: TextView
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

    // ✅ Listener para nuevos logs (solo INFO, WARN, ERROR — no DEBUG spam)
    private val logListener = object : Logger.LogListener {
        override fun onNewLog(entry: Logger.LogEntry) {
            if (entry.level == "DEBUG") return
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
        btnCheckUpdate = findViewById(R.id.btnCheckUpdate)
        tvDeviceExtNo = findViewById(R.id.tvDeviceExtNo)
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

        // ✅ Botón verificar/instalar actualización manual
        btnCheckUpdate.setOnClickListener {
            checkForUpdateManually()
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

        // ✅ Recargar logs para mostrar eventos generados en segundo plano
        loadExistingLogs()

        // Mostrar N° de dispositivo si ya fue detectado
        val extNo = DeviceRegistrar.getDeviceExtNo(this)
        tvDeviceExtNo.text = if (extNo != null) "N° dispositivo: $extNo" else ""

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

        val logs = Logger.getAllLogs().filter { it.level != "DEBUG" }
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
     * ✅ Exportar logs — guarda en la carpeta Descargas pública del dispositivo
     */
    private fun exportLogs() {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val srcFile = Logger.exportLogs(this@MainActivity)
            if (srcFile == null || !srcFile.exists()) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Error al exportar logs", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val fileName = srcFile.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // API 29+: insertar en MediaStore Downloads (accesible por cualquier app)
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val itemUri = contentResolver.insert(collection, values)!!
                    contentResolver.openOutputStream(itemUri)!!.use { out ->
                        srcFile.inputStream().use { it.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(itemUri, values, null, null)

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Logger.i("Logs exportados a Descargas: $fileName")
                        Toast.makeText(this@MainActivity,
                            "✅ Guardado en Descargas:\n$fileName",
                            Toast.LENGTH_LONG).show()
                    }
                } else {
                    // API < 29: copiar directo a /sdcard/Download/
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    downloadsDir.mkdirs()
                    val destFile = java.io.File(downloadsDir, fileName)
                    srcFile.copyTo(destFile, overwrite = true)

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Logger.i("Logs exportados: ${destFile.absolutePath}")
                        Toast.makeText(this@MainActivity,
                            "✅ Guardado en:\n${destFile.absolutePath}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Logger.e("Error exportando logs a Descargas", e)
                // Fallback: share intent con FileProvider
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        val uri = FileProvider.getUriForFile(
                            this@MainActivity,
                            "${packageName}.fileprovider",
                            srcFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(intent, "Exportar logs"))
                    } catch (ex: Exception) {
                        Toast.makeText(this@MainActivity, "❌ Error al exportar logs", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
            val panelName = if (panelNumber == 1) "Panel 1 (CoffeeJi)" else "Panel 2 (Powerbox)"
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
                    "⚠️ Al menos un panel debe estar activo. Se usará Panel 1 por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 Panel 1 (CoffeeJi) habilitado" else "📤 Panel 1 (CoffeeJi) deshabilitado",
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
                    "⚠️ Al menos un panel debe estar activo. Se usará Panel 1 por defecto.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    if (isChecked) "📤 Panel 2 (Powerbox) habilitado" else "📤 Panel 2 (Powerbox) deshabilitado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ServerConfig.logCurrentConfig(this)
            refreshDiagnostics()
        }
    }

    /**
     * ✅ Verificar actualizaciones manualmente desde el botón
     */
    private fun checkForUpdateManually() {
        btnCheckUpdate.isEnabled = false
        btnCheckUpdate.text = "Buscando…"
        Logger.i("🔍 Verificando actualización manual...")

        lifecycleScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    UpdateChecker(applicationContext).checkForUpdate()
                }

                if (info != null) {
                    Logger.i("✅ Nueva versión disponible: ${info.versionName} (code ${info.versionCode})")
                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Nueva versión disponible")
                            .setMessage("Versión ${info.versionName}\n\n${info.changelog}\n\n¿Descargar e instalar ahora?")
                            .setPositiveButton("Instalar") { _, _ ->
                                lifecycleScope.launch {
                                    Logger.i("⬇️ Descargando actualización ${info.versionName}...")
                                    val apkFile = withContext(Dispatchers.IO) {
                                        UpdateDownloader(applicationContext).downloadApk(info.apkUrl, info.md5)
                                    }
                                    if (apkFile != null) {
                                        Logger.i("📦 APK descargado, instalando...")
                                        val ok = withContext(Dispatchers.IO) { RootInstaller().installSilently(apkFile) }
                                        if (ok) {
                                            Logger.i("✅ Actualización instalada. Reiniciando...")
                                            withContext(Dispatchers.IO) { RootInstaller().restartApp() }
                                        } else {
                                            Logger.e("❌ Fallo al instalar APK")
                                            Toast.makeText(this@MainActivity, "❌ Error al instalar", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Logger.e("❌ Fallo al descargar APK")
                                        Toast.makeText(this@MainActivity, "❌ Error descargando APK", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                } else {
                    Logger.i("✅ App ya está actualizada")
                    Toast.makeText(this@MainActivity, "✅ App ya está actualizada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Logger.e("❌ Error verificando actualización", e)
                Toast.makeText(this@MainActivity, "❌ Error al verificar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                runOnUiThread {
                    btnCheckUpdate.isEnabled = true
                    btnCheckUpdate.text = "Actualizar"
                }
            }
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
