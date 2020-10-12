package com.esafirm.imagepicker.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.provider.MediaStore
import com.esafirm.imagepicker.helper.ImagePickerUtils

open class Image : Parcelable {
    val id: Long
    val name: String?
    val path: String?

    private var uriHolder: Uri? = null

    constructor(id: Long, name: String?, path: String?) {
        this.id = id
        this.name = name
        this.path = path
    }

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

    override fun equals(o: Any?): Boolean {
        return when {
            this === o -> true
            o == null || javaClass != o.javaClass -> return false
            else -> {
                val image = o as Image
                image.path.equals(path, ignoreCase = true)
            }
        }
    }

    /* --------------------------------------------------- */
    /* > Parcelable */
    /* --------------------------------------------------- */
    protected constructor(`in`: Parcel) {
        id = `in`.readLong()
        name = `in`.readString()
        path = `in`.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeLong(id)
            writeString(name)
            writeString(path)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Image?> = object : Creator<Image?> {
            override fun createFromParcel(source: Parcel): Image? {
                return Image(source)
            }

            override fun newArray(size: Int): Array<Image?> {
                return arrayOfNulls(size)
            }
        }
    }
}