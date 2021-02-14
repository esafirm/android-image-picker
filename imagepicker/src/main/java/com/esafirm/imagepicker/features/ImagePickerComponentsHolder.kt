package com.esafirm.imagepicker.features

import android.content.Context
import com.esafirm.imagepicker.features.camera.CameraModule
import com.esafirm.imagepicker.features.camera.DefaultCameraModule
import com.esafirm.imagepicker.features.fileloader.DefaultImageFileLoader
import com.esafirm.imagepicker.features.fileloader.ImageFileLoader
import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader
import com.esafirm.imagepicker.features.imageloader.ImageLoader

interface ImagePickerComponents {
    val imageLoader: ImageLoader
    val imageFileLoader: ImageFileLoader
    val cameraModule: CameraModule
}

open class DefaultImagePickerComponents(context: Context) : ImagePickerComponents {
    override val imageLoader: ImageLoader by lazy { DefaultImageLoader() }
    override val imageFileLoader: ImageFileLoader by lazy { DefaultImageFileLoader(context) }
    override val cameraModule: CameraModule by lazy { DefaultCameraModule() }
}

object ImagePickerComponentsHolder : ImagePickerComponents {

    private lateinit var internalComponents: ImagePickerComponents

    override val imageLoader: ImageLoader
        get() = internalComponents.imageLoader

    override val imageFileLoader: ImageFileLoader
        get() = internalComponents.imageFileLoader

    override val cameraModule: CameraModule
        get() = internalComponents.cameraModule

    fun setInternalComponent(components: ImagePickerComponents) {
        internalComponents = components
    }
}