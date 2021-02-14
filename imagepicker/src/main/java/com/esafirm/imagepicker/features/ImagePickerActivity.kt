package com.esafirm.imagepicker.features

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.helper.IpLogger.e
import com.esafirm.imagepicker.helper.LocaleManager
import com.esafirm.imagepicker.helper.ViewUtils
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image

class ImagePickerActivity : AppCompatActivity(), ImagePickerInteractionListener, ImagePickerView {

    private var actionBar: ActionBar? = null
    private lateinit var imagePickerFragment: ImagePickerFragment
    private lateinit var config: ImagePickerConfig

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        /* This should not happen */
        val intent = intent
        if (intent == null || intent.extras == null) {
            e("This should not happen. Please open an issue!")
            finish()
            return
        }
        config = intent.extras?.getParcelable(ImagePickerConfig::class.java.simpleName)!!
        val cameraOnlyConfig: CameraOnlyConfig? = intent.extras?.getParcelable(CameraOnlyConfig::class.java.simpleName)
        val isCameraOnly = cameraOnlyConfig != null

        // TODO extract camera only function so we don't have to rely to Fragment
        if (!isCameraOnly) {
            setTheme(config.theme)
            setContentView(R.layout.ef_activity_image_picker)
            setupView()
        } else {
            setContentView(createCameraLayout())
        }
        if (savedInstanceState != null) {
            // The fragment has been restored.
            imagePickerFragment = supportFragmentManager.findFragmentById(R.id.ef_imagepicker_fragment_placeholder) as ImagePickerFragment
        } else {
            imagePickerFragment = ImagePickerFragment.newInstance(config, cameraOnlyConfig)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.ef_imagepicker_fragment_placeholder, imagePickerFragment)
            ft.commit()
        }
    }

    private fun createCameraLayout(): FrameLayout {
        val frameLayout = FrameLayout(this)
        frameLayout.id = R.id.ef_imagepicker_fragment_placeholder
        return frameLayout
    }

    /**
     * Create option menus.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ef_image_picker_menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_camera).isVisible = config.isShowCamera
        menu.findItem(R.id.menu_done).apply {
            title = ConfigUtils.getDoneButtonText(this@ImagePickerActivity, config)
            isVisible = imagePickerFragment.isShowDoneButton
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handle option menu's click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (id == R.id.menu_done) {
            imagePickerFragment.onDone()
            return true
        }
        if (id == R.id.menu_camera) {
            imagePickerFragment.captureImageWithPermission()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!imagePickerFragment.handleBack()) {
            super.onBackPressed()
        }
    }

    private fun setupView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.run {
            val arrowDrawable = ViewUtils.getArrowIcon(this@ImagePickerActivity)
            val arrowColor = config.arrowColor
            if (arrowColor != ImagePickerConfig.NO_COLOR && arrowDrawable != null) {
                arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP)
            }
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(arrowDrawable)
            setDisplayShowTitleEnabled(true)
        }

    }

    /* --------------------------------------------------- */
    /* > ImagePickerInteractionListener Methods  */
    /* --------------------------------------------------- */

    override fun setTitle(title: String?) {
        actionBar?.title = title
        invalidateOptionsMenu()
    }

    override fun cancel() {
        finish()
    }

    override fun selectionChanged(imageList: List<Image>?) {
        // Do nothing when the selection changes.
    }

    override fun finishPickImages(result: Intent?) {
        setResult(RESULT_OK, result)
        finish()
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    override fun showLoading(isLoading: Boolean) {
        imagePickerFragment.showLoading(isLoading)
    }

    override fun showFetchCompleted(images: List<Image>?, folders: List<Folder?>?) {
        imagePickerFragment.showFetchCompleted(images, folders)
    }

    override fun showError(throwable: Throwable?) {
        imagePickerFragment.showError(throwable)
    }

    override fun showEmpty() {
        imagePickerFragment.showEmpty()
    }

    override fun showCapturedImage() {
        imagePickerFragment.showCapturedImage()
    }

    override fun finishPickImages(images: List<Image>?) {
        imagePickerFragment.finishPickImages(images)
    }
}