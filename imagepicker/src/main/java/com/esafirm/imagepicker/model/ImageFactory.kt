package com.esafirm.imagepicker.model

import com.esafirm.imagepicker.helper.ImagePickerUtils

object ImageFactory {
    @JvmStatic
    fun singleListFromPath(path: String): List<Image> {
        return listOf(Image(0, ImagePickerUtils.getNameFromFilePath(path), path))
    }
}