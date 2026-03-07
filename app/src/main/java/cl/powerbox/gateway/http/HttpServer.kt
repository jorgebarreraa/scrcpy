package cl.powerbox.gateway.http

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.PendingRequest
import cl.powerbox.gateway.data.entity.ReplenishmentEvent
import cl.powerbox.gateway.data.entity.StockState
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.queryString
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import java.text.SimpleDateFormat
import java.util.*

class HttpServer(private val ctx: Context) {

    private var engine: ApplicationEngine? = null
    private val db by lazy { AppDatabase.get(ctx) }
    private val mapper = jacksonObjectMapper()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun start() {
        if (isRunning()) {
            Logger.d("HTTP Server already running, skipping start")
            return
        }

        try {
            engine = embeddedServer(factory = Netty, host = "0.0.0.0", port = 9090) {
                routing {

                    // ==================== ENDPOINTS DE DEBUG ====================

                    // Panel principal
                    get("/__debug") {
                        val html = buildDebugPanelHtml()
                        call.respondText(html, ContentType.Text.Html)
                    }

                    // Estado del sistema
                    get("/__debug/status") {
                        val html = buildSystemStatusHtml()
                        call.respondText(html, ContentType.Text.Html)
                    }

                    // Stock actual (HTML)
                    get("/__debug/stock") {
                        val list = withContext(Dispatchers.IO) { db.stockStateDao().all() }
                        val html = buildStockHtml(list)
                        call.respondText(html, ContentType.Text.Html)
                    }

                    // Peticiones pendientes (HTML)
                    get("/__debug/pending") {
                        val list = withContext(Dispatchers.IO) { db.pendingRequestDao().getAll() }
                        val html = buildPendingRequestsHtml(list)
                        call.respondText(html, ContentType.Text.Html)
                    }

                    // Reabastecimientos (HTML)
                    get("/__debug/replenishments") {
                        val list = withContext(Dispatchers.IO) { db.replenishmentEventDao().getAll() }
                        val html = buildReplenishmentsHtml(list)
                        call.respondText(html, ContentType.Text.Html)
                    }

                    // Forzar sincronización
                    post("/__debug/force-sync") {
                        cl.powerbox.gateway.sync.SyncScheduler.syncNow(ctx)
                        call.respondText("✅ Sincronización iniciada", ContentType.Text.Plain)
                    }

                    // Limpiar datos sincronizados (para pruebas)
                    post("/__debug/clear-synced") {
                        withContext(Dispatchers.IO) {
                            db.pendingRequestDao().deleteSynced()
                        }
                        call.respondText("✅ Peticiones eliminadas", ContentType.Text.Plain)
                    }

                    // ========================== Proxy catch-all ==========================
                    route("/{...}") {
                        get { handleProxyRequest(call, ctx) }
                        post { handleProxyRequest(call, ctx) }
                        put { handleProxyRequest(call, ctx) }
                        delete { handleProxyRequest(call, ctx) }
                        patch { handleProxyRequest(call, ctx) }
                        head { handleProxyRequest(call, ctx) }
                        options { handleProxyRequest(call, ctx) }
                    }
                }
            }.start(wait = false)

            Logger.d("HTTP server started on 0.0.0.0:9090")

        } catch (e: Exception) {
            Logger.e("Failed to start HTTP server: ${e.message}", e)
            throw e
        }
    }

    fun stop() {
        try {
            engine?.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
            engine = null
            Logger.d("HTTP server stopped")
        } catch (t: Throwable) {
            Logger.e("Error stopping HTTP server", t)
        }
    }

    fun isRunning(): Boolean = engine != null

    // ==================== HTML BUILDERS ====================

