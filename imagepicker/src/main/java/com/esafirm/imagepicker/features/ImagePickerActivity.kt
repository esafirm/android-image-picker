package com.esafirm.imagepicker.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.helper.IpCrasher
import com.esafirm.imagepicker.helper.LocaleManager
import com.esafirm.imagepicker.helper.ViewUtils
import com.esafirm.imagepicker.model.Image

class ImagePickerActivity : AppCompatActivity(), ImagePickerInteractionListener {

    private val cameraModule = ImagePickerComponentsHolder.cameraModule

    private var actionBar: ActionBar? = null
    private lateinit var imagePickerFragment: ImagePickerFragment

    private val config: ImagePickerConfig? by lazy {
        intent.extras!!.getParcelable(ImagePickerConfig::class.java.simpleName)
    }

    private val cameraOnlyConfig: CameraOnlyConfig? by lazy {
        intent.extras?.getParcelable(CameraOnlyConfig::class.java.simpleName)
    }

    private val isCameraOnly by lazy { cameraOnlyConfig != null }

    private val startForCameraResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // Handle the Intent
            if (intent?.extras?.isEmpty == true) {
                cameraModule.removeImage(this)
                setResult(RESULT_CANCELED)
                finish()
            } else {
                cameraModule.getImage(this, intent) { images ->
                    finishPickImages(ImagePickerUtils.createResultIntent(images))
                }
            }
        } else {
            cameraModule.removeImage(this)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        /* This should not happen */
        val intent = intent
        if (intent == null || intent.extras == null) {
            IpCrasher.openIssue()
        }

        if (isCameraOnly) {
            val cameraIntent = cameraModule.getCameraIntent(this, cameraOnlyConfig!!)
            startForCameraResult.launch(cameraIntent)
            return
        }

        val currentConfig = config!!
        setTheme(currentConfig.theme)
        setContentView(R.layout.ef_activity_image_picker)
        setupView(currentConfig)

        if (savedInstanceState != null) {
            // The fragment has been restored.
            imagePickerFragment =
                supportFragmentManager.findFragmentById(R.id.ef_imagepicker_fragment_placeholder) as ImagePickerFragment
        } else {
            imagePickerFragment = ImagePickerFragment.newInstance(currentConfig)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.ef_imagepicker_fragment_placeholder, imagePickerFragment)
            ft.commit()
        }
    }

    /**
     * Create option menus.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ef_image_picker_menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (!isCameraOnly) {
            menu.findItem(R.id.menu_camera).isVisible = config?.isShowCamera ?: true
            menu.findItem(R.id.menu_done).apply {
                title = ConfigUtils.getDoneButtonText(this@ImagePickerActivity, config!!)
                isVisible = imagePickerFragment.isShowDoneButton
            }
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
            imagePickerFragment.captureImage()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (this::imagePickerFragment.isInitialized) {
            if (!imagePickerFragment.handleBack()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun setupView(config: ImagePickerConfig) {
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

    override fun cancel() = finish()

    override fun selectionChanged(imageList: List<Image>?) {
        // Do nothing when the selection changes.
    }

    override fun finishPickImages(result: Intent?) {
        setResult(RESULT_OK, result)
        finish()
    }
}
