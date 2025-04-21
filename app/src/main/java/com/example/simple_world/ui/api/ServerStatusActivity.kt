package com.example.simple_world.ui.api

import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.simple_world.R
import com.example.simple_world.ui.api.ApiConfigActivity
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.Serializable

class ServerStatusActivity : AppCompatActivity() {

    private lateinit var buttonStartServer: Button
    private lateinit var textViewServerStatus: TextView
    private lateinit var textViewRoutes: TextView
    private lateinit var editLog: EditText

    private var server: DynamicRouteServer? = null
    private val port = 8080

    data class Route(val method: String, val path: String, val response: String) : Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_status)

        buttonStartServer = findViewById(R.id.buttonStartServer)
        textViewServerStatus = findViewById(R.id.textViewServerStatus)
        textViewRoutes = findViewById(R.id.textViewRoutes)
        editLog = findViewById(R.id.editLog)

        val receivedRoutes = intent.getSerializableExtra("routeList") as? ArrayList<ApiConfigActivity.RouteEntry> ?: arrayListOf()

        val convertedRoutes = receivedRoutes.map {
            Route(it.method, it.path, it.response)
        }

        buttonStartServer.setOnClickListener {
            if (server == null) {
                startServer(convertedRoutes)
                buttonStartServer.isEnabled = false
            } else {
                Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startServer(routes: List<Route>) {
        val ip = getLocalIpAddress()
        textViewServerStatus.text = "Server running at: http://$ip:$port"

        val routesDisplay = buildString {
            append("Available Routes:\n\n")
            routes.forEach {
                append("${it.method} → http://$ip:$port${it.path}\n")
            }
        }
        textViewRoutes.text = routesDisplay
        println(routesDisplay)

        server = DynamicRouteServer(port, routes) { logMsg ->
            runOnUiThread {
                val current = editLog.text.toString()
                editLog.setText("$current\n$logMsg")
            }
        }
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ipInt = wifiManager.connectionInfo.ipAddress
        return java.net.InetAddress.getByAddress(
            byteArrayOf(
                (ipInt and 0xff).toByte(),
                (ipInt shr 8 and 0xff).toByte(),
                (ipInt shr 16 and 0xff).toByte(),
                (ipInt shr 24 and 0xff).toByte()
            )
        ).hostAddress
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    class DynamicRouteServer(
        port: Int,
        private val routes: List<Route>,
        private val logCallback: (String) -> Unit
    ) : NanoHTTPD(port) {

        init {
            start(SOCKET_READ_TIMEOUT, false)
            println("Server started at http://localhost:$port")
        }

        override fun serve(session: IHTTPSession): Response {
            return try {
                val uri = session.uri
                val method = session.method.name

                var body = ""
                if (session.method == Method.POST || session.method == Method.PUT) {
                    session.parseBody(mutableMapOf())
                    body = session.inputStream.bufferedReader().readText()
                }

                logCallback("Request → $method $uri\nBody: $body\n")

                val matched = routes.find {
                    it.method.equals(method, ignoreCase = true) && it.path == uri
                }

                if (matched != null) {
                    newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, matched.response)
                } else {
                    newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Route not found.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                logCallback("Server error: ${e.message}")
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal Server Error")
            }
        }
    }

}
