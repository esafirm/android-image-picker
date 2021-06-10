package com.esafirm.imagepicker.features.fileloader

import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.common.ImageLoaderListener

interface ImageFileLoader {
    fun loadDeviceImages(config: ImagePickerConfig, listener: ImageLoaderListener)
    fun abortLoadImages()
}