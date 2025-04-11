package com.example.simple_world.model

data class VideoFolder(
    val folderPath: String,
    val folderName: String,
    val videoCount: Int,
    val videoPaths: List<String> // optional if you want to show a preview later
)
