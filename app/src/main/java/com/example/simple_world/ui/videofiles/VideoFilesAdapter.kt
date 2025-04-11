package com.example.simple_world.ui.videofiles

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simple_world.R
import java.io.File

class VideoFilesAdapter(
    private val videoPaths: List<String>,
    private val onItemClick:(String)->Unit
) :
    RecyclerView.Adapter<VideoFilesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(path: String) {
            val fileName = File(path).name
            itemView.findViewById<TextView>(R.id.videoFileName).text = fileName
            val imageView = itemView.findViewById<ImageView>(R.id.videoIcon)
            Glide.with(itemView.context)
                .load(Uri.fromFile(File(path)))
                .thumbnail(0.1f) // Optional: loads lower-res first
                .into(imageView)
            itemView.setOnClickListener {
                onItemClick(path)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videoPaths[position])
    }

    override fun getItemCount() = videoPaths.size
}
