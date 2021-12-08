package com.esafirm.imagepicker.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.esafirm.imagepicker.helper.ImagePickerUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Image(
    val id: Long,
    val name: String,
    val path: String,
    val type: String? = null,
    val dateModified: Date? = null,
    val size: Long? = null
) : Parcelable, BaseItem {

    @IgnoredOnParcel
    private var uriHolder: Uri? = null

    val uri: Uri
        get() {
            return uriHolder ?: let {
                val contentUri = if (ImagePickerUtils.isVideoFormat(this)) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                ContentUris.withAppendedId(contentUri, id).also {
                    uriHolder = it
                }
            }
        }

    override fun getItemName() = name

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null || javaClass != other.javaClass -> return false
            else -> {
                val image = other as Image
                image.path.equals(path, ignoreCase = true)
            }
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + (uriHolder?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (dateModified?.hashCode() ?: 0)
        result = 31 * result + (size?.hashCode() ?: 0)
        return result
    }
}