package com.esafirm.imagepicker.features.imageloader

import android.widget.ImageView
import com.esafirm.imagepicker.model.Image

interface ImageLoader {
    fun loadImage(image: Image, imageView: ImageView, imageType: ImageType)
}