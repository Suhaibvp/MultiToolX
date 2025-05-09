package com.example.multitoolx.model

/**
 * Data class representing a video folder.
 *
 * @param folderPath The path to the folder containing the videos.
 * @param folderName The display name of the folder.
 * @param videoCount The number of video files in the folder.
 * @param videoPaths An optional list of paths to the video files.
 *                   This is used to show a preview of the videos in the folder.
 *                   If not provided, it can be left empty or null.
 */
data class VideoFolder(
    val folderPath: String,    // The absolute path of the video folder on the device.
    val folderName: String,    // The name of the folder (e.g., "Movies", "Music Videos").
    val videoCount: Int,       // The total number of video files in the folder.
    val videoPaths: List<String>? = null // A nullable list of video file paths for preview purposes.
)
