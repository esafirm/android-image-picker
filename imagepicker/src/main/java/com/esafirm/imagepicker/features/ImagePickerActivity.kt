package com.esafirm.imagepicker.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

    companion object {
        private const val RC_CAMERA = 1011
    }

    private val cameraModule = ImagePickerComponentsHolder.cameraModule

    private var actionBar: ActionBar? = null
    private var optionsMenu: Menu? = null
    private var searchView: SearchView? = null
    private var isNeedSearch = true
    private lateinit var imagePickerFragment: ImagePickerFragment

    private val config: ImagePickerConfig? by lazy {
        intent.extras!!.getParcelable(ImagePickerConfig::class.java.simpleName)
    }

    private val cameraOnlyConfig: CameraOnlyConfig? by lazy {
        intent.extras?.getParcelable(CameraOnlyConfig::class.java.simpleName)
    }

    private val isCameraOnly by lazy { cameraOnlyConfig != null }

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
            cameraModule.getCameraIntent(this, cameraOnlyConfig!!)?.let { cameraIntent ->
                startActivityForResult(cameraIntent, RC_CAMERA)
            }
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
        this.optionsMenu = menu
        initSearchView(menu)
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
        if (id == R.id.menu_sort) {
            imagePickerFragment.showSortPopupMenu(item)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!imagePickerFragment.handleBack()) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            cameraModule.removeImage(this)
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        if (requestCode == RC_CAMERA && resultCode == Activity.RESULT_OK) {
            cameraModule.getImage(this, data) { images ->
                val result = ImagePickerUtils.createResultIntent(images)
                finishPickImages(result)
            }
        }
    }

    //region Search

    private fun initSearchView(menu: Menu) {
        searchView = menu.findItem(R.id.menu_search).actionView as? SearchView
        if (config?.isShowSearch == true) {
            searchView?.setIconifiedByDefault(true)
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    search(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    search(newText)
                    return true
                }
            })
            searchView?.setOnCloseListener {
                search(null)
                false
            }
        } else {
            searchView?.visibility = View.GONE
        }
    }

    private fun search(query: String?) {
        if (isNeedSearch) {
            imagePickerFragment.search(query)
        }
    }

    private fun closeSearchView() {
        // at the same time the onClose event is triggered, so isNeedSearch must be avoided reloading
        isNeedSearch = false
        searchView?.setQuery(null, false)
        searchView?.isIconified = true
        isNeedSearch = true
    }

    //endregion Search

    /* --------------------------------------------------- */
    /* > ImagePickerInteractionListener Methods  */
    /* --------------------------------------------------- */

    override fun setTitle(title: String?) {
        actionBar?.title = title
        if (optionsMenu != null) {
            onPrepareOptionsMenu(optionsMenu!!)
        }
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

    override fun isFolderModeChanged() {
        closeSearchView()
    }
}