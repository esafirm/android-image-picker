package com.esafirm.imagepicker.features

import android.os.Parcelable
import androidx.annotation.StyleRes
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.model.Image
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
class ImagePickerConfig(
    var folderTitle: String? = null,
    var imageTitle: String? = null,
    var doneButtonText: String? = null,
    var arrowColor: Int = NO_COLOR,
    var mode: Int = 0,
    var limit: Int = 0,
    @StyleRes var theme: Int = 0,
    var isFolderMode: Boolean = false,
    var isIncludeVideo: Boolean = false,
    var isOnlyVideo: Boolean = false,
    var isIncludeAnimation: Boolean = false,
    var isShowCamera: Boolean = false,
) : BaseConfig(), Parcelable {

    var selectedImages: ArrayList<Image>? = null
    var excludedImages: ArrayList<File?>? = null
        private set

    @Transient
    var language: String? = null

    fun setExcludedImages(excludedImages: ArrayList<Image>?) {
        if (excludedImages != null && excludedImages.isNotEmpty()) {
            this.excludedImages = ArrayList()
            for (image in excludedImages) {
                this.excludedImages!!.add(File(image.path))
            }
        } else {
            this.excludedImages = null
        }
    }

    fun setExcludedImageFiles(excludedImages: ArrayList<File?>?) {
        this.excludedImages = excludedImages
    }

    companion object {
        const val NO_COLOR = -1
    }
}