package com.esafirm.imagepicker.features

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.helper.LocaleManager
import com.esafirm.imagepicker.model.Image

/* --------------------------------------------------- */
/* > Ext */
/* --------------------------------------------------- */

typealias ImagePickerLauncher = ImagePickerConfig.() -> Unit
typealias ImagePickerCallback = (List<Image>) -> Unit

fun Fragment.registerImagePicker(
    callback: ImagePickerCallback
): ImagePickerLauncher {
    val fragment = this
    val launcher = createLauncher(callback)
    return { launcher.launch(createImagePickerIntent(fragment.requireContext(), this)) }
}

fun ComponentActivity.registerImagePicker(
    callback: ImagePickerCallback
): ImagePickerLauncher {
    val context = this
    val launcher = createLauncher(callback)
    return { launcher.launch(createImagePickerIntent(context, this)) }
}

fun createImagePickerIntent(context: Context, config: ImagePickerConfig): Intent {
    config.language?.run { LocaleManager.language = this }
    val intent = Intent(context, ImagePickerActivity::class.java)
    intent.putExtra(ImagePickerConfig::class.java.simpleName, config)
    return intent
}

/* --------------------------------------------------- */
/* > Launcher Creation */
/* --------------------------------------------------- */

private fun Fragment.createLauncher(callback: ImagePickerCallback) =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val images = ImagePicker.getImages(it.data) ?: emptyList()
        callback(images)
    }

private fun ComponentActivity.createLauncher(callback: ImagePickerCallback) =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val images = ImagePicker.getImages(it.data) ?: emptyList()
        callback(images)
    }
