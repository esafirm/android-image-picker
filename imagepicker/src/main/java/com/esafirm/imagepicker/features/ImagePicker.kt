package com.esafirm.imagepicker.features

import android.content.Intent
import com.esafirm.imagepicker.features.cameraonly.ImagePickerCameraOnly
import com.esafirm.imagepicker.model.Document
import com.esafirm.imagepicker.model.Image

object ImagePicker {
    fun cameraOnly(): ImagePickerCameraOnly {
        return ImagePickerCameraOnly()
    }

    /* --------------------------------------------------- */
    /* > Helper */
    /* --------------------------------------------------- */

    @Deprecated("This method will marked internal soon. Please use the new API")
    fun getImages(intent: Intent?): List<Image>? {
        return intent?.getParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES)
    }

    fun getDocuments(intent: Intent?): List<Document>? {
        return intent?.getParcelableArrayListExtra(IpCons.EXTRA_SELECTED_DOCUMENTS)
    }
}
