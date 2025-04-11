package com.example.simple_world.ui.videofiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_world.R
import com.example.simple_world.ui.VideoPlayActivity

class VideoFilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_files)

        val videoPaths = intent.getStringArrayListExtra("videoPaths") ?: arrayListOf()

        val recyclerView = findViewById<RecyclerView>(R.id.videoFilesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = VideoFilesAdapter(videoPaths) { path ->
            val intent = Intent(this, VideoPlayActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                setDataAndType(Uri.parse("file://$path"), "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            this.startActivity(intent)
        }
        recyclerView.adapter = adapter


    }
}
