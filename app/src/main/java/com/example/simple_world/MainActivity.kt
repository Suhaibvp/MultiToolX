package com.example.simple_world

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.simple_world.databinding.ActivityMainBinding
import com.example.simple_world.ui.api.ApiConfigActivity
import com.example.simple_world.ui.pdf.PdfGeneratorActivity
import com.example.simple_world.ui.videolistpage.VideoListActivity
import com.example.simple_world.ui.ble.BleMainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access the buttons via ViewBinding
        val openVideoPlayerButton = binding.buttonVideoPage
        val openPdfGeneratorButton = binding.buttonPdfPage
        val openUnityGameButton = binding.buttonUnityGame
        val bleManagerButton = binding.buttonBleManager

        openVideoPlayerButton.setOnClickListener {
            val intent = Intent(this, VideoListActivity::class.java)
            startActivity(intent)
        }

        openPdfGeneratorButton.setOnClickListener {
            val intent = Intent(this, PdfGeneratorActivity::class.java)
            startActivity(intent)
        }

        openUnityGameButton.setOnClickListener {
            //val intent = Intent(this, com.unity3d.player.UnityPlayerActivity::class.java)
            val intent=Intent(this,ApiConfigActivity::class.java)
            startActivity(intent)
        }

        bleManagerButton.setOnClickListener {
            val intent =Intent(this,BleMainActivity::class.java)
            startActivity(intent)
//            intent.setClassName(this, "com.example.ble_manager.MainActivity")
//            startActivity(intent)
        }
    }
}
