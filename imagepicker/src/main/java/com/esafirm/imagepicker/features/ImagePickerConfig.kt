package com.esafirm.imagepicker.features

import android.os.Parcelable
import androidx.annotation.StyleRes
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.helper.IpLogger
import com.esafirm.imagepicker.model.Image
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
class ImagePickerConfig(
    internal var mode: Int = 0,
    var folderTitle: String? = null,
    var imageTitle: String? = null,
    var doneButtonText: String? = null,
    var arrowColor: Int = NO_COLOR,
    var limit: Int = 0,
    @StyleRes var theme: Int = 0,
    var isFolderMode: Boolean = false,
    var isIncludeVideo: Boolean = false,
    var isOnlyVideo: Boolean = false,
    var isIncludeAnimation: Boolean = false,
    var isShowCamera: Boolean = false,
    var selectedImages: List<Image> = emptyList()
) : BaseConfig(), Parcelable {

    var excludedImages: List<File>? = null
        private set

    @Transient
    var language: String? = null

    fun setExcludedImages(excludedImages: List<Image>) {
        this.excludedImages = excludedImages.map { File(it.path) }
    }

    fun setExcludedImageFiles(excludedImages: List<File>?) {
        this.excludedImages = excludedImages
    }

    companion object {
        const val NO_COLOR = -1
    }
}

/* --------------------------------------------------- */
/* > Ext */
/* --------------------------------------------------- */

fun ImagePickerConfig.single() {
    mode = IpCons.MODE_SINGLE
}

fun ImagePickerConfig.multi() {
    mode = IpCons.MODE_MULTIPLE
}

fun ImagePickerConfig.enableLog(isEnable: Boolean) {
    IpLogger.setEnable(isEnable)
}