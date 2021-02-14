package com.esafirm.imagepicker.features.common

import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image

interface ImageLoaderListener {
    fun onImageLoaded(images: List<Image>?, folders: List<Folder>?)
    fun onFailed(throwable: Throwable)
}