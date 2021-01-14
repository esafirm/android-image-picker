package com.esafirm.sample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.rickb.imagepicker.features.*
import com.rickb.imagepicker.features.imageloader.DefaultImageLoader
import com.rickb.imagepicker.model.Image
import com.rickb.rximagepicker.RxImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable

class MainActivity : AppCompatActivity() {

    private val images = arrayListOf<Image>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_pick_image.setOnClickListener { start() }
        button_pick_image_rx.setOnClickListener { imagePickerObservable.forEach(action) }
        button_intent.setOnClickListener { startWithIntent() }
        button_camera.setOnClickListener { captureImage() }
        button_launch_fragment.setOnClickListener {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MainFragment())
                    .commitAllowingStateLoss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RC_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun captureImage() {
        ImagePicker.cameraOnly().start(this)
    }

    private val action = { images: List<Image>? -> printImages(images) }
    private val imagePickerObservable: Observable<List<Image>>
        get() = RxImagePicker.instance
                .start(this, ImagePicker.create(this))// max images can be selected (99 by default)

    private val imagePicker: ImagePicker
        get() {
            val returnAfterCapture = ef_switch_return_after_capture.isChecked
            val isSingleMode = ef_switch_single.isChecked
            val useCustomImageLoader = ef_switch_imageloader.isChecked
            val folderMode = ef_switch_folder_mode.isChecked
            val includeVideo = ef_switch_include_video.isChecked
            val onlyVideo = ef_switch_only_video.isChecked
            val isExclude = ef_switch_include_exclude.isChecked

            val imagePicker = ImagePicker.create(this)
                    .language("en") // Set image picker language
                    .theme(R.style.ImagePickerTheme)
                    .showSelectionLimitBottomView(true)
                    .totalSizeLimit(3.0)
                    .amountOfMBsAlreadyInUse(1.0)
                    .returnMode(if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                    .folderMode(folderMode) // set folder mode (false by default)
                    .includeVideo(includeVideo) // include video (false by default)
                    .onlyVideo(onlyVideo) // include video (false by default)
                    .toolbarArrowColor(Color.RED) // set toolbar arrow up color
                    .toolbarFolderTitle("Folder") // folder selection title
                    .toolbarImageTitle("Tap to select") // image selection title
                    .toolbarDoneButtonText("DONE") // done button text

            ImagePickerComponentHolder.getInstance().imageLoader = DefaultImageLoader()

            if (isSingleMode) {
                imagePicker.single()
            } else {
                imagePicker.multi() // multi mode (default mode)
            }
            if (isExclude) {
                imagePicker.exclude(images) // don't show anything on this selected images
            } else {
                imagePicker.origin(images) // original selected images, used in multi mode
            }
            return imagePicker.limit(4) // max images can be selected (99 by default)
                    .showCamera(true) // show camera or not (true by default)
                    .imageDirectory("Camera") // captured image directory name ("Camera" folder by default)
                    .imageFullDirectory(Environment.getExternalStorageDirectory().path) // can be full path
        }

    private fun startWithIntent() {
        startActivityForResult(imagePicker.getIntent(this), IpCons.RC_IMAGE_PICKER)
    }

    private fun start() {
        imagePicker.start() // start image picker activity with request code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            images.clear()
            images.addAll(ImagePicker.getImages(data))
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

    companion object {
        private const val RC_CAMERA = 3000
    }
}