package com.esafirm.imagepicker.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerConfigFactory.createDefault
import com.esafirm.imagepicker.features.cameraonly.ImagePickerCameraOnly
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.helper.IpLogger.setEnable
import com.esafirm.imagepicker.helper.LocaleManager
import com.esafirm.imagepicker.model.Image
import java.io.File
import java.util.*

abstract class ImagePicker {

    private val config: ImagePickerConfig = createDefault()

    abstract fun start()
    abstract fun start(requestCode: Int)

    /* --------------------------------------------------- */
    /* > Builder */
    /* --------------------------------------------------- */

    fun single(): ImagePicker {
        config.mode = IpCons.MODE_SINGLE
        return this
    }

    fun multi(): ImagePicker {
        config.mode = IpCons.MODE_MULTIPLE
        return this
    }

    fun returnMode(returnMode: ReturnMode): ImagePicker {
        config.returnMode = returnMode
        return this
    }

    fun saveImage(saveImage: Boolean): ImagePicker {
        config.isSaveImage = saveImage
        return this
    }

    fun limit(count: Int): ImagePicker {
        config.limit = count
        return this
    }

    fun showCamera(show: Boolean): ImagePicker {
        config.isShowCamera = show
        return this
    }

    fun toolbarArrowColor(@ColorInt color: Int): ImagePicker {
        config.arrowColor = color
        return this
    }

    fun toolbarFolderTitle(title: String?): ImagePicker {
        config.folderTitle = title
        return this
    }

    fun toolbarImageTitle(title: String?): ImagePicker {
        config.imageTitle = title
        return this
    }

    fun toolbarDoneButtonText(text: String?): ImagePicker {
        config.doneButtonText = text
        return this
    }

    fun origin(images: ArrayList<Image>): ImagePicker {
        config.selectedImages = images
        return this
    }

    fun exclude(images: List<Image>): ImagePicker {
        config.setExcludedImages(images)
        return this
    }

    fun excludeFiles(files: List<File>): ImagePicker {
        config.setExcludedImageFiles(files)
        return this
    }

    fun folderMode(folderMode: Boolean): ImagePicker {
        config.isFolderMode = folderMode
        return this
    }

    fun includeVideo(includeVideo: Boolean): ImagePicker {
        config.isIncludeVideo = includeVideo
        return this
    }

    fun onlyVideo(onlyVideo: Boolean): ImagePicker {
        config.isOnlyVideo = onlyVideo
        return this
    }

    fun includeAnimation(includeAnimation: Boolean): ImagePicker {
        config.isIncludeAnimation = includeAnimation
        return this
    }

    fun imageDirectory(directory: String): ImagePicker {
        config.setImageDirectory(directory)
        return this
    }

    fun imageFullDirectory(fullPath: String): ImagePicker {
        config.setImageFullDirectory(fullPath)
        return this
    }

    fun theme(@StyleRes theme: Int): ImagePicker {
        config.theme = theme
        return this
    }

    fun enableLog(isEnable: Boolean): ImagePicker {
        setEnable(isEnable)
        return this
    }

    fun language(language: String): ImagePicker {
        config.language = language
        return this
    }

    fun getConfig(): ImagePickerConfig {
        val language = config.language
        if (language != null) {
            LocaleManager.language = language
        }
        return ConfigUtils.checkConfig(config)
    }

    fun getIntent(context: Context?): Intent {
        val config = getConfig()
        val intent = Intent(context, ImagePickerActivity::class.java)
        intent.putExtra(ImagePickerConfig::class.java.simpleName, config)
        return intent
    }

    companion object {
        fun create(activity: Activity): ImagePickerWithActivity {
            return ImagePickerWithActivity(activity)
        }

        fun create(fragment: Fragment): ImagePickerWithFragment {
            return ImagePickerWithFragment(fragment)
        }

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
}

class ImagePickerWithActivity(private val activity: Activity) : ImagePicker() {
    override fun start(requestCode: Int) {
        activity.startActivityForResult(getIntent(activity), requestCode)
    }

    override fun start() {
        activity.startActivityForResult(getIntent(activity), IpCons.RC_IMAGE_PICKER)
    }
}

class ImagePickerWithFragment(private val fragment: Fragment) : ImagePicker() {
    override fun start(requestCode: Int) {
        fragment.startActivityForResult(getIntent(fragment.activity), requestCode)
    }

    override fun start() {
        fragment.startActivityForResult(getIntent(fragment.activity), IpCons.RC_IMAGE_PICKER)
    }
}