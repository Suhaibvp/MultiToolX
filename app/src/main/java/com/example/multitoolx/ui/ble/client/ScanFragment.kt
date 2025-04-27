package com.example.multitoolx.ui.ble.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.multitoolx.R

/**
 * ScanFragment.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Handles BLE device scanning.
 * - Displays a list of available devices.
 * - Navigates to a detail page when a device is selected.
 */

class ScanFragment : Fragment() {

    // ViewBinding instance for accessing layout views
    private lateinit var scanButton: Button
    private lateinit var deviceListView: ListView

    // List to store scanned devices
    private val scannedDevices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>

    // Bluetooth adapter initialization
    private val bluetoothAdapter by lazy {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                startScan()  // Start scanning if permissions are granted
            } else {
                Toast.makeText(requireContext(), "Bluetooth permissions are required to scan devices", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        // Initialize views using ViewBinding
        scanButton = view.findViewById(R.id.scanButton)
        deviceListView = view.findViewById(R.id.deviceListView)

        // Set up the adapter for displaying device names
        deviceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        deviceListView.adapter = deviceAdapter

        // Set up the scan button listener
        scanButton.setOnClickListener {
            if (checkPermissions()) {
                startScan()
            } else {
                requestPermissions()
            }
        }

        // Set listener for item clicks on device list
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = scannedDevices[position]
            (activity as? ClientMainActivity)?.openDeviceDetail(selectedDevice)
        }

        return view
    }

    // Check if Bluetooth permissions are granted
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Request Bluetooth permissions if not granted
    private fun requestPermissions() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    }

    // Start BLE scan for devices
    private fun startScan() {
        scannedDevices.clear()
        deviceAdapter.clear()

        val scanner = bluetoothAdapter.bluetoothLeScanner
        scanner.startScan(scanCallback)

        // Stop scanning after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
        }, 5000)
    }

    // BLE scan callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && !scannedDevices.contains(device)) {
                scannedDevices.add(device)
                deviceAdapter.add("${device.name} - ${device.address}")
                deviceAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(requireContext(), "Scan failed with error code: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }
}
