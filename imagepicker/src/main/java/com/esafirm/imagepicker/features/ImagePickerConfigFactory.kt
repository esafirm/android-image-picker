package com.esafirm.imagepicker.features

import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig

object ImagePickerConfigFactory {
    fun createCameraDefault(): CameraOnlyConfig {
        val config = CameraOnlyConfig()
        config.returnMode = ReturnMode.ALL
        return config
    }

    @JvmStatic
    fun createDefault(): ImagePickerConfig {
        val config = ImagePickerConfig(
            mode = IpCons.MODE_MULTIPLE,
            limit = IpCons.MAX_LIMIT,
            isShowCamera = true,
            isFolderMode = false,
        )
        config.returnMode = ReturnMode.NONE
        return config
    }
}