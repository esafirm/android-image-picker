package com.esafirm.imagepicker.features

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.features.camera.CameraModule
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.features.common.BasePresenter
import com.esafirm.imagepicker.features.common.ImageLoaderListener
import com.esafirm.imagepicker.features.fileloader.DefaultImageFileLoader
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image
import java.io.File

internal class ImagePickerPresenter(
    private val imageLoader: DefaultImageFileLoader
) : BasePresenter<ImagePickerView>() {

    private val cameraModule: CameraModule = ImagePickerComponentsHolder.cameraModule

    private val main = Handler(Looper.getMainLooper())
    fun abortLoad() {
        imageLoader.abortLoadImages()
    }

    fun loadImages(config: ImagePickerConfig) {
        if (!isViewAttached) return

        val isFolder = config.isFolderMode
        val includeVideo = config.isIncludeVideo
        val onlyVideo = config.isOnlyVideo
        val includeAnimation = config.isIncludeAnimation
        val excludedImages = config.excludedImages ?: emptyList()

        runOnUi { showLoading(true) }

        imageLoader.loadDeviceImages(isFolder, onlyVideo, includeVideo, includeAnimation, excludedImages, object : ImageLoaderListener {
            override fun onImageLoaded(images: List<Image>, folders: List<Folder>) {
                runOnUi {
                    showFetchCompleted(images, folders)
                    val isEmpty = folders.isEmpty()
                    if (isEmpty) {
                        showEmpty()
                    } else {
                        showLoading(false)
                    }
                }
            }

            override fun onFailed(throwable: Throwable) {
                runOnUi { showError(throwable) }
            }
        })
    }

    fun onDoneSelectImages(selectedImages: List<Image>?) {
        if (selectedImages == null || selectedImages.isEmpty()) return

        runOnUi {
            finishPickImages(selectedImages.filter {
                val file = File(it.path)
                file.exists()
            })
        }
    }

    fun captureImage(fragment: Fragment, config: BaseConfig, requestCode: Int) {
        val context = fragment.requireContext().applicationContext
        val intent = cameraModule.getCameraIntent(fragment.requireContext(), config)
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show()
            return
        }
        fragment.startActivityForResult(intent, requestCode)
    }

    fun finishCaptureImage(context: Context, data: Intent?, config: BaseConfig?) {
        cameraModule.getImage(context, data) { images ->
            runOnUi {
                if (ConfigUtils.shouldReturn(config!!, true)) {
                    finishPickImages(images)
                } else {
                    showCapturedImage()
                }
            }
        }
    }

    fun abortCaptureImage() {
        cameraModule.removeImage()
    }

    private fun runOnUi(block: ImagePickerView.() -> Unit) {
        main.post {
            if (isViewAttached) {
                block(view!!)
            }
        }
    }
}