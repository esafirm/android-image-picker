package com.esafirm.imagepicker.features

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.databinding.EfFragmentImagePickerBinding
import com.esafirm.imagepicker.features.camera.CameraHelper.checkCameraAvailability
import com.esafirm.imagepicker.features.fileloader.DefaultImageFileLoader
import com.esafirm.imagepicker.features.recyclers.RecyclerViewManager
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.helper.ImagePickerPreferences
import com.esafirm.imagepicker.helper.ImagePickerUtils
import com.esafirm.imagepicker.helper.IpLogger
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image
import java.util.*

class ImagePickerFragment : Fragment(), ImagePickerView {

    private var binding: EfFragmentImagePickerBinding? = null
    private val logger = IpLogger

    private val preferences: ImagePickerPreferences by lazy {
        ImagePickerPreferences(requireContext())
    }

    private val config: ImagePickerConfig by lazy {
        arguments!!.getParcelable(ImagePickerConfig::class.java.simpleName)!!
    }

    private lateinit var presenter: ImagePickerPresenter

    private var recyclerViewManager: RecyclerViewManager? = null
    private var interactionListener: ImagePickerInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ContentObserverTrigger(
            requireActivity().contentResolver,
            this::loadData
        ))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupComponents()

        val interactionListener = this.interactionListener
            ?: throw RuntimeException("ImagePickerFragment needs an " +
                "ImagePickerInteractionListener. This will be set automatically if the " +
                "activity implements ImagePickerInteractionListener, and can be set manually " +
                "with fragment.setInteractionListener(listener).")

        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater.cloneInContext(ContextThemeWrapper(activity, config.theme))

        // inflate the layout using the cloned inflater, not default inflater
        val view = localInflater.inflate(R.layout.ef_fragment_image_picker, container, false)
        val viewBinding = EfFragmentImagePickerBinding.bind(view)

        val selectedImages = if (savedInstanceState == null) {
            config.selectedImages
        } else {
            savedInstanceState.getParcelableArrayList(STATE_KEY_SELECTED_IMAGES)
        }

        val recyclerViewManager = createRecyclerViewManager(
            recyclerView = viewBinding.recyclerView,
            config = config,
            passedSelectedImages = selectedImages ?: emptyList(),
            interactionListener = interactionListener
        )

        if (savedInstanceState != null) {
            recyclerViewManager.onRestoreState(savedInstanceState.getParcelable(STATE_KEY_RECYCLER))
        }

        interactionListener.selectionChanged(recyclerViewManager.selectedImages)

        // Assignment for property
        this.binding = viewBinding
        this.recyclerViewManager = recyclerViewManager

        return view
    }

    private fun createRecyclerViewManager(
        recyclerView: RecyclerView,
        config: ImagePickerConfig,
        passedSelectedImages: List<Image>,
        interactionListener: ImagePickerInteractionListener
    ) = RecyclerViewManager(
        recyclerView,
        config,
        resources.configuration.orientation
    ).apply {
        val selectListener = { isSelected: Boolean -> selectImage(isSelected) }
        val folderClick = { bucket: Folder -> setImageAdapter(bucket.images) }

        setupAdapters(passedSelectedImages, selectListener, folderClick)
        setImageSelectedListener { selectedImages ->
            updateTitle()
            interactionListener.selectionChanged(selectedImages)
            if (ConfigUtils.shouldReturn(config, false) && selectedImages.isNotEmpty()) {
                onDone()
            }
        }
    }

    private fun setupComponents() {
        presenter = ImagePickerPresenter(DefaultImageFileLoader(requireContext()))
        presenter.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        loadDataWithPermission()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY_RECYCLER, recyclerViewManager!!.recyclerState)
        outState.putParcelableArrayList(STATE_KEY_SELECTED_IMAGES, recyclerViewManager!!.selectedImages as ArrayList<out Parcelable?>)
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private fun setImageAdapter(images: List<Image>) {
        recyclerViewManager!!.setImageAdapter(images)
        updateTitle()
    }

    private fun setFolderAdapter(folders: List<Folder>) {
        recyclerViewManager!!.setFolderAdapter(folders)
        updateTitle()
    }

    private fun updateTitle() {
        interactionListener!!.setTitle(recyclerViewManager!!.title)
    }

    /**
     * On finish selected image
     * Get all selected images then return image to caller activity
     */
    fun onDone() {
        presenter.onDoneSelectImages(recyclerViewManager!!.selectedImages)
    }

    /**
     * Config recyclerView when configuration changed
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // recyclerViewManager can be null here if we use cameraOnly mode
        recyclerViewManager?.changeOrientation(newConfig.orientation)
    }

    /**
     * Check permission
     */
    private fun loadDataWithPermission() {
        val rc = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            loadData()
        } else {
            requestWriteExternalPermission()
        }
    }

    private fun loadData() {
        presenter.abortLoad()
        presenter.loadImages(config)
    }

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Never Ask Again', show snackbar with an action that navigate to app settings
     */
    private fun requestWriteExternalPermission() {
        logger.w("Write External permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            val permission = ImagePickerPreferences.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission)
                requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
            } else {
                binding!!.efSnackbar.show(R.string.ef_msg_no_write_external_permission) { v: View? -> openAppSettings() }
            }
        }
    }

    /**
     * Handle permission results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Write External permission granted")
                    loadData()
                    return
                }
                logger.e("Permission not granted: results len = " + grantResults.size +
                    " Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)")
                interactionListener!!.cancel()
            }
            else -> {
                logger.d("Got unexpected permission result: $requestCode")
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    /**
     * Open app settings screen
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**
     * Check if the captured image is stored successfully
     * Then reload data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.finishCaptureImage(requireContext(), data, config)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                presenter.abortCaptureImage()
            }
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    fun captureImage() {
        if (!checkCameraAvailability(activity!!)) {
            return
        }
        presenter.captureImage(this, config, RC_CAPTURE)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.abortLoad()
        presenter.detachView()
    }

    /**
     * @return true if the [Fragment] consume the back event
     */
    fun handleBack(): Boolean {
        if (recyclerViewManager!!.handleBack()) {
            // Handled.
            updateTitle()
            return true
        }
        return false
    }

    val isShowDoneButton: Boolean
        get() = recyclerViewManager!!.isShowDoneButton

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ImagePickerInteractionListener) {
            interactionListener = context
        }
    }

    fun setInteractionListener(listener: ImagePickerInteractionListener?) {
        interactionListener = listener
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    override fun finishPickImages(images: List<Image>?) {
        interactionListener?.finishPickImages(ImagePickerUtils.createResultIntent(images))
    }

    override fun showCapturedImage() {
        loadDataWithPermission()
    }

    override fun showFetchCompleted(images: List<Image>, folders: List<Folder>) {
        if (config.isFolderMode) {
            setFolderAdapter(folders)
        } else {
            setImageAdapter(images)
        }
    }

    override fun showError(throwable: Throwable?) {
        var message = "Unknown Error"
        if (throwable is NullPointerException) {
            message = "Images do not exist"
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(isLoading: Boolean) = binding!!.run {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        tvEmptyImages.visibility = View.GONE
    }

    override fun showEmpty() = binding!!.run {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyImages.visibility = View.VISIBLE
    }

    companion object {
        private const val STATE_KEY_RECYCLER = "Key.Recycler"
        private const val STATE_KEY_SELECTED_IMAGES = "Key.SelectedImages"

        private const val RC_CAPTURE = 2000
        private const val RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23

        fun newInstance(config: ImagePickerConfig): ImagePickerFragment {
            val args = Bundle().apply {
                putParcelable(ImagePickerConfig::class.java.simpleName, config)
            }
            return ImagePickerFragment().apply {
                arguments = args
            }
        }
    }
}