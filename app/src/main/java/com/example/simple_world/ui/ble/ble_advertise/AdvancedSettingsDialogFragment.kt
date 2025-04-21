package com.example.simple_world.ui.ble.ble_advertise

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.example.simple_world.R

//import com.example.ble_manager.R

class AdvancedSettingsDialogFragment : DialogFragment() {

    private lateinit var readCheckBox: CheckBox
    private lateinit var writeCheckBox: CheckBox
    private lateinit var notifyCheckBox: CheckBox
    private lateinit var readResponseEditText: EditText

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var mappingsContainer: LinearLayout


    var onSaveListener: ((Boolean, Boolean, Boolean, String?, List<Pair<String, String>>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the custom style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.advanced_characteristic_settings, container, false)
        mappingsContainer = rootView.findViewById(R.id.mappingsContainer)
        readCheckBox = rootView.findViewById(R.id.advancedCheckRead)
        writeCheckBox = rootView.findViewById(R.id.advancedCheckWrite)
        notifyCheckBox = rootView.findViewById(R.id.advancedCheckNotify)
        readResponseEditText = rootView.findViewById(R.id.editAdvancedReadValue)

        val btnAdd = rootView.findViewById<Button>(R.id.btnAddMapping)

        saveButton = rootView.findViewById(R.id.btnSaveAdvanced)
        cancelButton = rootView.findViewById(R.id.btnCancelAdvanced)
        readCheckBox.setOnCheckedChangeListener { _, isChecked ->
            readResponseEditText.visibility=View.VISIBLE
        }
        readCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateReadResponseVisibility(isChecked)
        }
        val toggleMappingsVisibility = {
            val visible = writeCheckBox.isChecked && notifyCheckBox.isChecked
            if (container != null) {
                container.visibility = if (visible) View.VISIBLE else View.GONE
            }
            btnAdd.visibility = if (visible) View.VISIBLE else View.GONE
        }
        writeCheckBox.setOnCheckedChangeListener { _, _ -> toggleMappingsVisibility() }
        notifyCheckBox.setOnCheckedChangeListener { _, _ -> toggleMappingsVisibility() }
        btnAdd.setOnClickListener {
            println("button add clicked")
            val row = layoutInflater.inflate(R.layout.request_response_row, null)
            if (mappingsContainer != null) {
                mappingsContainer.addView(row)
            }else{
                println("container seems to be null")
            }
        }

        saveButton.setOnClickListener {
            val read = readCheckBox.isChecked
            val write = writeCheckBox.isChecked
            val notify = notifyCheckBox.isChecked
            val readResponse = readResponseEditText.text.toString().takeIf { it.isNotBlank() }
            val mappings = mutableListOf<Pair<String, String>>()

            if (mappingsContainer != null) {
                for (i in 0 until mappingsContainer.childCount) {
                    val view = mappingsContainer.getChildAt(i)
                    if (view is LinearLayout) {
                        val requestInput = view.findViewById<EditText>(R.id.requestEditText)
                        val responseInput = view.findViewById<EditText>(R.id.responseEditText)

                        if (requestInput != null && responseInput != null) {
                            val requestText = requestInput.text.toString()
                            val responseText = responseInput.text.toString()
                            if (requestText.isNotBlank() && responseText.isNotBlank()) {
                                mappings.add(Pair(requestText, responseText))
                            }
                        } else {
                            Log.w("BLE", "Missing input fields at index $i")
                        }
                    } else {
                        Log.w("BLE", "Skipping non-LinearLayout view at index $i")
                    }
                }
            }

            Log.i("BLE", "Collected Mappings:")
            mappings.forEach { Log.i("BLE", "Request: ${it.first} → Response: ${it.second}") }
            onSaveListener?.invoke(read, write, notify, readResponse, mappings)

            dismiss()
        }


        cancelButton.setOnClickListener {
            dismiss()
        }

        return rootView
    }
    private fun updateReadResponseVisibility(isChecked: Boolean = readCheckBox.isChecked) {
        if (isChecked) {
            readResponseEditText.visibility = View.VISIBLE
        } else {
            readResponseEditText.visibility = View.GONE
        }
    }

}
