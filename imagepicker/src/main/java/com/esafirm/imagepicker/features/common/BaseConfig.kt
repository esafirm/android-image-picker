package com.esafirm.imagepicker.features.common

import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.ReturnMode

abstract class BaseConfig {
    abstract var savePath: ImagePickerSavePath
    abstract var returnMode: ReturnMode
    abstract var isSaveImage: Boolean
    abstract var showDoneButtonAlways: Boolean
}