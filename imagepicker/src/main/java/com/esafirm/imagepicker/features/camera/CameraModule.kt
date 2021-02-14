package com.esafirm.imagepicker.features.camera

import android.content.Context
import android.content.Intent
import com.esafirm.imagepicker.features.common.BaseConfig
import java.io.Serializable

interface CameraModule : Serializable {
    fun getCameraIntent(context: Context, config: BaseConfig): Intent?
    fun getImage(context: Context, intent: Intent?, imageReadyListener: OnImageReadyListener?)
    fun removeImage()
}