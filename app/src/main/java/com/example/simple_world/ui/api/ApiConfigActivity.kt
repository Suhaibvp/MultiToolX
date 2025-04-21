package com.example.simple_world.ui.api
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.simple_world.R
import com.example.simple_world.ui.api.ServerStatusActivity
import java.io.Serializable

class ApiConfigActivity : AppCompatActivity() {

    private lateinit var spinnerMethod: Spinner
    private lateinit var editPath: EditText
    private lateinit var editResponse: EditText
    private lateinit var buttonAddRoute: Button
    private lateinit var textRouteList: TextView
    private lateinit var btnCreateServer: Button

    private val routeList = mutableListOf<RouteEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_config)

        spinnerMethod = findViewById(R.id.spinnerMethod)
        editPath = findViewById(R.id.editPath)
        editResponse = findViewById(R.id.editResponse)
        buttonAddRoute = findViewById(R.id.buttonAddRoute)
        textRouteList = findViewById(R.id.textRouteList)
        btnCreateServer = findViewById(R.id.btn_createServer)

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

        btnCreateServer.setOnClickListener {
            val intent = Intent(this, ServerStatusActivity::class.java)
            intent.putExtra("routeList", ArrayList(routeList)) // Serializable list
            startActivity(intent)
        }
    }

    private fun updateRouteListUI() {
        val displayText = buildString {
            append("Added Routes:\n\n")
            routeList.forEach {
                append("${it.method} ${it.path} → ${it.response}\n")
            }
        }
        textRouteList.text = displayText
    }

    private fun clearInputs() {
        editPath.setText("")
        editResponse.setText("")
    }

    data class RouteEntry(val method: String, val path: String, val response: String) : Serializable
}
