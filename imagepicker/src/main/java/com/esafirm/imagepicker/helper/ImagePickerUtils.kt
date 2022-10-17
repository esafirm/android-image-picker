package com.esafirm.imagepicker.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.IpCons
import com.esafirm.imagepicker.helper.IpLogger.d
import com.esafirm.imagepicker.model.Image
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

object ImagePickerUtils {

    private const val DEFAULT_DURATION_LABEL = "00:00"

    private fun createFileInDirectory(savePath: ImagePickerSavePath, context: Context): File? {
        // External sdcard location
        val path = savePath.path
        val mediaStorageDir: File = if (savePath.isRelative) {
            val parent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            }
            File(parent, path)
        } else {
            File(path)
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                d("Oops! Failed create $path")
                return null
            }
        }
        return mediaStorageDir
    }

    private fun createFileInDirectoryVideo(savePath: ImagePickerSavePath, context: Context): File? {
        val path = savePath.path
        val mediaStorageDir: File = if (savePath.isRelative) {
            val parent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            }
            File(parent, path)
        } else {
            File(path)
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                d("Oops! Failed create $path")
                return null
            }
        }
        return mediaStorageDir
    }

    fun createImageFile(savePath: ImagePickerSavePath, context: Context): File? {
        val mediaStorageDir = createFileInDirectory(savePath, context) ?: return null

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        var result = File(mediaStorageDir, "IMG_$timeStamp.jpg")
        var counter = 0
        while (result.exists()) {
            counter++
            result = File(mediaStorageDir, "IMG_$timeStamp($counter).jpg")
        }
        return result
    }

    fun createVideoFile(savePath: ImagePickerSavePath, context: Context): File? {
        val mediaStorageDir = createFileInDirectoryVideo(savePath, context) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        var result = File(mediaStorageDir, "VID_$timeStamp.mp4")
        var counter = 0
        while (result.exists()) {
            counter++
            result = File(mediaStorageDir, "VID_$timeStamp($counter).mp4")
        }
        return result
    }

    fun getNameFromFilePath(path: String): String {
        return if (path.contains(File.separator)) {
            path.substring(path.lastIndexOf(File.separator) + 1)
        } else path
    }

    fun grantAppPermission(context: Context, intent: Intent, fileUri: Uri?) {
        val resolvedIntentActivities = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName, fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    fun revokeAppPermission(context: Context, fileUri: Uri?) {
        context.revokeUriPermission(
            fileUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    fun isGifFormat(image: Image): Boolean {
        return isGifFormat(image.path)
    }

    fun isGifFormat(path: String): Boolean {
        val extension = getExtension(path)
        return extension.equals("gif", ignoreCase = true)
    }

    fun isVideoFormat(image: Image): Boolean {
        val extension = getExtension(image.path)
        val mimeType =
            if (TextUtils.isEmpty(extension)) URLConnection.guessContentTypeFromName(image.path) else MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)
        return mimeType != null && mimeType.startsWith("video")
    }

    fun getVideoDurationLabel(context: Context?, uri: Uri): String {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationData =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()

            // Return default duration label if null
            val duration = durationData?.toLongOrNull() ?: return DEFAULT_DURATION_LABEL
            val second = duration / 1000 % 60
            val minute = duration / (1000 * 60) % 60
            val hour = duration / (1000 * 60 * 60) % 24
            return if (hour > 0) {
                String.format("%02d:%02d:%02d", hour, minute, second)
            } else {
                String.format("%02d:%02d", minute, second)
            }
        } catch (e: Exception) {
            return DEFAULT_DURATION_LABEL
        }
    }

    private fun getExtension(path: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        if (!TextUtils.isEmpty(extension)) {
            return extension
        }
        return if (path.contains(".")) {
            path.substring(path.lastIndexOf(".") + 1, path.length)
        } else {
            ""
        }
    }

    fun createResultIntent(images: List<Image>?): Intent {
        val data = Intent()
        val imageArrayList = ArrayList(images ?: emptyList())
        data.putParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES, imageArrayList)
        return data
    }
}