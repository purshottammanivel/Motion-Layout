package com.miko.motionlayoutpoc

import android.content.ContentUris
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.miko.motionlayoutpoc.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    private var _binding: ActivityVideoBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    private var player: ExoPlayer? = null

    private val recommendedVideos = mutableListOf<VideoModel>()
    private var videoAdapter: RecommendedVideoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUriString = intent.getStringExtra("video_uri")
        val videoUri = videoUriString?.let { Uri.parse(it) }

        if (videoUri != null) {
            initializePlayer(videoUri)
        }

        loadRecommendedVideos()
    }

    override fun onStart() {
        super.onStart()
        /*binding.exoPlayer.setOnClickListener {
            binding.motionLayout.transitionToEnd()
        }*/

        binding.exoPlayer.setOnClickListener {
            val motionLayout = binding.motionLayout
            val currentState = motionLayout.currentState

            if (currentState == R.id.start) {
                motionLayout.transitionToEnd()
            } else {
                motionLayout.transitionToStart()
            }
        }
    }

    private fun initializePlayer(videoUri: Uri) {
        player = ExoPlayer.Builder(this).build()
        binding.exoPlayer.player = player
        val mediaItem = MediaItem.fromUri(videoUri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    override fun onResume() {
        super.onResume()
        val motionLayout = binding.motionLayout
        /*motionLayout.transitionToEnd()
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                when (currentId) {
                    R.id.start -> {
                        binding.recommendedList.gone()
                        binding.exoPlayer.hideController()
                    }
                    R.id.end -> {
                        binding.recommendedList.visible()
                        binding.exoPlayer.showController()
                    }
                }
            }

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int) {
                Log.d("QWERTY", "onTransitionStarted: ")
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float) {
                Log.d("QWERTY", "onTransitionChange: progress -> $progress")
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float) {
                Log.d("QWERTY", "onTransitionTrigger: progress -> $progress | positive -> $positive")
            }
        })*/

        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int,
                progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                if (currentId == R.id.end) {
                    Log.d("QWERTY-Motion", "Shrink completed - Recommendations visible")
                } else if (currentId == R.id.start) {
                    Log.d("QWERTY-Motion", "Expanded to full screen")
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}
        })
    }

    private fun loadRecommendedVideos() {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(collection, projection, null, null, sortOrder)

        recommendedVideos.clear()

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val albumColumn = it.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val durationColumn = it.getColumnIndex(MediaStore.Video.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                val album = if (albumColumn != -1) it.getString(albumColumn) ?: "Unknown" else "Unknown"
                val duration = if (durationColumn != -1) it.getLong(durationColumn) else 0L

                val thumbnail = getVideoThumbnail(uri)

                /*val thumbnail = try {
                    ThumbnailUtils.createVideoThumbnail(
                        this.contentResolver,
                        uri,
                        Size(240, 240), // Adjust size as needed
                        null
                    )
                } catch (e: Exception) {
                    null
                }*/

                if (thumbnail != null) {
                    recommendedVideos.add(VideoModel(uri, thumbnail, duration, album))
                }
            }
        }

        videoAdapter = RecommendedVideoAdapter()
        binding.recommendedList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recommendedList.adapter = videoAdapter
        videoAdapter?.setVideos(recommendedVideos)

        videoAdapter?.setOnRecommendedVideoClickListener(object : RecommendedVideoAdapter.OnRecommendedVideoClickListener {
            override fun onRecommendedVideoClick(position: Int) {
                val selectedVideo = recommendedVideos[position]
                playVideoAtUri(selectedVideo.uri)
            }
        })
    }

    private fun getVideoThumbnail(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(this, uri)
            retriever.getFrameAtTime(0) // Get first frame as thumbnail
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    private fun playVideoAtUri(uri: Uri) {
        player?.let {
            it.setMediaItem(MediaItem.fromUri(uri))
            it.prepare()
            it.play()
        } ?: run {
            // If player is null, initialize it first
            player = ExoPlayer.Builder(this).build()
            binding.exoPlayer.player = player
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.play()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}