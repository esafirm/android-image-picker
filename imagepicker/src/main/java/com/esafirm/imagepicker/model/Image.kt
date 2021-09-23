package com.esafirm.imagepicker.model

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import com.esafirm.imagepicker.helper.ImagePickerUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
open class Image(
    val id: Long,
    val name: String,
    val path: String,
) : Parcelable {

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

    val file: File
        get() = File(path)

    fun asBitmap(context: Context): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

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
}