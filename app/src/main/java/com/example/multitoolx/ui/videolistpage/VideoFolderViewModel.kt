package com.example.multitoolx.ui.videolistpage

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.multitoolx.model.VideoFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

/**
 * VideoFolderViewModel.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - ViewModel for loading and managing video folders.
 * - Loads video folders recursively from external storage.
 * - Exposes LiveData for UI updates (loading, empty state, list of folders).
 */
class VideoFolderViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to hold the list of video folders
    private val _videoFolders = MutableLiveData<List<VideoFolder>>()
    val videoFolders: LiveData<List<VideoFolder>> get() = _videoFolders

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to track whether no folders were found
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    /**
     * Load video folders from external storage directory.
     */
    fun loadVideoFolders() {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            // Get video folders by recursively scanning storage
            val folders = getVideoFolders(Environment.getExternalStorageDirectory())
            _videoFolders.postValue(folders)
            _isLoading.postValue(false)
            _isEmpty.postValue(folders.isEmpty()) // Set empty state based on result
        }
    }

    /**
     * Recursively collect video folders starting from a base directory.
     */
    private fun getVideoFolders(baseDir: File): List<VideoFolder> {
        val folders = mutableListOf<VideoFolder>()
        collectVideoFoldersRecursively(baseDir, folders)
        return folders
    }

    /**
     * Recursively checks subdirectories and adds video folders to the result list.
     */
    private fun collectVideoFoldersRecursively(dir: File, result: MutableList<VideoFolder>) {
        val subDirs = dir.listFiles(FileFilter(File::isDirectory)) ?: return

        for (folder in subDirs) {
            // Only collect videos that are directly inside this folder
            val videoPaths = collectVideosInThisFolderOnly(folder)

            if (videoPaths.isNotEmpty()) {
                // Create a VideoFolder object and add it to the result list
                val folderData = VideoFolder(
                    folderPath = folder.absolutePath,
                    folderName = folder.name,
                    videoCount = videoPaths.size,
                    videoPaths = videoPaths
                )
                result.add(folderData)
            }

            // Continue checking subfolders recursively
            collectVideoFoldersRecursively(folder, result)
        }
    }

    /**
     * Collects only the video files directly inside this folder (not in subfolders).
     */
    private fun collectVideosInThisFolderOnly(dir: File): List<String> {
        val files = dir.listFiles() ?: return emptyList()
        return files.filter { it.isFile && isVideoFile(it) }
            .map { it.absolutePath }
    }

    /**
     * Checks if a file is a video file by its extension.
     */
    private fun isVideoFile(file: File): Boolean {
        val extensions = listOf("mp4", "mkv", "avi", "mov")
        return extensions.contains(file.extension.lowercase())
    }
}
