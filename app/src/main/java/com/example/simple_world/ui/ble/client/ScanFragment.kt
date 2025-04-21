package com.example.simple_world.ui.ble.client

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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.simple_world.R

class ScanFragment : Fragment() {

    private lateinit var scanButton: Button
    private lateinit var deviceListView: ListView

    private val scannedDevices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>

    private val bluetoothAdapter by lazy {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        scanButton = view.findViewById(R.id.scanButton)
        deviceListView = view.findViewById(R.id.deviceListView)

        deviceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        deviceListView.adapter = deviceAdapter

        scanButton.setOnClickListener {
            startScan()
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = scannedDevices[position]
            (activity as? ClientMainActivity)?.openDeviceDetail(selectedDevice)
        }

        return view
    }

    private fun startScan() {
        scannedDevices.clear()
        deviceAdapter.clear()

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        scanner.startScan(scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
        }, 5000)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (device.name != null && !scannedDevices.contains(device)) {
                scannedDevices.add(device)
                deviceAdapter.add("${device.name} - ${device.address}")
                deviceAdapter.notifyDataSetChanged()
            }
        }
    }
}
