package com.rickb.imagepicker.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.rickb.imagepicker.adapter.ImagePickerAdapter
import com.rickb.imagepicker.helper.ImagePickerUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class Image(
        val id: Long,
        val name: String,
        val path: String,
        val lastChangedTimestamp: Long
) : ImagePickerAdapter.Companion.PickerItem(lastChangedTimestamp)  {

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

    companion object {
        // Images which are edited less then MAX_DAYS_AGO_FOR_RECENT will appear under header 'Recent'.
        const val MAX_DAYS_AGO_FOR_RECENT = 2
    }
}