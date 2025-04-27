package com.example.multitoolx.ui.videolistpage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multitoolx.R
import com.example.multitoolx.model.VideoFolder
import com.example.multitoolx.ui.videofiles.VideoFilesActivity

/**
 * VideoListActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Displays a list of video folders found in external storage.
 * - Handles permissions for reading videos (Android 13+ and below).
 * - Navigates to VideoFilesActivity when a folder is selected.
 */
class VideoListActivity : AppCompatActivity() {
    private lateinit var viewModel: VideoFolderViewModel
    private lateinit var adapter: VideoFolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_video)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        // RecyclerView setup
        val recyclerView = findViewById<RecyclerView>(R.id.videoFolderRecyclerView)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, RecyclerView.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VideoFolderAdapter(emptyList()) { folder ->
            // Open VideoFilesActivity when a folder is clicked
            val intent = Intent(this, VideoFilesActivity::class.java)
            intent.putStringArrayListExtra("videoPaths", ArrayList(folder.videoPaths))
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // ViewModel initialization
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[VideoFolderViewModel::class.java]

        // Observe LiveData for video folders
        viewModel.videoFolders.observe(this) { folders ->
            adapter.updateData(folders)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe empty state
        viewModel.isEmpty.observe(this) { isEmpty ->
            emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        // Check and request permissions
        checkAndRequestPermissions()
    }

    /**
     * Checks if the required permissions are granted and requests them if needed.
     */
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_VIDEO), 100)
            } else {
                viewModel.loadVideoFolders()
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
            } else {
                viewModel.loadVideoFolders()
            }
        }
    }
}
