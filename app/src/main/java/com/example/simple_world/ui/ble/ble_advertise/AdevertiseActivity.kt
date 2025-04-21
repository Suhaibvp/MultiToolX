//package com.example.simple_world.ui.ble.ble_advertise
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothGattCharacteristic
//import android.bluetooth.BluetoothGattDescriptor
//import android.bluetooth.BluetoothGattServer
//import android.bluetooth.BluetoothGattServerCallback
//import android.bluetooth.BluetoothGattService
//import android.bluetooth.BluetoothManager
//import android.bluetooth.le.AdvertiseData
//import android.bluetooth.le.AdvertiseSettings
//import android.bluetooth.le.AdvertiseCallback
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.ParcelUuid
//import android.util.Log
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.example.simple_world.R
//import com.example.simple_world.databinding.ActivityAdvertiseBinding
//import com.example.simple_world.services.ble.MyGattServerCallback
////import com.example.ble_manager.R
////import com.example.ble_manager.services.ble.MyGattServerCallback
//import java.util.UUID
//
//class AdvertiseActivity : AppCompatActivity() {
//    private val REQUEST_CODE_PERMISSIONS = 1001
//    private lateinit var binding: ActivityAdvertiseBinding
//
//    private lateinit var bluetoothManager: BluetoothManager
//    private var bluetoothGattServer: BluetoothGattServer? = null
//    private lateinit var currentServiceUUID: UUID
//    private lateinit var context: Context
//    private lateinit var gattServerCallback: BluetoothGattServerCallback
//    private val bundle = Bundle()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAdvertiseBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//println("activity started")
//        context = this
//        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) return
//binding.testButton.setOnClickListener {
//    println("test demo button")
//}
//        // Add characteristic button
//        binding.btnAddCharacteristic.setOnClickListener {
//            Log.d("BLE", "add characteristic clicked")
//            val characteristicView = layoutInflater.inflate(R.layout.characteristic_row, null)
//            binding.characteristicsContainer.addView(characteristicView)
//
//            val btnAdvanced = characteristicView.findViewById<Button>(R.id.btnAdvancedSettings)
//            val checkRead = characteristicView.findViewById<CheckBox>(R.id.checkRead)
//            val checkWrite = characteristicView.findViewById<CheckBox>(R.id.checkWrite)
//            val checkNotify = characteristicView.findViewById<CheckBox>(R.id.checkNotify)
//
//            btnAdvanced.setOnClickListener {
//                val dialog = AdvancedSettingsDialogFragment()
//                dialog.onSaveListener = { read, write, notify, readResponse, mappings ->
//                    checkRead.isChecked = read
//                    checkWrite.isChecked = write
//                    checkNotify.isChecked = notify
//
//                    updateCharacteristicConfig(read, write, notify, readResponse, mappings)
//                }
//                dialog.show(supportFragmentManager, "AdvancedSettingsDialog")
//            }
//        }
//
//        // Start advertise button
//        binding.startAdvertiseButton.setOnClickListener {
//            val bleName = binding.bleNameEditText.text.toString().trim()
//
//            when {
//                bleName.isEmpty() -> {
//                    Toast.makeText(this, "Please enter a BLE name", Toast.LENGTH_SHORT).show()
//                }
//                bleName.length > 20 -> {
//                    Toast.makeText(this, "BLE name must be 20 characters or less", Toast.LENGTH_SHORT).show()
//                }
//                else -> {
//                    val serviceUUID = binding.editServiceUUID.text.toString()
//                        .ifBlank { UUID.randomUUID().toString() }
//
//                    Log.d("BLE_SETUP", "Service UUID: $serviceUUID")
//
//                    for (i in 0 until binding.characteristicsContainer.childCount) {
//                        val charView = binding.characteristicsContainer.getChildAt(i)
//                        val charUUID = charView.findViewById<EditText>(R.id.editCharacteristicUUID).text.toString()
//                            .ifBlank { UUID.randomUUID().toString() }
//
//                        val canRead = charView.findViewById<CheckBox>(R.id.checkRead).isChecked
//                        val canWrite = charView.findViewById<CheckBox>(R.id.checkWrite).isChecked
//                        val canNotify = charView.findViewById<CheckBox>(R.id.checkNotify).isChecked
//                        val value = charView.findViewById<EditText>(R.id.editValue).text.toString()
//
//                        Log.d("BLE_SETUP", "Characteristic #$i")
//                        Log.d("BLE_SETUP", "UUID: $charUUID")
//                        Log.d("BLE_SETUP", "Properties -> Read: $canRead, Write: $canWrite, Notify: $canNotify")
//                        Log.d("BLE_SETUP", "Initial Value: $value")
//                    }
//
//                    validateOrGenerateServiceUUIDAndProceed(bleName)
//                }
//            }
//        }
//    }
//    private fun updateCharacteristicConfig(read:Boolean, write:Boolean, notify:Boolean, readResponse:String?,mappings:List<Pair<String, String>>){
//        bundle.putString("readresponse", readResponse)
//
//        val mappingList = ArrayList<String>().apply {
//            mappings.forEach { add("${it.first}:${it.second}") }
//        }
//        bundle.putStringArrayList("mappings", mappingList)
//
//    }
//    private fun validateOrGenerateServiceUUIDAndProceed(bleName: String) {
//        val serviceUUIDText = binding.editServiceUUID.text.toString().trim()
//
//        if (serviceUUIDText.isBlank()) {
//            // Generate random UUID
//            currentServiceUUID = UUID.randomUUID()
//            binding.editServiceUUID.setText(currentServiceUUID.toString())
//            println("Generated random UUID: $currentServiceUUID")
//            checkAndRequestPermissions(bleName)
//        } else {
//            try {
//                // Try to parse the UUID to check if it's valid
//                currentServiceUUID = UUID.fromString(serviceUUIDText)
//                println("Valid UUID provided: $currentServiceUUID")
//                checkAndRequestPermissions(bleName)
//            } catch (e: IllegalArgumentException) {
//                Toast.makeText(this, "Invalid UUID format. Please check and try again.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    fun startBleAdvertising(customName: String) {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
//            println("BLE advertising is not supported on this device.")
//            return
//        }
//
//        // Change BLE name
//        bluetoothAdapter.name = customName
//
//        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
//        val advertiseSettings = AdvertiseSettings.Builder()
//            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
//            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//            .setConnectable(true) // Should be true to allow connections to GATT
//            .build()
//
//        val advertiseData = AdvertiseData.Builder()
//            .setIncludeDeviceName(true)
//            .addServiceUuid(ParcelUuid(currentServiceUUID)) // <-- use actual UUID here
//            .build()
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//            println("Permission not granted for BLE advertise")
//            return
//        }
//        gattServerCallback= MyGattServerCallback(this,bundle)
//        // Setup GATT Server
//        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)
//        (gattServerCallback as MyGattServerCallback).setGattServer(bluetoothGattServer)
//
//        val serviceUUIDText = binding.editServiceUUID.text.toString().ifBlank { UUID.randomUUID().toString() }
//        currentServiceUUID = UUID.fromString(serviceUUIDText)
//        val gattService = BluetoothGattService(currentServiceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
//
//        for (i in 0 until binding.characteristicsContainer.childCount) {
//            val charView = binding.characteristicsContainer.getChildAt(i)
//
//            val uuidText = charView.findViewById<EditText>(R.id.editCharacteristicUUID).text.toString()
//                .ifBlank { UUID.randomUUID().toString() }
//            val charUUID = UUID.fromString(uuidText)
//
//            val canRead = charView.findViewById<CheckBox>(R.id.checkRead).isChecked
//            val canWrite = charView.findViewById<CheckBox>(R.id.checkWrite).isChecked
//            val canNotify = charView.findViewById<CheckBox>(R.id.checkNotify).isChecked
//            val value = charView.findViewById<EditText>(R.id.editValue).text.toString()
//
//            // Calculate properties
//            var properties = 0
//            var permissions = 0
//            if (canRead) {
//                properties = properties or BluetoothGattCharacteristic.PROPERTY_READ
//                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_READ
//            }
//            if (canWrite) {
//                properties = properties or BluetoothGattCharacteristic.PROPERTY_WRITE
//                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_WRITE
//            }
//            if (canNotify) {
//                properties = properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY
//                permissions = permissions or BluetoothGattCharacteristic.PERMISSION_READ
//            }
//            val characteristic = BluetoothGattCharacteristic(charUUID, properties, permissions)
//
//            if (canNotify) {
//                // Add CCCD descriptor to enable notifications
//                val cccd = BluetoothGattDescriptor(
//                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),  // Standard CCCD UUID
//                    BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
//                )
//                characteristic.addDescriptor(cccd)
//            }
//
//
//            if (value.isNotBlank()) {
//                characteristic.value = value.toByteArray()
//            }
//
//            gattService.addCharacteristic(characteristic)
//        }
//
//        bluetoothGattServer?.addService(gattService)
//
//        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, object : AdvertiseCallback() {
//            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
//                println("✅ Advertising started successfully.")
//                Toast.makeText(this@AdvertiseActivity,"Advertise Started Successfully",Toast.LENGTH_LONG).show()
//            }
//
//            override fun onStartFailure(errorCode: Int) {
//                println("❌ Advertising failed with error code: $errorCode")
//                Toast.makeText(this@AdvertiseActivity,"Failed to Advertise",Toast.LENGTH_LONG).show()
//            }
//        })
//    }
//
//    private fun checkAndRequestPermissions(name:String) {
//        val permissionsNeeded = mutableListOf<String>()
//
//        // Check for Bluetooth permissions
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_ADVERTISE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE)
//        }
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
//        }
//
//        // Check for Bluetooth permissions
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionsNeeded.add(Manifest.permission.BLUETOOTH)
//        }
//
//        // Check for Location permission
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//
//        // If any permissions are missing, request them
//        if (permissionsNeeded.isNotEmpty()) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsNeeded.toTypedArray(),
//                REQUEST_CODE_PERMISSIONS
//            )
//        } else {
//            // If permissions are already granted, start advertising
//            startBleAdvertising(name)
//        }
//    }
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            // Check if all permissions are granted
//            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                // Permissions granted, start advertising
//                //startBleAdvertising(name)
//            } else {
//                // Permissions denied, show a message to the user
//                Toast.makeText(this, "Permissions are required to start BLE advertising.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}
