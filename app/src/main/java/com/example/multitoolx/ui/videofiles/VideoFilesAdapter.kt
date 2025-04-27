package com.example.multitoolx.ui.videofiles

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.multitoolx.R
import java.io.File

/**
 * VideoFilesAdapter.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Adapter for displaying video files in a RecyclerView.
 * - Each item shows the video file name and a thumbnail.
 */

class VideoFilesAdapter(
    private val videoPaths: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<VideoFilesAdapter.ViewHolder>() {

    /**
     * ViewHolder class to bind each video item to the view
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Binds the video file to the item view
        fun bind(path: String) {
            // Set the video file name
            val fileName = File(path).name
            itemView.findViewById<TextView>(R.id.videoFileName).text = fileName

            // Set the video thumbnail using Glide
            val imageView = itemView.findViewById<ImageView>(R.id.videoIcon)
            Glide.with(itemView.context)
                .load(Uri.fromFile(File(path)))
                .thumbnail(0.1f) // Optional: loads lower-res first
                .into(imageView)

            // Set click listener for the item
            itemView.setOnClickListener {
                onItemClick(path)
            }
        }
    }

    /**
     * Inflates the view for each item in the RecyclerView
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_file, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the data to the view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videoPaths[position])
    }

    /**
     * Returns the number of video items
     */
    override fun getItemCount() = videoPaths.size
}
