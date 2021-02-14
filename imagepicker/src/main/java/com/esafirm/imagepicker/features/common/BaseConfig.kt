package com.esafirm.imagepicker.features.common

import android.os.Parcelable
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.ReturnMode
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseConfig : Parcelable {

    var imageDirectory: ImagePickerSavePath? = null
        private set

    var returnMode: ReturnMode? = null

    var saveImage: Boolean = true

    fun setSavePath(savePath: ImagePickerSavePath?) {
        imageDirectory = savePath
    }

    fun setImageDirectory(dirName: String?) {
        imageDirectory = ImagePickerSavePath(dirName!!, false)
    }

    fun setImageFullDirectory(path: String?) {
        imageDirectory = ImagePickerSavePath(path!!, true)
    }
}