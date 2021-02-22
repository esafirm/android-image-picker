package com.esafirm.imagepicker.features

import android.app.Activity
import android.content.Intent
import com.esafirm.imagepicker.features.cameraonly.ImagePickerCameraOnly
import com.esafirm.imagepicker.model.Image

object ImagePicker {
    fun cameraOnly(): ImagePickerCameraOnly {
        return ImagePickerCameraOnly()
    }

    /* --------------------------------------------------- */
    /* > Helper */
    /* --------------------------------------------------- */

    fun shouldHandle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK && requestCode == IpCons.RC_IMAGE_PICKER && data != null
    }

    fun getImages(intent: Intent?): List<Image>? {
        return intent?.getParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES)
    }

    fun getFirstImageOrNull(intent: Intent?): Image? {
        val images = getImages(intent)
        return if (images == null || images.isEmpty()) {
            null
        } else images[0]
    }
}