package com.esafirm.imagepicker.features.cameraonly

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.features.ImagePickerActivity
import com.esafirm.imagepicker.features.ImagePickerConfigFactory
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.IpCons

class ImagePickerCameraOnly {

    private val config = ImagePickerConfigFactory.createCameraDefault()

    fun imageDirectory(directory: String): ImagePickerCameraOnly {
        config.savePath = ImagePickerSavePath(directory)
        return this
    }

    fun imageFullDirectory(fullPath: String): ImagePickerCameraOnly {
        config.savePath = ImagePickerSavePath(fullPath, false)
        return this
    }

    @JvmOverloads
    fun start(activity: Activity, requestCode: Int = IpCons.RC_IMAGE_PICKER) {
        activity.startActivityForResult(getIntent(activity), requestCode)
    }

    @JvmOverloads
    fun start(fragment: Fragment, requestCode: Int = IpCons.RC_IMAGE_PICKER) {
        fragment.startActivityForResult(getIntent(fragment.activity), requestCode)
    }

    fun getIntent(context: Context?): Intent {
        val intent = Intent(context, ImagePickerActivity::class.java)
        intent.putExtra(CameraOnlyConfig::class.java.simpleName, config)
        return intent
    }
}