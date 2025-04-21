package com.example.simple_world.ui.ble.client

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.simple_world.R
import com.example.simple_world.databinding.ActivityBleClientBinding

class ClientMainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_client)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, ScanFragment())
                .commit()
        }
    }

    fun openDeviceDetail(device: BluetoothDevice) {
        val fragment = DeviceDetailFragment.newInstance(device.name, device.address)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }
}