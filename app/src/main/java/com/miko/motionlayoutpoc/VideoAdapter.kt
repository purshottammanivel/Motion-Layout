package com.miko.motionlayoutpoc

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.miko.motionlayoutpoc.databinding.ItemVideoBinding

class VideoAdapter(
    private val context: Context,
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var videoUris: List<Uri>? = null
    private var videoAlbums: List<String>? = null
    private var displayNames: List<String>? = null
    private var onVideoClickListener: OnVideoClickListener? = null

    fun setAdapter(videoUris: List<Uri>, videoAlbums: List<String>, displayNames: List<String>) {
        this.videoUris = videoUris
        this.videoAlbums = videoAlbums
        this.displayNames = displayNames
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVideoBinding.inflate(inflater, parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = videoUris?.size ?: 0

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val videoUri = videoUris?.get(position)
            val videoAlbum = videoAlbums?.get(position)
            val displayName = displayNames?.get(position)

            binding.tvVideoName.text = videoAlbum.toString()
            // binding.tvDuration.text = videoAlbum.toString()
            binding.tvDisplayName.text = displayName.toString()
            binding.root.setOnClickListener {
                Log.d("QWERTY", "bind: clicked position -> $position")
                onVideoClickListener?.onVideoClick(position)
            }
        }
    }

    fun setOnVideoClickListener(onVideoClickListener: OnVideoClickListener) {
        this.onVideoClickListener = onVideoClickListener
    }

    interface OnVideoClickListener {
        fun onVideoClick(position: Int)
    }
}