package com.miko.motionlayoutpoc

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun ImageView.loadFromString(imagePath: String?) {
    if (!imagePath.isNullOrEmpty())
        loadWithGlide(imagePath)
}

fun ImageView.loadWithGlide(source: Any) {
    Glide.with(context)
        .asDrawable()
        .load(source)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // Do nothing: keep current background until new one is ready
            }
        })
}