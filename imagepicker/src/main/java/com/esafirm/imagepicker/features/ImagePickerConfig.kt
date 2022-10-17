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
    var mode: ImagePickerMode = ImagePickerMode.MULTIPLE,
    var folderTitle: String? = null,
    var imageTitle: String? = null,
    var doneButtonText: String? = null,
    var arrowColor: Int = NO_COLOR,
    var limit: Int = IpCons.MAX_LIMIT,
    @StyleRes var theme: Int = 0,
    var isFolderMode: Boolean = false,
    var isIncludeVideo: Boolean = false,
    var isOnlyVideo: Boolean = false,
    var isIncludeAnimation: Boolean = false,
    var isShowCamera: Boolean = true,
    var selectedImages: List<Image> = emptyList(),
    var excludedImages: List<File> = emptyList(),
    override var savePath: ImagePickerSavePath = ImagePickerSavePath.DEFAULT,
    override var returnMode: ReturnMode = ReturnMode.NONE,
    override var isSaveImage: Boolean = true,
    override var isSaveVideo: Boolean = true,
    var showDoneButtonAlways: Boolean = false
) : BaseConfig(), Parcelable {

    @Transient
    var language: String? = null

    companion object {
        const val NO_COLOR = -1

        operator fun invoke(builder: ImagePickerConfig.() -> Unit): ImagePickerConfig {
            return ImagePickerConfig().apply(builder)
        }
    }
}

/* --------------------------------------------------- */
/* > Ext */
/* --------------------------------------------------- */

fun ImagePickerConfig.enableLog(isEnable: Boolean) {
    IpLogger.setEnable(isEnable)
}

fun List<Image>.toFiles() = this.map { File(it.path) }