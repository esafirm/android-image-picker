package com.esafirm.imagepicker.features

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.databinding.EfActivityImagePickerBinding
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.helper.IpCrasher
import com.esafirm.imagepicker.helper.LocaleManager
import com.esafirm.imagepicker.helper.ViewUtils
import com.esafirm.imagepicker.model.Image
import java.io.File

class ImagePickerActivity : AppCompatActivity(), ImagePickerInteractionListener {

    companion object {
        private const val TAG = "ImagePickerActivity"
        private const val DEFAULT_MAX_WIDTH = 1920
        private const val DEFAULT_MAX_HEIGHT = 1080
        private const val MIN_IMAGE_SIZE = 1
    }

    private val cameraModule = ImagePickerComponentsHolder.cameraModule

    private var actionBar: ActionBar? = null
    private lateinit var imagePickerFragment: ImagePickerFragment
    private lateinit var binding: EfActivityImagePickerBinding

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
        handleCameraResult(result)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Starting ImagePickerActivity")

        try {
            initializeActivity(savedInstanceState)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            handleImageError(e, "onCreate")
            finish()
        }
    }

    private fun initializeActivity(savedInstanceState: Bundle?) {
        setResult(RESULT_CANCELED)

        /* This should not happen */
        val intent = intent
        if (intent == null || intent.extras == null) {
            Log.e(TAG, "Intent or extras is null")
            IpCrasher.openIssue()
        }

        if (isCameraOnly) {
            handleCameraOnlyMode()
            return
        }

        val currentConfig = config!!

        // Validate configuration before use
        if (!validateConfig(currentConfig)) {
            Log.e(TAG, "Invalid configuration")
            handleImageError(Exception("Invalid configuration"), "configuration")
            finish()
            return
        }

        setTheme(currentConfig.theme)

        // Apply system bar colors
        setupSystemBars(currentConfig)

        // Initialize ViewBinding
        binding = EfActivityImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView(currentConfig)
        setupImageProcessing(currentConfig)

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

    private fun handleCameraOnlyMode() {
        try {
            // Apply system bar colors even for camera-only mode
            cameraOnlyConfig?.let { setupSystemBars(it) }

            val cameraIntent = cameraModule.getCameraIntent(this, cameraOnlyConfig!!)
            startForCameraResult.launch(cameraIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in camera-only mode", e)
            handleImageError(e, "camera-only mode")
            finish()
        }
    }

    private fun handleCameraResult(result: ActivityResult) {
        val resultCode = result.resultCode
        Log.d(TAG, "Camera result received with code: $resultCode")

        if (resultCode == Activity.RESULT_CANCELED) {
            cameraModule.removeImage(this)
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            try {
                cameraModule.getImage(this, result.data) { images ->
                    if (images != null) {
                        processImages(images)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera result", e)
                handleImageError(e, "camera result processing")
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun processImages(images: List<Image>) {
        try {
            Log.d(TAG, "Processing ${images.size} images")

            // Validate images before processing
            val validImages = images.filter { image ->
                validateImage(image.path)
            }

            if (validImages.isEmpty()) {
                Log.e(TAG, "No valid images found")
                handleImageError(Exception("No valid images found"), "image validation")
                setResult(RESULT_CANCELED)
                finish()
                return
            }

            Log.d(TAG, "Found ${validImages.size} valid images out of ${images.size}")

            val resultIntent = ImagePickerUtils.createResultIntent(validImages)
            finishPickImages(resultIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing images", e)
            handleImageError(e, "image processing")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun validateImage(imagePath: String): Boolean {
        return try {
            Log.d(TAG, "Validating image: $imagePath")

            // Check if file exists
            val file = File(imagePath)
            if (!file.exists()) {
                Log.e(TAG, "Image file does not exist: $imagePath")
                return false
            }

            if (file.length() == 0L) {
                Log.e(TAG, "Image file is empty: $imagePath")
                return false
            }

            // Check if we can decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            val isValid = options.outWidth > MIN_IMAGE_SIZE && options.outHeight > MIN_IMAGE_SIZE
            if (!isValid) {
                Log.e(TAG, "Invalid image dimensions: ${options.outWidth}x${options.outHeight} for $imagePath")
            } else {
                Log.d(TAG, "Valid image: ${options.outWidth}x${options.outHeight} for $imagePath")
            }

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error validating image file: $imagePath", e)
            false
        }
    }

    private fun decodeSampledBitmapFromFile(imagePath: String, reqWidth: Int = maxImageWidth, reqHeight: Int = maxImageHeight): Bitmap? {
        return try {
            Log.d(TAG, "Decoding bitmap from: $imagePath")

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e(TAG, "Invalid image dimensions: ${options.outWidth}x${options.outHeight}")
                return null
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory

            val bitmap = BitmapFactory.decodeFile(imagePath, options)

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from: $imagePath")
            } else {
                Log.d(TAG, "Successfully decoded bitmap: ${bitmap.width}x${bitmap.height}")
            }

            bitmap
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while decoding bitmap: $imagePath", e)
            System.gc() // Suggest garbage collection
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap: $imagePath", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        Log.d(TAG, "Calculated inSampleSize: $inSampleSize for ${width}x${height} -> ${reqWidth}x${reqHeight}")
        return inSampleSize
    }

    private fun validateConfig(config: ImagePickerConfig): Boolean {
        return try {
            // Check if theme is valid
            if (config.theme != 0) {
                setTheme(config.theme)
            }

            // Validate color values if they're not NO_COLOR
            if (config.statusBarColor != ImagePickerConfig.NO_COLOR) {
                // Ensure it's a valid color by trying to parse it
                val colorString = String.format("#%08X", config.statusBarColor and 0xFFFFFFFF.toInt())
                Color.parseColor(colorString)
            }

            if (config.navigationBarColor != ImagePickerConfig.NO_COLOR) {
                val colorString = String.format("#%08X", config.navigationBarColor and 0xFFFFFFFF.toInt())
                Color.parseColor(colorString)
            }

            if (config.arrowColor != ImagePickerConfig.NO_COLOR) {
                val colorString = String.format("#%08X", config.arrowColor and 0xFFFFFFFF.toInt())
                Color.parseColor(colorString)
            }

            Log.d(TAG, "Configuration validation passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Invalid configuration", e)
            false
        }
    }

    private var maxImageWidth: Int = DEFAULT_MAX_WIDTH
    private var maxImageHeight: Int = DEFAULT_MAX_HEIGHT
    private var imageCacheSize: Int = 0

    private fun setupImageProcessing(config: ImagePickerConfig) {
        try {
            // Set maximum image size limits - use defaults since config doesn't have these properties
            maxImageWidth = DEFAULT_MAX_WIDTH
            maxImageHeight = DEFAULT_MAX_HEIGHT

            // Adjust based on device screen size if possible
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Use screen dimensions as reasonable limits (with some padding)
            maxImageWidth = minOf(DEFAULT_MAX_WIDTH, screenWidth * 2)
            maxImageHeight = minOf(DEFAULT_MAX_HEIGHT, screenHeight * 2)

            // Set reasonable memory limits
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryClass = activityManager.memoryClass
            imageCacheSize = 1024 * 1024 * memoryClass / 8 // Use 1/8th of available memory

            // Set bitmap options for better memory management
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inPurgeable = true
                inInputShareable = true
            }

            Log.d(TAG, "Image processing setup - Max size: ${maxImageWidth}x${maxImageHeight}, Cache size: $imageCacheSize, Screen: ${screenWidth}x${screenHeight}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up image processing", e)
            // Fallback to defaults if anything goes wrong
            maxImageWidth = DEFAULT_MAX_WIDTH
            maxImageHeight = DEFAULT_MAX_HEIGHT
        }
    }

    private fun handleImageError(error: Exception, context: String) {
        Log.e(TAG, "Image processing error in $context", error)

        // Show user-friendly error message
        runOnUiThread {
            val message = when (error) {
                is OutOfMemoryError -> "Image too large. Please try a smaller image."
                is SecurityException -> "Permission denied accessing image."
                else -> "Error processing image. Please try again."
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSystemBars(config: BaseConfig) {
        try {
            val statusBarColor = when (config) {
                is ImagePickerConfig -> config.statusBarColor
                else -> ImagePickerConfig.NO_COLOR
            }

            val navigationBarColor = when (config) {
                is ImagePickerConfig -> config.navigationBarColor
                else -> ImagePickerConfig.NO_COLOR
            }

            val lightStatusBar = when (config) {
                is ImagePickerConfig -> config.lightStatusBar
                else -> false
            }

            val lightNavigationBar = when (config) {
                is ImagePickerConfig -> config.lightNavigationBar
                else -> false
            }

            window.apply {
                // Set status bar color
                if (statusBarColor != ImagePickerConfig.NO_COLOR) {
                    this.statusBarColor = statusBarColor
                    Log.d(TAG, "Set status bar color: $statusBarColor")
                }

                // Set navigation bar color
                if (navigationBarColor != ImagePickerConfig.NO_COLOR) {
                    this.navigationBarColor = navigationBarColor
                    Log.d(TAG, "Set navigation bar color: $navigationBarColor")
                }

                // Modern approach for Android 11+ (API 30+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Ensure the decor view is available
                    val decorView = window.decorView
                    if (decorView != null) {
                        val controller = decorView.windowInsetsController
                        if (controller != null) {
                            // Set status bar appearance
                            if (lightStatusBar) {
                                controller.setSystemBarsAppearance(
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                )
                            } else {
                                controller.setSystemBarsAppearance(
                                    0,
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                )
                            }

                            // Set navigation bar appearance
                            if (lightNavigationBar) {
                                controller.setSystemBarsAppearance(
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                                )
                            } else {
                                controller.setSystemBarsAppearance(
                                    0,
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                                )
                            }
                        }
                    }
                } else {
                    // Fallback for older versions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (lightStatusBar) {
                            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        } else {
                            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (lightNavigationBar) {
                            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        } else {
                            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up system bars", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ImagePickerActivity destroyed")
        // ViewBinding doesn't require explicit cleanup for Activities
        // as the binding is automatically cleared when the Activity is destroyed
    }

    /**
     * Create option menus.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ef_image_picker_menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        try {
            if (!isCameraOnly) {
                menu.findItem(R.id.menu_camera).isVisible = config?.isShowCamera ?: true
                menu.findItem(R.id.menu_done).apply {
                    title = ConfigUtils.getDoneButtonText(this@ImagePickerActivity, config!!)
                    isVisible = imagePickerFragment.isShowDoneButton
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing options menu", e)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handle option menu's click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error handling options item selection", e)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        try {
            if (this::imagePickerFragment.isInitialized) {
                if (!imagePickerFragment.handleBack()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling back press", e)
            super.onBackPressed()
        }
    }

    private fun setupView(config: ImagePickerConfig) {
        try {
            setSupportActionBar(binding.toolbar.root as androidx.appcompat.widget.Toolbar)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up view", e)
        }
    }

    /* --------------------------------------------------- */
    /* > ImagePickerInteractionListener Methods  */
    /* --------------------------------------------------- */

    override fun setTitle(title: String?) {
        try {
            actionBar?.title = title
            invalidateOptionsMenu()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting title", e)
        }
    }

    override fun cancel() {
        try {
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling", e)
        }
    }

    override fun selectionChanged(imageList: List<Image>?) {
        // Do nothing when the selection changes.
        // This method is intentionally left empty as per original implementation
    }

    override fun finishPickImages(result: Intent?) {
        try {
            setResult(RESULT_OK, result)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing pick images", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}