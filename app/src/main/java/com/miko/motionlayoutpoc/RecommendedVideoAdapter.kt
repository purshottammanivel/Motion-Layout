package com.miko.motionlayoutpoc

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miko.motionlayoutpoc.databinding.ItemRecommendedVideoBinding

class RecommendedVideoAdapter
    : RecyclerView.Adapter<RecommendedVideoAdapter.RecommendedVideoViewHolder>() {

    private var videoModels: List<VideoModel>? = null
    private var onRecommendedVideoClickListener: OnRecommendedVideoClickListener? = null

    fun setVideos(
        videoModels: List<VideoModel>?
    ) {
        this.videoModels = videoModels
    }

    /*fun updateSelectedPosition(newPosition: Int) {
        if (videoModels.isNullOrEmpty()) return
        if (newPosition !in videoModels!!.indices) return

        val oldPosition = selectedPosition
        selectedPosition = newPosition

        oldPosition?.takeIf { it != newPosition }?.let {
            notifyItemChanged(it)
        }
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedVideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecommendedVideoBinding.inflate(inflater, parent, false)
        return RecommendedVideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendedVideoViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return videoModels?.size ?: 0
    }

    inner class RecommendedVideoViewHolder(
        private val binding: ItemRecommendedVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val video = videoModels?.get(position) ?: return

            setVideoThumbnail(videoModels?.get(position)!!.thumbnail)
            setVideoDuration(formatDuration(video.duration))
        }

        private fun setVideoThumbnail(bitmap: Bitmap) {
            binding.imvVideoThumbnail.setImageBitmap(bitmap)
        }

        private fun setVideoDuration(duration: String) {
            binding.tvDuration.text = duration
        }

        private fun formatDuration(durationMillis: Long): String {
            val totalSeconds = durationMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun setOnRecommendedVideoClickListener(
        onRecommendedVideoClickListener: OnRecommendedVideoClickListener
    ) {
        this.onRecommendedVideoClickListener = onRecommendedVideoClickListener
    }

    interface OnRecommendedVideoClickListener {
        fun onRecommendedVideoClick(position: Int)
    }
}