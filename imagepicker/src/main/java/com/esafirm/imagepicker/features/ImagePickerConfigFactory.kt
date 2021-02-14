package com.esafirm.imagepicker.features

import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import java.util.*

object ImagePickerConfigFactory {
    fun createCameraDefault(): CameraOnlyConfig {
        val config = CameraOnlyConfig()
        config.setSavePath(ImagePickerSavePath.DEFAULT)
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
        config.selectedImages = ArrayList()
        config.setSavePath(ImagePickerSavePath.DEFAULT)
        config.returnMode = ReturnMode.NONE
        return config
    }
}