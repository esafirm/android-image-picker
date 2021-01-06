package com.rickb.imagepicker.model

import android.content.ContentUris
import android.net.Uri
import com.rickb.imagepicker.features.fileloader.DefaultImageFileLoader
import com.rickb.imagepicker.helper.ImagePickerUtils

object ImageFactory {
    @JvmStatic
    fun singleImage(uri: Uri, path: String): List<Image> {
        val lastModified = DefaultImageFileLoader.makeSafeFile(path)?.lastModified() ?: 0

        return listOf(Image(
                id = ContentUris.parseId(uri),
                name = ImagePickerUtils.getNameFromFilePath(path),
                path = path,
                lastChangedTimestamp = lastModified
        ))
    }
}