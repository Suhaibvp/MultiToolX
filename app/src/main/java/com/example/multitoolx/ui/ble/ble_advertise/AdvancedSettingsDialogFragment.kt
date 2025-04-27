package com.example.multitoolx.ui.ble.ble_advertise

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
import com.example.multitoolx.R

/**
 * AdvancedSettingsDialogFragment.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - DialogFragment to handle advanced settings for BLE characteristics.
 * - Allows users to set Read, Write, Notify properties for characteristics,
 *   and add custom request/response mappings.
 */
class AdvancedSettingsDialogFragment : DialogFragment() {

    // UI components
    private lateinit var readCheckBox: CheckBox
    private lateinit var writeCheckBox: CheckBox
    private lateinit var notifyCheckBox: CheckBox
    private lateinit var readResponseEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var mappingsContainer: LinearLayout

    // Callback listener for saving the settings
    var onSaveListener: ((Boolean, Boolean, Boolean, String?, List<Pair<String, String>>) -> Unit)? = null

    /**
     * Sets the custom dialog theme.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the custom style for the dialog
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
    }

    /**
     * Inflates the layout and sets up the UI components.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.advanced_characteristic_settings, container, false)

        // Initialize UI components
        mappingsContainer = rootView.findViewById(R.id.mappingsContainer)
        readCheckBox = rootView.findViewById(R.id.advancedCheckRead)
        writeCheckBox = rootView.findViewById(R.id.advancedCheckWrite)
        notifyCheckBox = rootView.findViewById(R.id.advancedCheckNotify)
        readResponseEditText = rootView.findViewById(R.id.editAdvancedReadValue)
        saveButton = rootView.findViewById(R.id.btnSaveAdvanced)
        cancelButton = rootView.findViewById(R.id.btnCancelAdvanced)

        // Set up visibility logic for Read Response field
        readCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateReadResponseVisibility(isChecked)
        }

        // Set up visibility for the mappings container and "Add Mapping" button
        val toggleMappingsVisibility = {
            val visible = writeCheckBox.isChecked && notifyCheckBox.isChecked
            mappingsContainer.visibility = if (visible) View.VISIBLE else View.GONE
            rootView.findViewById<Button>(R.id.btnAddMapping).visibility = if (visible) View.VISIBLE else View.GONE
        }
        writeCheckBox.setOnCheckedChangeListener { _, _ -> toggleMappingsVisibility() }
        notifyCheckBox.setOnCheckedChangeListener { _, _ -> toggleMappingsVisibility() }

        // Set up the "Add Mapping" button
        rootView.findViewById<Button>(R.id.btnAddMapping).setOnClickListener {
            Log.d("BLE", "Add mapping button clicked")
            val row = layoutInflater.inflate(R.layout.request_response_row, null)
            mappingsContainer.addView(row)
        }

        // Set up the "Save" button
        saveButton.setOnClickListener {
            val read = readCheckBox.isChecked
            val write = writeCheckBox.isChecked
            val notify = notifyCheckBox.isChecked
            val readResponse = readResponseEditText.text.toString().takeIf { it.isNotBlank() }
            val mappings = mutableListOf<Pair<String, String>>()

            // Collect the mappings from the UI
            for (i in 0 until mappingsContainer.childCount) {
                val view = mappingsContainer.getChildAt(i)
                if (view is LinearLayout) {
                    val requestInput = view.findViewById<EditText>(R.id.requestEditText)
                    val responseInput = view.findViewById<EditText>(R.id.responseEditText)

                    // Validate and add mappings if valid
                    val requestText = requestInput?.text.toString()
                    val responseText = responseInput?.text.toString()
                    if (requestText.isNotBlank() && responseText.isNotBlank()) {
                        mappings.add(Pair(requestText, responseText))
                    }
                } else {
                    Log.w("BLE", "Skipping non-LinearLayout view at index $i")
                }
            }

            // Log the collected mappings for debugging
            Log.i("BLE", "Collected Mappings:")
            mappings.forEach { Log.i("BLE", "Request: ${it.first} → Response: ${it.second}") }

            // Invoke the onSaveListener to pass the collected settings back to the activity
            onSaveListener?.invoke(read, write, notify, readResponse, mappings)

            // Dismiss the dialog after saving the settings
            dismiss()
        }

        // Set up the "Cancel" button
        cancelButton.setOnClickListener {
            dismiss()
        }

        return rootView
    }

    /**
     * Updates the visibility of the Read Response field based on the "Read" checkbox state.
     */
    private fun updateReadResponseVisibility(isChecked: Boolean = readCheckBox.isChecked) {
        readResponseEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
    }
}
