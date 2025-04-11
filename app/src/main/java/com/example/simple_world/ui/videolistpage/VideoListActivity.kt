package com.example.simple_world.ui.videolistpage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_world.R
import com.example.simple_world.model.VideoFolder
import com.example.simple_world.ui.videofiles.VideoFilesActivity
import java.io.File

class VideoListActivity : AppCompatActivity() {
    private lateinit var viewModel: VideoFolderViewModel
    private lateinit var adapter: VideoFolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_video)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        val recyclerView = findViewById<RecyclerView>(R.id.videoFolderRecyclerView)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, RecyclerView.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VideoFolderAdapter(emptyList()) { folder ->
            val intent = Intent(this, VideoFilesActivity::class.java)
            intent.putStringArrayListExtra("videoPaths", ArrayList(folder.videoPaths))
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[VideoFolderViewModel::class.java]

        viewModel.videoFolders.observe(this) { folders ->
            adapter.updateData(folders)
        }
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isEmpty.observe(this) { isEmpty ->
            emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }


        checkAndRequestPermissions()
    }
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
