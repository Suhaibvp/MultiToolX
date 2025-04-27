package com.example.multitoolx.ui.ble.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.multitoolx.R
import com.example.multitoolx.databinding.ActivityBleClientBinding

/**
 * ClientMainActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Main activity for BLE Client functionality.
 * - Handles fragment transitions for scanning BLE devices and viewing device details.
 */
class ClientMainActivity : AppCompatActivity() {

    /**
     * Called when the activity is created.
     * - Sets the content view and initializes the first fragment (ScanFragment).
     * - If no saved instance, replace fragment container with the ScanFragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_client)

        // Only replace fragment if savedInstanceState is null (first-time launch)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, ScanFragment())  // Replace with ScanFragment
                .commit()
        }
    }

    /**
     * Navigates to the DeviceDetailFragment, showing detailed info about a selected Bluetooth device.
     *
     * @param device The Bluetooth device selected to view details.
     */
    fun openDeviceDetail(device: BluetoothDevice) {
        // Create new instance of DeviceDetailFragment and pass device info as arguments
         if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
             val fragment = DeviceDetailFragment.newInstance(device.name, device.address)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)  // Replace with DeviceDetailFragment
                .addToBackStack(null)  // Allow back navigation
                .commit()

        }else{
            Toast.makeText(this,"Permission not provided ",Toast.LENGTH_LONG).show()
         }

        // Replace the current fragment with DeviceDetailFragment and add it to the back stack

    }
}
