package com.miko.motionlayoutpoc

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.miko.motionlayoutpoc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    private var videoAdapter: VideoAdapter? = null

    private val videoUris = mutableListOf<Uri>()
    private val videoAlbums = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),
                    101
                )
            } else {
                loadVideos()
            }
        }
    }

    /*override fun onResume() {
        super.onResume()
        if (videoAdapter == null) {
            Log.d("QWERTY", "onResume: videoAdapter is null")
        }
        videoAdapter?.setOnVideoClickListener(object: VideoAdapter.OnVideoClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onVideoClick(position: Int) {
                val selectedVideoUri = videoUris[position]
                Log.d("QWERTY", "onVideoClick: selectedVideoUri -> $selectedVideoUri")
                playVideoAtPosition(position)
                videoAdapter?.notifyDataSetChanged()
            }
        })
    }*/

    private fun loadVideos() {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.ALBUM // May not always be available; consider using BUCKET_DISPLAY_NAME instead
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(collection, projection, null, null, sortOrder)

        videoUris.clear()
        videoAlbums.clear()

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val albumColumn = it.getColumnIndex(MediaStore.Video.Media.ALBUM)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                videoUris.add(uri)

                val album = albumColumn.let { col ->
                    it.getString(col) ?: "Unknown Album"
                } ?: "N/A"
                videoAlbums.add(album)
            }
        }

        videoAdapter = VideoAdapter(this)
        binding.rvVideos.layoutManager = LinearLayoutManager(this)
        binding.rvVideos.adapter = videoAdapter
        videoAdapter?.setAdapter(videoUris, videoAlbums)

        videoAdapter?.setOnVideoClickListener(object: VideoAdapter.OnVideoClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onVideoClick(position: Int) {
                val selectedVideoUri = videoUris[position]
                Log.d("QWERTY", "onVideoClick: selectedVideoUri -> $selectedVideoUri")
                playVideoAtPosition(position)
                videoAdapter?.notifyDataSetChanged()
            }
        })
    }

    private fun playVideoAtPosition(position: Int) {
        val selectedVideoUri = videoUris[position]
        val intent = Intent(this, VideoActivity::class.java).apply {
            putExtra("video_uri", selectedVideoUri.toString())
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadVideos()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}