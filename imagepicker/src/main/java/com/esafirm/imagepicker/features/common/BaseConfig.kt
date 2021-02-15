package com.esafirm.imagepicker.features.common

import android.os.Parcelable
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.ReturnMode
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseConfig : Parcelable {

    var savePath: ImagePickerSavePath = ImagePickerSavePath.DEFAULT

    var returnMode: ReturnMode? = null

    var isSaveImage: Boolean = true

    fun setImageDirectory(dirName: String) {
        savePath = ImagePickerSavePath(dirName, false)
    }

    fun setImageFullDirectory(path: String) {
        savePath = ImagePickerSavePath(path, true)
    }
}