package com.example.multitoolx.ui.api

import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.multitoolx.R
import fi.iki.elonen.NanoHTTPD
import java.io.Serializable

/**
 * ServerStatusActivity.kt
 *
 * Created on: 2025-04-12
 * Author: Suhaib VP
 * Description:
 * - This activity handles the server's status and manages the dynamic routes.
 * - It allows the user to start an HTTP server on the local device and view the status.
 * - Routes can be configured, and requests to those routes are handled by the server.
 * - Displays logs related to incoming HTTP requests and responses.
 */
class ServerStatusActivity : AppCompatActivity() {

    private lateinit var buttonStartServer: Button
    private lateinit var textViewServerStatus: TextView
    private lateinit var textViewRoutes: TextView
    private lateinit var editLog: EditText

    private var server: DynamicRouteServer? = null
    private val port = 8080

    // Data class representing a route with method, path, and response
    data class Route(val method: String, val path: String, val response: String) : Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_status)

        // Initialize UI components
        buttonStartServer = findViewById(R.id.buttonStartServer)
        textViewServerStatus = findViewById(R.id.textViewServerStatus)
        textViewRoutes = findViewById(R.id.textViewRoutes)
        editLog = findViewById(R.id.editLog)

        // Retrieve the route list passed from ApiConfigActivity
        val receivedRoutes = intent.getSerializableExtra("routeList") as? ArrayList<ApiConfigActivity.RouteEntry> ?: arrayListOf()

        // Convert routes to the format used in this activity
        val convertedRoutes = receivedRoutes.map {
            Route(it.method, it.path, it.response)
        }

        // Start server button listener
        buttonStartServer.setOnClickListener {
            if (server == null) {
                startServer(convertedRoutes) // Start the server with the given routes
                buttonStartServer.isEnabled = false
            } else {
                Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Starts the server and updates the UI with server status and available routes.
     * @param routes The list of routes to be used by the server.
     */
    private fun startServer(routes: List<Route>) {
        val ip = getLocalIpAddress() // Get the local IP address of the device
        textViewServerStatus.text = "Server running at: http://$ip:$port"

        // Display available routes on the UI
        val routesDisplay = buildString {
            append("Available Routes:\n\n")
            routes.forEach {
                append("${it.method} → http://$ip:$port${it.path}\n")
            }
        }
        textViewRoutes.text = routesDisplay
        println(routesDisplay)

        // Initialize and start the server with the given routes
        server = DynamicRouteServer(port, routes) { logMsg ->
            // Update the log UI on the main thread
            runOnUiThread {
                val current = editLog.text.toString()
                editLog.setText("$current\n$logMsg")
            }
        }
    }

    /**
     * Retrieves the local IP address of the device.
     * @return The local IP address as a String.
     */
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
        // Stop the server when the activity is destroyed
        server?.stop()
    }

    /**
     * DynamicRouteServer is a custom HTTP server that serves the given routes.
     * It logs incoming requests and sends appropriate responses based on the method and path.
     */
    class DynamicRouteServer(
        port: Int,
        private val routes: List<Route>,
        private val logCallback: (String) -> Unit // Callback to log request details
    ) : NanoHTTPD(port) {

        init {
            start(SOCKET_READ_TIMEOUT, false) // Start the server asynchronously
            println("Server started at http://localhost:$port")
        }

        /**
         * Handles incoming HTTP requests, matches routes, and sends appropriate responses.
         * @param session The HTTP session object containing request data.
         * @return The HTTP response based on the request.
         */
        override fun serve(session: IHTTPSession): Response {
            return try {
                val uri = session.uri
                val method = session.method.name

                // Read the body of POST/PUT requests
                var body = ""
                if (session.method == Method.POST || session.method == Method.PUT) {
                    session.parseBody(mutableMapOf())
                    body = session.inputStream.bufferedReader().readText()
                }

                // Log the request details
                logCallback("Request → $method $uri\nBody: $body\n")

                // Find the matching route for the current request
                val matched = routes.find {
                    it.method.equals(method, ignoreCase = true) && it.path == uri
                }

                // Return the appropriate response
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
