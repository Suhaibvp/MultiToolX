package com.example.multitoolx
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.multitoolx.databinding.ActivityMainBinding
import com.example.multitoolx.ui.api.ApiConfigActivity
import com.example.multitoolx.ui.pdf.PdfGeneratorActivity
import com.example.multitoolx.ui.videolistpage.VideoListActivity
import com.example.multitoolx.ui.ble.BleMainActivity

/**
 * MainActivity.kt
 *
 * Created on: 2025-04-12
 * Author: Suhaib VP
 * Description:
 * - Main entry screen for MultiToolX app.
 * - Provides navigation to Video Player, PDF Generator, Unity Game (API Config), and BLE Manager modules.
 */

class MainActivity : ComponentActivity() {

    // ViewBinding instance for accessing layout views
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding (2025-04-27)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize button listeners (2025-04-27)
        setupListeners()
    }

    /**
     * Sets up click listeners for all navigation buttons.
     * Each listener triggers a new Activity based on the module selected.
     */
    private fun setupListeners() {

        // Navigate to VideoListActivity (Video Player Module) (2025-04-27)
        binding.buttonVideoPage.setOnClickListener {
            startActivity(Intent(this, VideoListActivity::class.java))
        }

        // Navigate to PdfGeneratorActivity (PDF Generator Module) (2025-04-27)
        binding.buttonPdfPage.setOnClickListener {
            startActivity(Intent(this, PdfGeneratorActivity::class.java))
        }

        // Navigate to ApiConfigActivity (API Config Module) (2025-04-27)
        binding.buttonUnityGame.setOnClickListener {
            startActivity(Intent(this, ApiConfigActivity::class.java))

            // Note: If integrating Unity, replace with UnityPlayerActivity
            // Example:
            // startActivity(Intent(this, com.unity3d.player.UnityPlayerActivity::class.java))
        }

        // Navigate to BleMainActivity (Bluetooth BLE Manager Module) (2025-04-27)
        binding.buttonBleManager.setOnClickListener {
            startActivity(Intent(this, BleMainActivity::class.java))

            // Alternative approach using explicit class name (commented):
            // val intent = Intent().setClassName(this, "com.example.ble_manager.MainActivity")
            // startActivity(intent)
        }
    }
}
