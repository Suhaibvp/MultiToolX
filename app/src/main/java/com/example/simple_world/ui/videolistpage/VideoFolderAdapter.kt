package com.example.simple_world.ui.videolistpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_world.R
import com.example.simple_world.model.VideoFolder

class VideoFolderAdapter(
    private var folderList: List<VideoFolder>,
    private val onItemClick: (VideoFolder) -> Unit
) : RecyclerView.Adapter<VideoFolderAdapter.VideoFolderViewHolder>() {

    inner class VideoFolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderNameTextView)
        val videoCountTextView: TextView = itemView.findViewById(R.id.videoCountTextView)
        val videoPathTextView:TextView=itemView.findViewById(R.id.folderPathTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_folder, parent, false)
        return VideoFolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoFolderViewHolder, position: Int) {
        val folder = folderList[position]
        holder.folderNameTextView.text = folder.folderName
        holder.videoCountTextView.text = "${folder.videoCount} video(s)"
        holder.videoPathTextView.text=folder.folderPath

        holder.itemView.setOnClickListener {
            onItemClick(folder) // this is safe now
        }
    }

    override fun getItemCount(): Int = folderList.size

    fun updateData(newList: List<VideoFolder>) {
        folderList = newList
        notifyDataSetChanged()
    }
}

