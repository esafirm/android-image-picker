package com.esafirm.imagepicker.features.fileloader

import com.esafirm.imagepicker.features.common.ImageLoaderListener
import java.io.File

interface ImageFileLoader {

    fun loadDeviceImages(
        isFolderMode: Boolean,
        onlyVideo: Boolean,
        includeVideo: Boolean,
        includeAnimation: Boolean,
        excludedImages: List<File>,
        listener: ImageLoaderListener
    )

    fun abortLoadImages()
}