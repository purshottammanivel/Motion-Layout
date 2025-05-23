package com.miko.motionlayoutpoc

import android.graphics.Bitmap
import android.net.Uri

data class VideoModel(
    val uri: Uri,
    val thumbnail: Bitmap,
    val duration: Long,
    val album: String
)