package com.esafirm.imagepicker.features

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ImagePickerSavePath(
    val path: String,
    val isRelative: Boolean = true
) : Parcelable {
    companion object {
        val DEFAULT = ImagePickerSavePath("Camera", false)
    }
}