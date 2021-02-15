package com.esafirm.imagepicker.features.camera

import android.content.Context
import android.content.Intent
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.model.Image

typealias OnImageReadyListener = (List<Image>?) -> Unit

interface CameraModule {
    fun getCameraIntent(context: Context, config: BaseConfig): Intent?
    fun getImage(context: Context, intent: Intent?, imageReadyListener: OnImageReadyListener)
    fun removeImage()
}