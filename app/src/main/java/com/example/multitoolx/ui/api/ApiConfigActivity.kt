package com.example.multitoolx.ui.api

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.multitoolx.R
import com.example.multitoolx.ui.api.ServerStatusActivity
import java.io.Serializable

/**
 * ApiConfigActivity.kt
 *
 * Created on: 2025-04-12
 * Author: Suhaib VP
 * Description:
 * - Activity that allows the user to configure API routes for the server.
 * - Provides functionality to add routes, define HTTP methods, and set responses.
 * - Includes a button to start a new activity to display server status.
 */
class ApiConfigActivity : AppCompatActivity() {

    // View references for UI components
    private lateinit var spinnerMethod: Spinner
    private lateinit var editPath: EditText
    private lateinit var editResponse: EditText
    private lateinit var buttonAddRoute: Button
    private lateinit var textRouteList: TextView
    private lateinit var btnCreateServer: Button

    // List to hold the added route entries
    private val routeList = mutableListOf<RouteEntry>()

    /**
     * Called when the activity is created. Initializes the views and sets up listeners for buttons.
     *
     * @param savedInstanceState The saved state of the activity, used to restore previous state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_config)

        // Initialize UI components
        spinnerMethod = findViewById(R.id.spinnerMethod)
        editPath = findViewById(R.id.editPath)
        editResponse = findViewById(R.id.editResponse)
        buttonAddRoute = findViewById(R.id.buttonAddRoute)
        textRouteList = findViewById(R.id.textRouteList)
        btnCreateServer = findViewById(R.id.btn_createServer)

        // Set up the listener for the "Add Route" button (2025-04-27)
        buttonAddRoute.setOnClickListener {
            val method = spinnerMethod.selectedItem.toString()
            val path = editPath.text.toString().trim()
            val response = editResponse.text.toString().trim()

            if (path.isNotEmpty()) {
                val entry = RouteEntry(method, path, response)
                routeList.add(entry)
                updateRouteListUI()
                clearInputs()
            } else {
                Toast.makeText(this, "Path cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the listener for the "Create Server" button (2025-04-27)
        btnCreateServer.setOnClickListener {
            val intent = Intent(this, ServerStatusActivity::class.java)
            intent.putExtra("routeList", ArrayList(routeList)) // Pass the route list as a serializable object
            startActivity(intent)
        }
    }

    /**
     * Updates the UI to display the current list of added routes.
     */
    private fun updateRouteListUI() {
        val displayText = buildString {
            append("Added Routes:\n\n")
            routeList.forEach {
                append("${it.method} ${it.path} → ${it.response}\n")
            }
        }
        textRouteList.text = displayText
    }

    /**
     * Clears the input fields for path and response.
     */
    private fun clearInputs() {
        editPath.setText("")
        editResponse.setText("")
    }

    /**
     * Data class to represent a route entry, including the HTTP method, path, and response.
     *
     * @property method The HTTP method (e.g., GET, POST).
     * @property path The API route path.
     * @property response The response for the given route.
     */
    data class RouteEntry(val method: String, val path: String, val response: String) : Serializable
}
