package com.esafirm.imagepicker.features.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.esafirm.imagepicker.features.ImagePickerConfigFactory
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.helper.IpLogger
import com.esafirm.imagepicker.helper.UriUtils
import com.esafirm.imagepicker.model.ImageFactory
import java.io.File
import java.io.Serializable

class DefaultCameraModule : CameraModule, Serializable {

    private var currentImagePath: String? = null

    /**
     * Helper function to get camera Intent without config
     */
    fun getCameraIntent(context: Context): Intent? {
        return getCameraIntent(context, ImagePickerConfigFactory.createDefault(context))
    }

    override fun getCameraIntent(context: Context, config: BaseConfig): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = ImagePickerUtils.createImageFile(config.imageDirectory, context)

        if (imageFile != null) {
            val appContext = context.applicationContext
            val uri = createCameraUri(appContext, imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            ImagePickerUtils.grantAppPermission(context, intent, uri)
            return intent
        }
        return null
    }

    private fun createCameraUri(appContext: Context, imageFile: File): Uri? {
        currentImagePath = "file:" + imageFile.absolutePath
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            return appContext.contentResolver.insert(collection, values)
        }
        return UriUtils.uriForFile(appContext, imageFile)
    }

    override fun getImage(context: Context, intent: Intent?, imageReadyListener: OnImageReadyListener?) {
        checkNotNull(imageReadyListener) { "OnImageReadyListener must not be null" }

        if (currentImagePath == null) {
            IpLogger.getInstance().w("currentImagePath null. " +
                "This happen if you haven't call #getCameraIntent() or the activity is being recreated")
            imageReadyListener.onImageReady(null)
            return
        }

        val imageUri = Uri.parse(currentImagePath)
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(imageUri.path), null) { path: String?, uri: Uri ->
                IpLogger.getInstance().d("File $path was scanned successfully: $uri")

                if (path == null) {
                    IpLogger.getInstance().d("This should not happen, go back to Immediate implementation")
                }
                val finalPath = path ?: currentImagePath!!
                imageReadyListener.onImageReady(ImageFactory.singleImage(uri, finalPath))
                ImagePickerUtils.revokeAppPermission(context, imageUri)
            }
        }
    }

    override fun removeImage() {
        val imagePath = currentImagePath ?: return
        val file = File(imagePath)
        if (file.exists()) {
            file.delete()
        }
    }
}