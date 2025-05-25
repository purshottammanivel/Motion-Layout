package com.miko.motionlayoutpoc

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.ContentUris
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.miko.motionlayoutpoc.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    private var _binding: ActivityVideoBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    private var exoPlayer: ExoPlayer? = null

    private val recommendedVideos = mutableListOf<VideoModel>()
    private var videoAdapter: RecommendedVideoAdapter? = null

    private var currentPlayerState = VideoPlayerState.EXPANDED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // supportActionBar?.hide()

        val videoUriString = intent.getStringExtra("video_uri")
        val videoUri = videoUriString?.let { Uri.parse(it) }

        if (videoUri != null) {
            initializePlayer(videoUri)
        }

        loadRecommendedVideos()
    }

    override fun onStart() {
        super.onStart()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        binding.exoPlayer.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.performClick()
                toggleShrinkExpand()
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
        setupMotionLayoutListener()
        setupExoPlayerPlaybackListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isInPictureInPictureMode) {
            enterPipMode()
        }
    }

    private fun enterPipMode() {
        val aspectRatio = Rational(16, 9)
        val pipBuilder = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .build()
        enterPictureInPictureMode(pipBuilder)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        when (isInPictureInPictureMode) {
            true -> { Log.d("QWERTY", "isInPIPMode-true") }
            false -> { Log.d("QWERTY", "isInPIPMode-false") }
        }
    }

    private fun toggleShrinkExpand() {
        val motionLayout = binding.motionLayout
        val currentState = motionLayout.currentState

        if (currentState == R.id.start) {
            binding.exoPlayer.useController = true
            binding.exoPlayer.controllerShowTimeoutMs = 0 // controller stays visible
            binding.exoPlayer.showController()
            motionLayout.transitionToEnd()
            currentPlayerState = VideoPlayerState.SHRUNK
        } else {
            binding.exoPlayer.useController = false
            binding.exoPlayer.hideController()
            motionLayout.transitionToStart()
            currentPlayerState = VideoPlayerState.EXPANDED
        }
    }

    private fun initializePlayer(videoUri: Uri) {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoPlayer.player = exoPlayer
        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    private fun setupMotionLayoutListener() {
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                if (currentId == R.id.end) {
                    // binding.progressbar.visibility = View.GONE
                    // Log.d("QWERTY-Motion", "Shrink completed - Recommendations visible")
                } else if (currentId == R.id.start) {
                    // Log.d("QWERTY-Motion", "Expanded to full screen")
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })
    }

    private fun setupExoPlayerPlaybackListeners() {
        binding.exoPlayer.player?.addListener(object : Player.Listener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onPlaybackStateChanged(state: Int) {
                runOnUiThread {
                    when (state) {
                        Player.STATE_ENDED -> {
                            Log.d("QWERTY", "onPlaybackStateChanged: STATE_ENDED")
                        }

                        Player.STATE_BUFFERING -> {
                            Log.d("QWERTY", "onPlaybackStateChanged: STATE_BUFFERING")
                            // binding.progressbar.visibility = View.VISIBLE
                        }

                        Player.STATE_READY -> {
                            Log.d("QWERTY", "STATE_READY reached, hiding progressbar")
                            // binding.progressbar.visibility = View.GONE
                            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }

                        Player.STATE_IDLE -> {
                            Log.d("QWERTY", "onPlaybackStateChanged: STATE_IDLE")
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {}
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
                val album =
                    if (albumColumn != -1) it.getString(albumColumn) ?: "Unknown" else "Unknown"
                val duration = if (durationColumn != -1) it.getLong(durationColumn) else 0L

                val thumbnail = getVideoThumbnail(uri)

                if (thumbnail != null) {
                    recommendedVideos.add(VideoModel(uri, thumbnail, duration, album))
                }
            }
        }

        videoAdapter = RecommendedVideoAdapter()
        binding.recommendedList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recommendedList.adapter = videoAdapter
        videoAdapter?.setVideos(recommendedVideos)

        videoAdapter?.setOnRecommendedVideoClickListener(object :
            RecommendedVideoAdapter.OnRecommendedVideoClickListener {
            override fun onRecommendedVideoClick(position: Int) {
                Log.d("QWERTY", "onRecommendedVideoClick: position -> $position")
                val selectedVideo = recommendedVideos[position]
                Log.d("QWERTY", "onRecommendedVideoClick: selectedVideo -> $selectedVideo")
                playVideoAtUri(selectedVideo.uri)
                if (currentPlayerState == VideoPlayerState.SHRUNK)
                    toggleShrinkExpand()
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
        exoPlayer?.let {
            it.setMediaItem(MediaItem.fromUri(uri))
            it.prepare()
            it.play()
        } ?: run {
            // If player is null, initialize it first
            exoPlayer = ExoPlayer.Builder(this).build()
            binding.exoPlayer.player = exoPlayer
            exoPlayer?.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer?.prepare()
            exoPlayer?.play()
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
    }
}