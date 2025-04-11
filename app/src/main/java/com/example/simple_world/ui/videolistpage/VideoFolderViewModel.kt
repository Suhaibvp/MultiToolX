package com.example.simple_world.ui.videolistpage

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.simple_world.model.VideoFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

class VideoFolderViewModel(application: Application) : AndroidViewModel(application) {
    private val _videoFolders = MutableLiveData<List<VideoFolder>>()
    val videoFolders: LiveData<List<VideoFolder>> get() = _videoFolders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    fun loadVideoFolders() {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val folders = getVideoFolders(Environment.getExternalStorageDirectory())
            _videoFolders.postValue(folders)
            _isLoading.postValue(false)
            _isEmpty.postValue(folders.isEmpty())
        }
    }

    private fun getVideoFolders(baseDir: File): List<VideoFolder> {
        val folders = mutableListOf<VideoFolder>()
        collectVideoFoldersRecursively(baseDir, folders)
        return folders
    }

    private fun collectVideoFoldersRecursively(dir: File, result: MutableList<VideoFolder>) {
        val subDirs = dir.listFiles(FileFilter(File::isDirectory)) ?: return


        for (folder in subDirs) {
            // Only collect videos directly inside this folder
            val videoPaths = collectVideosInThisFolderOnly(folder)

            if (videoPaths.isNotEmpty()) {
                val folderData = VideoFolder(
                    folderPath = folder.absolutePath,
                    folderName = folder.name,
                    videoCount = videoPaths.size,
                    videoPaths = videoPaths
                )
                result.add(folderData)
            }

            // Continue checking its subfolders
            collectVideoFoldersRecursively(folder, result)
        }
    }

    // ✅ Only returns videos that are directly inside this folder
    private fun collectVideosInThisFolderOnly(dir: File): List<String> {
        val files = dir.listFiles() ?: return emptyList()
        return files.filter { it.isFile && isVideoFile(it) }
            .map { it.absolutePath }
    }

    private fun isVideoFile(file: File): Boolean {
        val extensions = listOf("mp4", "mkv", "avi", "mov")
        return extensions.contains(file.extension.lowercase())
    }
}
