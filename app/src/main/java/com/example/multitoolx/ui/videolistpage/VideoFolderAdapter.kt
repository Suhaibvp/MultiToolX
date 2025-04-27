package com.example.multitoolx.ui.videolistpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.multitoolx.model.VideoFolder
import com.example.multitoolx.R

/**
 * VideoFolderAdapter.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Adapter for displaying video folders in a RecyclerView.
 * - Each item shows the folder name, video count, and folder path.
 */
class VideoFolderAdapter(
    private var folderList: List<VideoFolder>,
    private val onItemClick: (VideoFolder) -> Unit
) : RecyclerView.Adapter<VideoFolderAdapter.VideoFolderViewHolder>() {

    /**
     * ViewHolder class to bind each folder item to the view.
     */
    inner class VideoFolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderNameTextView)
        val videoCountTextView: TextView = itemView.findViewById(R.id.videoCountTextView)
        val videoPathTextView: TextView = itemView.findViewById(R.id.folderPathTextView)
    }

    /**
     * Inflates the view for each folder item in the RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_folder, parent, false)
        return VideoFolderViewHolder(view)
    }

    /**
     * Binds the folder data to the view.
     */
    override fun onBindViewHolder(holder: VideoFolderViewHolder, position: Int) {
        val folder = folderList[position]
        holder.folderNameTextView.text = folder.folderName
        holder.videoCountTextView.text = "${folder.videoCount} video(s)"
        holder.videoPathTextView.text = folder.folderPath

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClick(folder)
        }
    }

    /**
     * Returns the number of video folders in the list.
     */
    override fun getItemCount(): Int = folderList.size

    /**
     * Updates the data in the adapter with a new list and notifies the RecyclerView to refresh.
     */
    fun updateData(newList: List<VideoFolder>) {
        folderList = newList
        notifyDataSetChanged()
    }
}
