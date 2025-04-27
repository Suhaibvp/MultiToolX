package com.example.multitoolx.ui.videofiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multitoolx.R
import com.example.multitoolx.ui.VideoPlayActivity

/**
 * VideoFilesActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Activity that displays a list of video files and allows the user to play a selected video.
 * - The video paths are passed through an Intent and displayed in a RecyclerView.
 */
class VideoFilesActivity : AppCompatActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_files)

        // Get video paths passed via the Intent
        val videoPaths = intent.getStringArrayListExtra("videoPaths") ?: arrayListOf()

        // Initialize RecyclerView and its adapter
        val recyclerView = findViewById<RecyclerView>(R.id.videoFilesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up the adapter with the video paths and item click listener
        val adapter = VideoFilesAdapter(videoPaths) { path ->
            // Open the selected video in a new activity
            val intent = Intent(this, VideoPlayActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                setDataAndType(Uri.parse("file://$path"), "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        }

        // Attach the adapter to the RecyclerView
        recyclerView.adapter = adapter
    }
}
