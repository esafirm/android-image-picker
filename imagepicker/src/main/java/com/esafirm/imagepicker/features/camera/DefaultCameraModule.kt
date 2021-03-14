package com.esafirm.imagepicker.features.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.helper.IpLogger
import com.esafirm.imagepicker.helper.UriUtils
import com.esafirm.imagepicker.model.ImageFactory
import java.io.File

class DefaultCameraModule : CameraModule {

    private var currentImagePath: String? = null
    private var currentUri: String? = null

    override fun getCameraIntent(context: Context, config: BaseConfig): Intent? {
        prepareForNewIntent()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = ImagePickerUtils.createImageFile(config.savePath, context)

        if (config.isSaveImage && imageFile != null) {
            val appContext = context.applicationContext
            val uri = createCameraUri(appContext, imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            ImagePickerUtils.grantAppPermission(context, intent, uri)
            currentUri = uri.toString()
            return intent
        }
        return null
    }

    private fun prepareForNewIntent() {
        currentImagePath = null
        currentUri = null
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

    override fun getImage(context: Context, intent: Intent?, imageReadyListener: OnImageReadyListener) {
        if (currentImagePath == null) {
            IpLogger.w("currentImagePath null. " +
                "This happen if you haven't call #getCameraIntent() or the activity is being recreated")
            imageReadyListener.invoke(null)
            return
        }

        val imageUri = Uri.parse(currentImagePath)
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(imageUri.path), null) { path: String?, uri: Uri? ->
                IpLogger.d("File $path was scanned successfully: $uri")

                if (path == null) {
                    IpLogger.d("This should not happen, go back to Immediate implementation")
                }
                if (uri == null) {
                    IpLogger.d("scanFile is failed. Uri is null")
                }

                val finalPath = path ?: currentImagePath!!
                val finalUri = uri ?: Uri.parse(currentUri)
                imageReadyListener.invoke(ImageFactory.singleImage(finalUri, finalPath))
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