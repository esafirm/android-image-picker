package com.esafirm.imagepicker.features.camera

import com.esafirm.imagepicker.model.Image

interface OnImageReadyListener {
    fun onImageReady(images: List<Image>?)
}