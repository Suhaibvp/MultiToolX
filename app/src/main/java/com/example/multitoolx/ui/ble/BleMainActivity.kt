package com.example.multitoolx.ui.ble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.multitoolx.databinding.ActivityBleMainBinding
import com.example.multitoolx.ui.ble.advertise.AdvertiseActivity
import com.example.multitoolx.ui.ble.client.ClientMainActivity

/**
 * BleMainActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Main entry screen for BLE-related functionalities in the MultiToolX app.
 * - Provides navigation to Advertise and Client modules.
 */

class BleMainActivity : ComponentActivity() {

    // ViewBinding instance for accessing layout views
    private lateinit var binding: ActivityBleMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityBleMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize button listeners
        setupListeners()
    }

    /**
     * Sets up click listeners for all BLE-related buttons.
     * Each listener triggers a new Activity based on the module selected.
     */
    private fun setupListeners() {

        // Navigate to AdvertiseActivity (Advertise Module)
        binding.btnAdvertise.setOnClickListener {
            println("Btn Advertise clicked")
            val intent = Intent(this, AdvertiseActivity::class.java)
            startActivity(intent)
        }

        // Navigate to ClientMainActivity (Client Module)
        binding.btnClient.setOnClickListener {
            println("Btn Client clicked")
            val intent = Intent(this, ClientMainActivity::class.java)
            startActivity(intent)
        }
    }
}
