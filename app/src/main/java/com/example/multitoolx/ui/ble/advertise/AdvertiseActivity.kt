package com.example.multitoolx.ui.ble.advertise

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.multitoolx.R
import com.example.multitoolx.databinding.ActivityAdvertiseBinding
import com.example.multitoolx.services.ble.MyGattServerCallback
import com.example.multitoolx.ui.ble.ble_advertise.AdvancedSettingsDialogFragment
import java.util.UUID

/**
 * AdvertiseActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Manages Bluetooth Low Energy (BLE) advertisement setup and configuration.
 * - Allows users to configure service and characteristic UUIDs, set properties (Read, Write, Notify),
 *   and start BLE advertising with configurable settings.
 */
class AdvertiseActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 1001
    private lateinit var binding: ActivityAdvertiseBinding
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null
    private lateinit var currentServiceUUID: UUID
    private lateinit var context: Context
    private lateinit var gattServerCallback: BluetoothGattServerCallback
    private val bundle = Bundle()

    /**
     * Initializes the activity, sets up ViewBinding, and configures button listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding (2025-04-27)
        binding = ActivityAdvertiseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // Set up click listener for the test button (debug purpose)
        binding.testButton.setOnClickListener {
            println("Test button clicked")
        }

        // Set up click listener for adding new characteristic
        binding.btnAddCharacteristic.setOnClickListener {
            Log.d("BLE", "Add characteristic button clicked")

            // Inflate a new characteristic row view
            val characteristicView = layoutInflater.inflate(R.layout.characteristic_row, null)
            binding.characteristicsContainer.addView(characteristicView)

            // Set up button for advanced settings
            val btnAdvanced = characteristicView.findViewById<Button>(R.id.btnAdvancedSettings)
            val checkRead = characteristicView.findViewById<CheckBox>(R.id.checkRead)
            val checkWrite = characteristicView.findViewById<CheckBox>(R.id.checkWrite)
            val checkNotify = characteristicView.findViewById<CheckBox>(R.id.checkNotify)

            // Handle advanced settings click
            btnAdvanced.setOnClickListener {
                val dialog = AdvancedSettingsDialogFragment()
                dialog.onSaveListener = { read, write, notify, readResponse, mappings ->
                    checkRead.isChecked = read
                    checkWrite.isChecked = write
                    checkNotify.isChecked = notify

                    // Update characteristic configuration
                    updateCharacteristicConfig(read, write, notify, readResponse, mappings)
                }
                dialog.show(supportFragmentManager, "AdvancedSettingsDialog")
            }
        }

        // Set up click listener for the start advertising button
        binding.startAdvertiseButton.setOnClickListener {
            val bleName = binding.bleNameEditText.text.toString().trim()

            // Validate BLE name input
            when {
                bleName.isEmpty() -> {
                    Toast.makeText(this, "Please enter a BLE name", Toast.LENGTH_SHORT).show()
                }
                bleName.length > 20 -> {
                    Toast.makeText(this, "BLE name must be 20 characters or less", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Generate or use the user-provided service UUID
                    val serviceUUID = binding.editServiceUUID.text.toString()
                        .ifBlank { UUID.randomUUID().toString() }

                    Log.d("BLE_SETUP", "Service UUID: $serviceUUID")

                    // Iterate through the characteristics container and log configurations
                    for (i in 0 until binding.characteristicsContainer.childCount) {
                        val charView = binding.characteristicsContainer.getChildAt(i)
                        val charUUID = charView.findViewById<EditText>(R.id.editCharacteristicUUID).text.toString()
                            .ifBlank { UUID.randomUUID().toString() }

                        val canRead = charView.findViewById<CheckBox>(R.id.checkRead).isChecked
                        val canWrite = charView.findViewById<CheckBox>(R.id.checkWrite).isChecked
                        val canNotify = charView.findViewById<CheckBox>(R.id.checkNotify).isChecked
                        val value = charView.findViewById<EditText>(R.id.editValue).text.toString()

                        Log.d("BLE_SETUP", "Characteristic #$i")
                        Log.d("BLE_SETUP", "UUID: $charUUID")
                        Log.d("BLE_SETUP", "Properties -> Read: $canRead, Write: $canWrite, Notify: $canNotify")
                        Log.d("BLE_SETUP", "Initial Value: $value")
                    }

                    // Validate or generate service UUID and proceed with permission checks
                    validateOrGenerateServiceUUIDAndProceed(bleName)
                }
            }
        }
    }

    /**
     * Updates the characteristic configuration with advanced settings.
     */
    private fun updateCharacteristicConfig(read: Boolean, write: Boolean, notify: Boolean, readResponse: String?, mappings: List<Pair<String, String>>) {
        bundle.putString("readresponse", readResponse)

        val mappingList = ArrayList<String>().apply {
            mappings.forEach { add("${it.first}:${it.second}") }
        }
        bundle.putStringArrayList("mappings", mappingList)
    }

    /**
     * Validates or generates a new service UUID and proceeds with permission checks.
     */
    private fun validateOrGenerateServiceUUIDAndProceed(bleName: String) {
        val serviceUUIDText = binding.editServiceUUID.text.toString().trim()

        if (serviceUUIDText.isBlank()) {
            // Generate random UUID if not provided
            currentServiceUUID = UUID.randomUUID()
            binding.editServiceUUID.setText(currentServiceUUID.toString())
            println("Generated random UUID: $currentServiceUUID")
            checkAndRequestPermissions(bleName)
        } else {
            try {
                // Parse and validate the provided UUID
                currentServiceUUID = UUID.fromString(serviceUUIDText)
                println("Valid UUID provided: $currentServiceUUID")
                checkAndRequestPermissions(bleName)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Invalid UUID format. Please check and try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Starts BLE advertising with the specified custom name.
     */
    fun startBleAdvertising(customName: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if BLE advertising is supported
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            println("BLE advertising is not supported on this device.")
            return
        }

        // Set the device's Bluetooth name
        bluetoothAdapter.name = customName

        // Create BLE advertiser and advertising settings
        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true) // Enable connection to GATT
            .build()

        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(currentServiceUUID)) // Use actual service UUID
            .build()

        // Check for necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            println("Permission not granted for BLE advertising.")
            return
        }

        // Set up the GATT server callback and initialize the GATT server
        gattServerCallback = MyGattServerCallback(this, bundle)
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)
        (gattServerCallback as MyGattServerCallback).setGattServer(bluetoothGattServer)

        // Configure the GATT service and characteristics
        val serviceUUIDText = binding.editServiceUUID.text.toString().ifBlank { UUID.randomUUID().toString() }
        currentServiceUUID = UUID.fromString(serviceUUIDText)
        val gattService = BluetoothGattService(currentServiceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Loop through characteristics and add them to the GATT service
        for (i in 0 until binding.characteristicsContainer.childCount) {
            val charView = binding.characteristicsContainer.getChildAt(i)

            val uuidText = charView.findViewById<EditText>(R.id.editCharacteristicUUID).text.toString()
                .ifBlank { UUID.randomUUID().toString() }
            val charUUID = UUID.fromString(uuidText)

            val canRead = charView.findViewById<CheckBox>(R.id.checkRead).isChecked
            val canWrite = charView.findViewById<CheckBox>(R.id.checkWrite).isChecked
            val canNotify = charView.findViewById<CheckBox>(R.id.checkNotify).isChecked
            val value = charView.findViewById<EditText>(R.id.editValue).text.toString()

            // Calculate the characteristic properties and permissions
            var properties = 0
            var permissions = 0
            if (canRead) {
                properties = properties or BluetoothGattCharacteristic.PROPERTY_READ
                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_READ
            }
            if (canWrite) {
                properties = properties or BluetoothGattCharacteristic.PROPERTY_WRITE
                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_WRITE
            }
            if (canNotify) {
                properties = properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY
                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_READ
            }
            val characteristic = BluetoothGattCharacteristic(charUUID, properties, permissions)

            // Add CCCD descriptor for notifications
            if (canNotify) {
                val cccd = BluetoothGattDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),  // Standard CCCD UUID
                    BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
                )
                characteristic.addDescriptor(cccd)
            }

            // Set initial value for the characteristic
            if (value.isNotBlank()) {
                characteristic.value = value.toByteArray()
            }

            gattService.addCharacteristic(characteristic)
        }

        bluetoothGattServer?.addService(gattService)

        // Start advertising
        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                println("✅ Advertising started successfully.")
                Toast.makeText(this@AdvertiseActivity, "Advertise Started Successfully", Toast.LENGTH_LONG).show()
            }

            override fun onStartFailure(errorCode: Int) {
                println("❌ Advertising failed with error code: $errorCode")
                Toast.makeText(this@AdvertiseActivity, "Failed to Advertise", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Checks and requests the necessary permissions for BLE operations.
     */
    private fun checkAndRequestPermissions(name: String) {
        val permissionsNeeded = mutableListOf<String>()

        // Request necessary permissions for Android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // For Android versions below 12
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        // Location permission (needed for BLE scanning on Android < 12)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // If permissions are missing, request them
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            // Permissions granted, proceed with advertising
            startBleAdvertising(name)
        }
    }

    /**
     * Handles the result of the permission request.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, proceed
                startBleAdvertising("name")
            } else {
                Toast.makeText(this, "Permissions are required to start BLE advertising.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
