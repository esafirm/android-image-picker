package com.esafirm.imagepicker.features

import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig

object ImagePickerConfigFactory {
    fun createCameraDefault(): CameraOnlyConfig {
        val config = CameraOnlyConfig()
        config.returnMode = ReturnMode.ALL
        return config
    }

    @JvmStatic
    inline fun create(builder: ImagePickerConfig.() -> Unit = {}): ImagePickerConfig {
        val config = ImagePickerConfig()
        return config.apply(builder)
    }
}