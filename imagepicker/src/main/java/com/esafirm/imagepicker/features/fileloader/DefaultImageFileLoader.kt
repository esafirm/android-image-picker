package com.esafirm.imagepicker.features.fileloader

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.esafirm.imagepicker.features.common.ImageLoaderListener
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DefaultImageFileLoader(private val context: Context) : ImageFileLoader {

    private var executor: ExecutorService? = null

    override fun loadDeviceImages(
        isFolderMode: Boolean,
        onlyVideo: Boolean,
        includeVideo: Boolean,
        includeAnimation: Boolean,
        excludedImages: List<File>,
        listener: ImageLoaderListener
    ) {
        getExecutorService().execute(
            ImageLoadRunnable(
                context.applicationContext,
                isFolderMode,
                onlyVideo,
                includeVideo,
                includeAnimation,
                excludedImages,
                listener
            ))
    }

    override fun abortLoadImages() {
        executor?.shutdown()
        executor = null
    }

    private fun getExecutorService(): ExecutorService {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor()
        }
        return executor!!
    }

    private class ImageLoadRunnable(
        private val context: Context,
        private val isFolderMode: Boolean,
        private val onlyVideo: Boolean,
        private val includeVideo: Boolean,
        private val includeAnimation: Boolean,
        private val exlucedImages: List<File>?,
        private val listener: ImageLoaderListener
    ) : Runnable {

        companion object {
            private const val DEFAULT_FOLDER_NAME = "SDCARD"
        }

        private val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        fun getQuerySelection(): String? {
            if (onlyVideo) {
                return (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            }
            if (includeVideo) {
                return (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            }
            return null
        }

        private fun getSourceUri(): Uri {
            return if (onlyVideo || includeVideo) {
                MediaStore.Files.getContentUri("external")
            } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        override fun run() {
            val sourceUri = getSourceUri()
            val querySelection = getQuerySelection()

            val cursor = context.contentResolver.query(sourceUri, projection,
                querySelection, null, MediaStore.Images.Media.DATE_ADDED)
            if (cursor == null) {
                listener.onFailed(NullPointerException())
                return
            }
            val temp: MutableList<Image> = ArrayList()
            var folderMap: MutableMap<String, Folder> = mutableMapOf()
            if (isFolderMode) {
                folderMap = HashMap()
            }
            if (cursor.moveToLast()) {
                do {
                    val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                    val file = makeSafeFile(path) ?: continue
                    if (exlucedImages != null && exlucedImages.contains(file)) continue

                    // Exclude GIF when we don't want it
                    if (!includeAnimation) {
                        if (ImagePickerUtils.isGifFormat(path)) {
                            continue
                        }
                    }
                    val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                    val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                    var bucket = cursor.getString(cursor.getColumnIndex(projection[3]))

                    if (bucket == null) {
                        val parent = File(path).parentFile
                        bucket = if (parent != null) {
                            parent.name
                        } else {
                            DEFAULT_FOLDER_NAME
                        }
                    }

                    if (name != null) {
                        val image = Image(id, name, path)
                        temp.add(image)
                        if (bucket != null) {
                            var folder = folderMap[bucket]
                            if (folder == null) {
                                folder = Folder(bucket)
                                folderMap[bucket] = folder
                            }
                            folder.images.add(image)
                        }
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()

            val folders = folderMap.values.toList()
            listener.onImageLoaded(temp, folders)
        }
    }

    companion object {
        private fun makeSafeFile(path: String?): File? {
            return if (path == null || path.isEmpty()) {
                null
            } else try {
                File(path)
            } catch (ignored: Exception) {
                null
            }
        }
    }
}