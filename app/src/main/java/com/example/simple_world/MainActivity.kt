package com.example.simple_world

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.simple_world.ui.pdf.PdfGeneratorActivity
import com.example.simple_world.ui.videolistpage.VideoListActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val openVideoPlayerButton = findViewById<Button>(R.id.button_video_page)
        val openPdfGeneratorButton=findViewById<Button>(R.id.button_pdf_page)
        val openunityGameButton=findViewById<Button>(R.id.button_unity_game)
        openVideoPlayerButton.setOnClickListener {
            println("start activity called")
            val intent = Intent(this, VideoListActivity::class.java)
            startActivity(intent)
        }
        openPdfGeneratorButton.setOnClickListener {
            val intent=Intent(this,PdfGeneratorActivity::class.java)
            startActivity(intent)
        }
        openunityGameButton.setOnClickListener {
            val intent = Intent(this, com.unity3d.player.UnityPlayerActivity::class.java)
            startActivity(intent)
        }



    }
}