    private fun buildDebugPanelHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Gateway Debug Panel</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background: #f0f2f5; }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 2rem; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                .header h1 { font-size: 2rem; margin-bottom: 0.5rem; }
                .header p { opacity: 0.9; font-size: 1rem; }
                .container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
                .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; margin: 2rem 0; }
                .card { background: white; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: transform 0.2s, box-shadow 0.2s; }
                .card:hover { transform: translateY(-4px); box-shadow: 0 8px 16px rgba(0,0,0,0.12); }
                .card h2 { color: #333; font-size: 1.25rem; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem; }
                .card h2 .icon { font-size: 1.5rem; }
                .btn { display: inline-block; padding: 0.75rem 1.5rem; margin: 0.5rem 0.5rem 0.5rem 0; background: #667eea; color: white; text-decoration: none; border-radius: 8px; border: none; cursor: pointer; font-size: 0.95rem; font-weight: 500; transition: all 0.2s; }
                .btn:hover { background: #5568d3; transform: scale(1.02); }
                .btn-success { background: #10b981; }
                .btn-success:hover { background: #059669; }
                .btn-danger { background: #ef4444; }
                .btn-danger:hover { background: #dc2626; }
                .info-box { background: #f8fafc; border-left: 4px solid #667eea; padding: 1rem; border-radius: 8px; margin: 1rem 0; }
                .info-box ul { margin-left: 1.5rem; margin-top: 0.5rem; }
                .info-box li { margin: 0.5rem 0; }
                code { background: #e5e7eb; padding: 0.25rem 0.5rem; border-radius: 4px; font-family: 'Courier New', monospace; font-size: 0.9rem; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="container">
                    <h1>🔧 Gateway Debug Panel</h1>
                    <p>Panel de control para monitorear y probar el Gateway Offline</p>
                </div>
            </div>
            
            <div class="container">
                <div class="grid">
                    <div class="card">
                        <h2><span class="icon">📊</span> Monitoreo</h2>
                        <a href="/__debug/status" class="btn">📈 Estado del Sistema</a>
                        <a href="/__debug/stock" class="btn">📦 Stock Actual</a>
                        <a href="/__debug/pending" class="btn">⏳ Peticiones Pendientes</a>
                        <a href="/__debug/replenishments" class="btn">🔄 Reabastecimientos</a>
                    </div>
                    
                    <div class="card">
                        <h2><span class="icon">🛠️</span> Acciones</h2>
                        <button class="btn btn-success" onclick="forceSync()">🔄 Forzar Sincronización</button>
                        <button class="btn btn-danger" onclick="clearSynced()">🗑️ Limpiar Sincronizados</button>
                    </div>
                </div>
                
                <div class="card">
                    <h2><span class="icon">📖</span> Instrucciones de Acceso</h2>
                    <div class="info-box">
                        <p><strong>Desde tu PC (misma red WiFi):</strong></p>
                        <ul>
                            <li>Encuentra la IP del dispositivo: <strong>Configuración → Wi-Fi → [Red conectada] → Ver IP</strong></li>
                            <li>Abre en tu navegador: <code>http://[IP_DISPOSITIVO]:9090/__debug</code></li>
                            <li>Ejemplo: <code>http://192.168.1.100:9090/__debug</code></li>
                        </ul>
                    </div>
                    <div class="info-box">
                        <p><strong>Desde el mismo dispositivo:</strong></p>
                        <ul>
                            <li>Abre el navegador en el dispositivo</li>
                            <li>Ve a: <code>http://127.0.0.1:9090/__debug</code></li>
                        </ul>
                    </div>
                </div>
                
                <div class="card">
                    <h2><span class="icon">🧪</span> Pruebas Offline (Sin Modo Avión)</h2>
                    <div class="info-box">
                        <p><strong>Para simular pérdida de internet:</strong></p>
                        <ul>
                            <li><strong>Opción 1:</strong> Desconecta el cable WAN de tu router</li>
                            <li><strong>Opción 2:</strong> Bloquea la MAC del dispositivo en el router temporalmente</li>
                            <li><strong>Opción 3:</strong> Apaga el módem/router por completo</li>
                        </ul>
                        <p style="margin-top: 1rem;"><em>El dispositivo seguirá conectado a WiFi pero sin acceso real a internet. El sistema detectará la pérdida mediante ping a servidores externos cada 30 segundos.</em></p>
                    </div>
                </div>
            </div>
            
            <script>
                function forceSync() {
                    if (confirm('¿Forzar sincronización ahora?')) {
                        fetch('/__debug/force-sync', { method: 'POST' })
                            .then(r => r.text())
                            .then(msg => { alert(msg); location.reload(); })
                            .catch(e => alert('Error: ' + e));
                    }
                }
                
                function clearSynced() {
                    if (confirm('¿Eliminar todas las peticiones sincronizadas de la base de datos?')) {
                        fetch('/__debug/clear-synced', { method: 'POST' })
                            .then(r => r.text())
                            .then(msg => { alert(msg); location.reload(); })
                            .catch(e => alert('Error: ' + e));
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    private fun buildSystemStatusHtml(): String {
        val isOnline = NetworkUtil.isOnline(ctx)
        val connectionType = NetworkUtil.getConnectionType(ctx)
        val statusClass = if (isOnline) "online" else "offline"
        val statusText = if (isOnline) "ONLINE" else "OFFLINE"

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="refresh" content="5">
                <title>System Status</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background: #f0f2f5; padding: 2rem; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                    h1 { color: #333; margin-bottom: 1.5rem; }
                    .status-box { padding: 1.5rem; border-radius: 8px; margin: 1rem 0; }
                    .status { display: inline-block; padding: 0.75rem 1.5rem; border-radius: 8px; font-weight: bold; font-size: 1.25rem; }
                    .online { background: #d1fae5; color: #065f46; }
                    .offline { background: #fee2e2; color: #991b1b; }
                    .info { background: #f8fafc; padding: 1rem; margin: 1rem 0; border-radius: 8px; border-left: 4px solid #667eea; }
                    .info-item { margin: 0.75rem 0; }
                    .info-label { font-weight: 600; color: #4b5563; }
                    .info-value { color: #1f2937; }
                    a { color: #667eea; text-decoration: none; font-weight: 500; }
                    a:hover { text-decoration: underline; }
                    .refresh-note { text-align: center; margin-top: 2rem; color: #6b7280; font-size: 0.9rem; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📊 Estado del Sistema</h1>
                    
                    <div class="status-box">
                        <span class="status $statusClass">$statusText</span>
                    </div>
                    
                    <div class="info">
                        <div class="info-item">
                            <span class="info-label">Tipo de Conexión:</span>
                            <span class="info-value">$connectionType</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Hora:</span>
                            <span class="info-value">${dateFormat.format(Date())}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Servidor:</span>
                            <span class="info-value">0.0.0.0:9090</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Base de datos:</span>
                            <span class="info-value">gateway_clean.db</span>
                        </div>
                    </div>
                    
                    <p><a href="/__debug">← Volver al panel principal</a></p>
                    
                    <div class="refresh-note">
                        ⟳ Auto-refresh cada 5 segundos
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildStockHtml(stockList: List<StockState>): String {
        val rows = stockList.joinToString("") { stock ->
            val effective = stock.effective()
            val deltaColor = when {
                stock.localDelta > 0 -> "color: #10b981; font-weight: bold;"
                stock.localDelta < 0 -> "color: #ef4444; font-weight: bold;"
                else -> "color: #6b7280;"
            }
            """
            <tr>
                <td>${stock.productId}</td>
                <td style="text-align: center;">${stock.serverQty}</td>
                <td style="text-align: center; $deltaColor">${if (stock.localDelta > 0) "+" else ""}${stock.localDelta}</td>
                <td style="text-align: center; font-weight: bold; font-size: 1.1rem;">$effective</td>
                <td style="text-align: center; font-size: 0.85rem; color: #6b7280;">${dateFormat.format(Date(stock.lastSync))}</td>
            </tr>
            """
        }

        val totalProducts = stockList.size
        val totalServer = stockList.sumOf { it.serverQty }
        val totalDelta = stockList.sumOf { it.localDelta }
        val totalEffective = stockList.sumOf { it.effective() }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Stock State</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background: #f0f2f5; padding: 2rem; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                    h1 { color: #333; margin-bottom: 1.5rem; }
                    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin: 1.5rem 0; }
                    .summary-card { background: #f8fafc; padding: 1rem; border-radius: 8px; border-left: 4px solid #667eea; }
                    .summary-label { font-size: 0.9rem; color: #6b7280; margin-bottom: 0.5rem; }
                    .summary-value { font-size: 1.75rem; font-weight: bold; color: #1f2937; }
                    table { width: 100%; border-collapse: collapse; margin: 1.5rem 0; }
                    th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #e5e7eb; }
                    th { background: #667eea; color: white; font-weight: 600; }
                    tr:hover { background: #f9fafb; }
                    a { color: #667eea; text-decoration: none; font-weight: 500; margin-top: 1.5rem; display: inline-block; }
                    a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📦 Estado del Stock</h1>
                    
                    <div class="summary">
                        <div class="summary-card">
                            <div class="summary-label">Total Productos</div>
                            <div class="summary-value">$totalProducts</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Total Servidor</div>
                            <div class="summary-value">$totalServer</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Delta Local</div>
                            <div class="summary-value" style="color: ${if (totalDelta < 0) "#ef4444" else "#10b981"};">
                                ${if (totalDelta > 0) "+" else ""}$totalDelta
                            </div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Stock Efectivo</div>
                            <div class="summary-value" style="color: #667eea;">$totalEffective</div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>Product ID</th>
                                <th style="text-align: center;">Server Qty</th>
                                <th style="text-align: center;">Local Delta</th>
                                <th style="text-align: center;">Effective Qty</th>
                                <th style="text-align: center;">Last Sync</th>
                            </tr>
                        </thead>
                        <tbody>
                            $rows
                        </tbody>
                    </table>
                    
                    <a href="/__debug">← Volver al panel</a>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildPendingRequestsHtml(requests: List<PendingRequest>): String {
        val rows = requests.joinToString("") { req ->
            val methodColor = when (req.method) {
                "POST" -> "#10b981"
                "PUT", "PATCH" -> "#f59e0b"
                "DELETE" -> "#ef4444"
                else -> "#667eea"
            }
            """
            <tr>
                <td style="font-size: 0.85rem; color: #6b7280;">${req.id.take(8)}...</td>
                <td><span style="background: $methodColor; color: white; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.85rem; font-weight: 600;">${req.method}</span></td>
                <td style="font-size: 0.9rem;">/${req.path}</td>
                <td style="font-size: 0.85rem; color: #6b7280;">${dateFormat.format(Date(req.createdAt))}</td>
            </tr>
            """
        }

        val postCount = requests.count { it.method == "POST" }
        val putCount = requests.count { it.method == "PUT" || it.method == "PATCH" }
        val deleteCount = requests.count { it.method == "DELETE" }
        val otherCount = requests.size - postCount - putCount - deleteCount

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pending Requests</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background: #f0f2f5; padding: 2rem; }
                    .container { max-width: 1400px; margin: 0 auto; background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                    h1 { color: #333; margin-bottom: 1.5rem; }
                    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 1rem; margin: 1.5rem 0; }
                    .summary-card { background: #f8fafc; padding: 1rem; border-radius: 8px; text-align: center; }
                    .summary-label { font-size: 0.85rem; color: #6b7280; margin-bottom: 0.5rem; }
                    .summary-value { font-size: 1.75rem; font-weight: bold; color: #1f2937; }
                    table { width: 100%; border-collapse: collapse; margin: 1.5rem 0; }
                    th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #e5e7eb; }
                    th { background: #667eea; color: white; font-weight: 600; }
                    tr:hover { background: #f9fafb; }
                    a { color: #667eea; text-decoration: none; font-weight: 500; margin-top: 1.5rem; display: inline-block; }
                    a:hover { text-decoration: underline; }
                    .empty-state { text-align: center; padding: 3rem; color: #6b7280; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>⏳ Peticiones Pendientes</h1>
                    
                    <div class="summary">
                        <div class="summary-card">
                            <div class="summary-label">Total</div>
                            <div class="summary-value">${requests.size}</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">POST</div>
                            <div class="summary-value" style="color: #10b981;">$postCount</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">PUT/PATCH</div>
                            <div class="summary-value" style="color: #f59e0b;">$putCount</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">DELETE</div>
                            <div class="summary-value" style="color: #ef4444;">$deleteCount</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Otros</div>
                            <div class="summary-value" style="color: #667eea;">$otherCount</div>
                        </div>
                    </div>
                    
                    ${if (requests.isEmpty()) {
            """<div class="empty-state">
                            <div style="font-size: 3rem; margin-bottom: 1rem;">✅</div>
                            <div style="font-size: 1.25rem; font-weight: 600;">No hay peticiones pendientes</div>
                            <div style="margin-top: 0.5rem;">Todas las operaciones están sincronizadas</div>
                        </div>"""
        } else {
            """<table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Method</th>
                                    <th>Path</th>
                                    <th>Created At</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rows
                            </tbody>
                        </table>"""
        }}
                    
                    <a href="/__debug">← Volver al panel</a>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildReplenishmentsHtml(events: List<ReplenishmentEvent>): String {
        val rows = events.joinToString("") { event ->
            val syncIcon = if (event.sent) "✅" else "⏳"
            val syncColor = if (event.sent) "#10b981" else "#f59e0b"
            """
            <tr>
                <td style="font-size: 0.85rem; color: #6b7280;">${event.id.take(8)}...</td>
                <td style="font-weight: 600;">${event.productId}</td>
                <td style="text-align: center; font-weight: 600; color: ${if (event.deltaQty > 0) "#10b981" else "#6b7280"};">
                    ${if (event.deltaQty > 0) "+" else ""}${event.deltaQty}
                </td>
                <td style="font-size: 0.85rem; color: #6b7280;">${dateFormat.format(Date(event.createdAt))}</td>
                <td style="text-align: center; font-size: 1.25rem;" title="${if (event.sent) "Sincronizado" else "Pendiente"}">
                    <span style="color: $syncColor;">$syncIcon</span>
                </td>
            </tr>
            """
        }

        val totalEvents = events.size
        val syncedEvents = events.count { it.sent }
        val pendingEvents = totalEvents - syncedEvents
        val totalQuantity = events.sumOf { it.deltaQty }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Replenishments</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background: #f0f2f5; padding: 2rem; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                    h1 { color: #333; margin-bottom: 1.5rem; }
                    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin: 1.5rem 0; }
                    .summary-card { background: #f8fafc; padding: 1rem; border-radius: 8px; text-align: center; }
                    .summary-label { font-size: 0.85rem; color: #6b7280; margin-bottom: 0.5rem; }
                    .summary-value { font-size: 1.75rem; font-weight: bold; color: #1f2937; }
                    table { width: 100%; border-collapse: collapse; margin: 1.5rem 0; }
                    th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #e5e7eb; }
                    th { background: #667eea; color: white; font-weight: 600; }
                    tr:hover { background: #f9fafb; }
                    a { color: #667eea; text-decoration: none; font-weight: 500; margin-top: 1.5rem; display: inline-block; }
                    a:hover { text-decoration: underline; }
                    .empty-state { text-align: center; padding: 3rem; color: #6b7280; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔄 Eventos de Reabastecimiento</h1>
                    
                    <div class="summary">
                        <div class="summary-card">
                            <div class="summary-label">Total Eventos</div>
                            <div class="summary-value">$totalEvents</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Sincronizados</div>
                            <div class="summary-value" style="color: #10b981;">$syncedEvents</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Pendientes</div>
                            <div class="summary-value" style="color: #f59e0b;">$pendingEvents</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-label">Cantidad Total</div>
                            <div class="summary-value" style="color: #667eea;">+$totalQuantity</div>
                        </div>
                    </div>
                    
                    ${if (events.isEmpty()) {
            """<div class="empty-state">
                            <div style="font-size: 3rem; margin-bottom: 1rem;">📦</div>
                            <div style="font-size: 1.25rem; font-weight: 600;">No hay eventos de reabastecimiento</div>
                            <div style="margin-top: 0.5rem;">Aún no se han registrado rellenos</div>
                        </div>"""
        } else {
            """<table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Product ID</th>
                                    <th style="text-align: center;">Quantity</th>
                                    <th>Created At</th>
                                    <th style="text-align: center;">Synced</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rows
                            </tbody>
                        </table>"""
        }}
                    
                    <a href="/__debug">← Volver al panel</a>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

// Función suspend standalone para manejar todas las peticiones del proxy
private suspend fun handleProxyRequest(call: ApplicationCall, ctx: Context) {
    // Incluir query parameters si existen
    val basePath = call.request.path().trimStart('/')
    val queryString = call.request.queryString()
    val path = if (queryString.isNotEmpty()) {
        "$basePath?$queryString"
    } else {
        basePath
    }
    val methodStr = call.request.httpMethod.value

    // Body
    val bodyBytes = call.receiveChannel()
        .readRemaining()
        .readBytes()

    val headersMap = mutableMapOf<String, String>()
    call.request.headers.forEach { name, values ->
        headersMap[name] = values.joinToString(",")
    }

    val res = ProxyHandler(ctx).handle(
        path = path,
        method = methodStr,
        headers = headersMap,
        body = if (bodyBytes.isNotEmpty()) bodyBytes else null
    )

    call.respondBytes(
        bytes = res.body,
        contentType = ContentType.parse(res.contentType),
        status = HttpStatusCode.fromValue(res.status)
    )
}
