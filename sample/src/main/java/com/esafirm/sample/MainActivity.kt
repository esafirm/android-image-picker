package com.esafirm.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.esafirm.imagepicker.features.*
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader
import com.esafirm.imagepicker.features.imageloader.ImageLoader
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val images = arrayListOf<Image>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_pick_image.setOnClickListener { start() }
        button_intent.setOnClickListener { startWithIntent() }
        button_camera.setOnClickListener { captureImage() }
        button_custom_ui.setOnClickListener { startCustomUI() }
        button_launch_fragment.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun captureImage() {
        imagePickerLauncher.launch(CameraOnlyConfig())
    }

    private fun createConfig(): ImagePickerConfig {
        val returnAfterCapture = ef_switch_return_after_capture.isChecked
        val isSingleMode = ef_switch_single.isChecked
        val useCustomImageLoader = ef_switch_imageloader.isChecked
        val folderMode = ef_switch_folder_mode.isChecked
        val includeVideo = ef_switch_include_video.isChecked
        val onlyVideo = ef_switch_only_video.isChecked
        val isExclude = ef_switch_include_exclude.isChecked

        ImagePickerComponentsHolder.setInternalComponent(object : DefaultImagePickerComponents(this) {
            override val imageLoader: ImageLoader
                get() = if (useCustomImageLoader) {
                    GrayscaleImageLoader()
                } else {
                    DefaultImageLoader()
                }
        })

        return ImagePickerConfig {

            mode = if (isSingleMode) {
                ImagePickerMode.SINGLE
            } else {
                ImagePickerMode.MULTIPLE // multi mode (default mode)
            }

            language = "in" // Set image picker language
            theme = R.style.ImagePickerTheme

            // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
            returnMode = if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE

            isFolderMode = folderMode // set folder mode (false by default)
            isIncludeVideo = includeVideo // include video (false by default)
            isOnlyVideo = onlyVideo // include video (false by default)
            arrowColor = Color.RED // set toolbar arrow up color
            folderTitle = "Folder" // folder selection title
            imageTitle = "Tap to select" // image selection title
            doneButtonText = "DONE" // done button text
            limit = 10 // max images can be selected (99 by default)
            isShowCamera = true // show camera or not (true by default)
            savePath = ImagePickerSavePath("Camera") // captured image directory name ("Camera" folder by default)
            savePath = ImagePickerSavePath(Environment.getExternalStorageDirectory().path, isRelative = false) // can be a full path

            if (isExclude) {
                excludedImages = images.toFiles() // don't show anything on this selected images
            } else {
                selectedImages = images  // original selected images, used in multi mode
            }
        }
    }

    private fun startWithIntent() {
        val intent = createImagePickerIntent(this, createConfig())
        startActivityForResult(intent, IpCons.RC_IMAGE_PICKER)
    }

    private val imagePickerLauncher = registerImagePicker {
        images.clear()
        images.addAll(it)
        printImages(images)
    }

    private fun start() {
        imagePickerLauncher.launch(createConfig())
    }

    private fun startCustomUI() {
        val intent = Intent(this, CustomUIActivity::class.java)
        intent.putExtra(ImagePickerConfig::class.java.simpleName, createConfig())
        startActivityForResult(intent, IpCons.RC_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IpCons.RC_IMAGE_PICKER && data != null) {
            images.clear()
            images.addAll(ImagePicker.getImages(data) ?: emptyList())
            printImages(images)
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun printImages(images: List<Image>?) {
        if (images == null) return
        text_view.text = images.joinToString("\n")
        text_view.setOnClickListener {
            ImageViewerActivity.start(this@MainActivity, images)
        }
    }
}