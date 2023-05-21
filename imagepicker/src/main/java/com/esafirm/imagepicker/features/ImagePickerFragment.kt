package com.esafirm.imagepicker.features

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
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
import com.esafirm.imagepicker.helper.state.fetch
import com.esafirm.imagepicker.model.BaseItem
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image
import java.util.ArrayList
import java.util.regex.Pattern

class ImagePickerFragment : Fragment() {

    private var binding: EfFragmentImagePickerBinding? = null
    private lateinit var recyclerViewManager: RecyclerViewManager

    private val preferences: ImagePickerPreferences by lazy {
        ImagePickerPreferences(requireContext())
    }

    private val config: ImagePickerConfig by lazy {
        requireArguments().getParcelable(ImagePickerConfig::class.java.simpleName)!!
    }

    private val permissions: Array<String> by lazy {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)

            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || Environment.isExternalStorageLegacy() -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestMultiplePermissions()) { resultMap ->
            val isGranted = resultMap.values.all { it }
            if (isGranted) {
                IpLogger.d("Write External permission granted")
                loadData()
            } else {
                IpLogger.e("Permission not granted")
                interactionListener.cancel()
            }
        }

    private lateinit var presenter: ImagePickerPresenter
    private lateinit var interactionListener: ImagePickerInteractionListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(
            ContentObserverTrigger(
                requireActivity().contentResolver,
                this::loadData
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        presenter = ImagePickerPresenter(DefaultImageFileLoader(requireContext()))

        if (::interactionListener.isInitialized.not()) {
            throw RuntimeException(
                "ImagePickerFragment needs an " +
                    "ImagePickerInteractionListener. This will be set automatically if the " +
                    "activity implements ImagePickerInteractionListener, and can be set manually " +
                    "with fragment.setInteractionListener(listener)."
            )
        }

        val interactionListener = this.interactionListener

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToUiState()
    }

    private fun subscribeToUiState() = presenter.getUiState().observe(this) { state ->
        showLoading(state.isLoading, hideRecyclerView = true)

        state.error.fetch {
            showError(this)
        }

        val isEmpty = state.images.isEmpty()
        if (isEmpty && state.isLoading.not()) {
            showEmpty()
            return@observe
        }

        state.isFoldersMode.fetch {
            config.searchQuery = null
            val isFolderMode = this
            if (isFolderMode) {
                setFolderAdapter(state.folders)
            } else {
                setImageAdapter(state.currentFolder?.images ?: emptyList())
            }
            interactionListener.isFolderModeChanged()
        }

        state.finishPickImage.fetch {
            val images = this
            interactionListener.finishPickImages(
                ImagePickerUtils.createResultIntent(images)
            )
        }

        state.showCapturedImage.fetch {
            loadDataWithPermission()
        }
    }

    private fun <T : BaseItem> filter(list: List<T>): List<T> {
        val regex = config.searchQuery?.let {
            if (it.isNotBlank()) buildRegex(it) else null
        }
        return if (regex != null) {
            list.filter { it.getItemName().matches(regex) }
        } else {
            list
        }
    }

    private fun sortFolders(list: List<Folder>): List<Folder> {
        return when (config.foldersSortMode) {
            FolderSortMode.NAME_ASC -> list.sortedBy { it.folderName }
            FolderSortMode.NAME_DESC -> list.sortedByDescending { it.folderName }
            FolderSortMode.NUMBER_ASC -> list.sortedBy { it.images.size }
            FolderSortMode.NUMBER_DESC -> list.sortedByDescending { it.images.size }
            else -> list
        }
    }

    private fun sortImages(list: List<Image>): List<Image> {
        return when (config.imagesSortMode) {
            ImageSortMode.NAME_ASC -> list.sortedBy { it.name }
            ImageSortMode.NAME_DESC -> list.sortedByDescending { it.name }
            ImageSortMode.TYPE_ASC -> list.sortedBy { it.type }
            ImageSortMode.TYPE_DESC -> list.sortedByDescending { it.type }
            ImageSortMode.DATE_MODIFIED_ASC -> list.sortedBy { it.dateModified }
            ImageSortMode.DATE_MODIFIED_DESC -> list.sortedByDescending { it.dateModified }
            ImageSortMode.SIZE_ASC -> list.sortedBy { it.size }
            ImageSortMode.SIZE_DESC -> list.sortedByDescending { it.size }
            else -> list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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
        val folderClick = { bucket: Folder ->
            presenter.setFolder(bucket)
            updateTitle()
        }

        setupAdapters(passedSelectedImages, selectListener, folderClick)
        setImageSelectedListener { selectedImages ->
            updateTitle()
            interactionListener.selectionChanged(selectedImages)
            if (ConfigUtils.shouldReturn(config, false) && selectedImages.isNotEmpty()) {
                onDone()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataWithPermission()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY_RECYCLER, recyclerViewManager.recyclerState)
        outState.putParcelableArrayList(
            STATE_KEY_SELECTED_IMAGES,
            recyclerViewManager.selectedImages as ArrayList<out Parcelable?>
        )
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private fun setImageAdapter(images: List<Image>) {
        filter(images)
            .let { sortImages(it) }
            .let {
                showLoading(true)
                recyclerViewManager.setImageAdapter(it) {
                    showLoading(false)
                }
                checkDataIsEmpty(it)
                updateTitle()
            }
    }

    private fun setFolderAdapter(folders: List<Folder>) {
        filter(folders)
            .let { sortFolders(it) }
            .let {
                showLoading(true)
                recyclerViewManager.setFolderAdapter(it) {
                    showLoading(false)
                }
                checkDataIsEmpty(it)
                updateTitle()
            }
    }

    private fun checkDataIsEmpty(data: List<*>) {
        binding?.tvEmptyImages?.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateTitle() {
        val state = presenter.getUiState().get()
        interactionListener.setTitle(recyclerViewManager.getTitle(state.currentFolder?.folderName))
    }

    /**
     * On finish selected image
     * Get all selected images then return image to caller activity
     */
    fun onDone() {
        presenter.onDoneSelectImages(recyclerViewManager.selectedImages, config)
    }

    /**
     * Config recyclerView when configuration changed
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // recyclerViewManager can be null here if we use cameraOnly mode
        recyclerViewManager.changeOrientation(newConfig.orientation)
    }

    /**
     * Check permission
     */
    private fun loadDataWithPermission() {
        val allGranted = permissions.all {
            ActivityCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            loadData()
        } else {
            requestWriteExternalOrReadImagesPermission()
        }
    }

    private fun loadData() = presenter.loadData(config)

    fun search(searchQuery: String?) {
        config.searchQuery = searchQuery
        val state = presenter.getUiState().get()
        if (state.currentFolder == null) {
            setFolderAdapter(state.folders)
        } else {
            setImageAdapter(state.currentFolder.images)
        }
    }

    fun sortFolders(sortMode: FolderSortMode) {
        config.foldersSortMode = sortMode
        val state = presenter.getUiState().get()
        setFolderAdapter(state.folders)
    }

    fun sortImages(sortMode: ImageSortMode) {
        config.imagesSortMode = sortMode
        val state = presenter.getUiState().get()
        setImageAdapter(state.currentFolder?.images ?: emptyList())
    }

    //region Sort

    @SuppressLint("NonConstantResourceId")
    fun showSortPopupMenu(menuItem: MenuItem) {
        val view: View = requireActivity().findViewById(menuItem.itemId)
        val popupMenu = PopupMenu(requireContext(), view)
        val isFoldersMode = isFoldersMode
        popupMenu.inflate(if (isFoldersMode) R.menu.ef_sort_folders else R.menu.ef_sort_images)

        // select the current sort mode
        val selectedMenuItemId = if (isFoldersMode) {
            when (config.foldersSortMode) {
                FolderSortMode.NAME_ASC -> R.id.action_sort_folder_name_asc
                FolderSortMode.NAME_DESC -> R.id.action_sort_folder_name_desc
                FolderSortMode.NUMBER_ASC -> R.id.action_sort_num_asc
                FolderSortMode.NUMBER_DESC -> R.id.action_sort_num_desc
                else -> R.id.action_sort_num_desc
            }
        } else {
            when (config.imagesSortMode) {
                ImageSortMode.NAME_ASC -> R.id.action_sort_name_asc
                ImageSortMode.NAME_DESC -> R.id.action_sort_name_desc
                ImageSortMode.TYPE_ASC -> R.id.action_sort_type_asc
                ImageSortMode.TYPE_DESC -> R.id.action_sort_type_desc
                ImageSortMode.DATE_MODIFIED_ASC -> R.id.action_sort_date_asc
                ImageSortMode.DATE_MODIFIED_DESC -> R.id.action_sort_date_desc
                ImageSortMode.SIZE_ASC -> R.id.action_sort_size_asc
                ImageSortMode.SIZE_DESC -> R.id.action_sort_size_desc
                else -> R.id.action_sort_date_desc
            }
        }

        val selectedItem = popupMenu.menu.findItem(selectedMenuItemId)
        selectedItem.isChecked = true

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val result = when (item.itemId) {
                // folders
                R.id.action_sort_folder_name_asc -> {
                    sortFolders(FolderSortMode.NAME_ASC)
                    true
                }
                R.id.action_sort_folder_name_desc -> {
                    sortFolders(FolderSortMode.NAME_DESC)
                    true
                }
                R.id.action_sort_num_asc -> {
                    sortFolders(FolderSortMode.NUMBER_ASC)
                    true
                }
                R.id.action_sort_num_desc -> {
                    sortFolders(FolderSortMode.NUMBER_DESC)
                    true
                }
                // images
                R.id.action_sort_name_asc -> {
                    sortImages(ImageSortMode.NAME_ASC)
                    true
                }
                R.id.action_sort_name_desc -> {
                    sortImages(ImageSortMode.NAME_DESC)
                    true
                }
                R.id.action_sort_type_asc -> {
                    sortImages(ImageSortMode.TYPE_ASC)
                    true
                }
                R.id.action_sort_type_desc -> {
                    sortImages(ImageSortMode.TYPE_DESC)
                    true
                }
                R.id.action_sort_date_asc -> {
                    sortImages(ImageSortMode.DATE_MODIFIED_ASC)
                    true
                }
                R.id.action_sort_date_desc -> {
                    sortImages(ImageSortMode.DATE_MODIFIED_DESC)
                    true
                }
                R.id.action_sort_size_asc -> {
                    sortImages(ImageSortMode.SIZE_ASC)
                    true
                }
                R.id.action_sort_size_desc -> {
                    sortImages(ImageSortMode.SIZE_DESC)
                    true
                }
                else -> false
            }
            if (result) {
                selectedItem.isChecked = false
                item.isChecked = true
            }
            return@setOnMenuItemClickListener result
        }

        (popupMenu.menu as? MenuBuilder)?.let {
            setForceShowMenuIcons(view, it)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setForceShowMenuIcons(v: View, menu: MenuBuilder) {
        val menuHelper = MenuPopupHelper(requireContext(), menu, v)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
    }

    //endregion Sort

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Never Ask Again', show snackbar with an action that navigate to app settings
     */
    private fun requestWriteExternalOrReadImagesPermission() {
        IpLogger.w("Write External permission or Read Media Images is not granted. Requesting permission")

        val shouldProvideRationale = permissions.any { permission -> shouldShowRequestPermissionRationale(permission) }
        if (shouldProvideRationale) {
            requestPermissionLauncher.launch(permissions)
            return
        }

        if (!preferences.isPermissionRequested()) {
            preferences.setPermissionIsRequested()
            requestPermissionLauncher.launch(permissions)
        } else {
            binding?.efSnackbar?.show(R.string.ef_msg_no_write_external_permission) {
                openAppSettings()
            }
        }
    }

    /**
     * Open app settings screen
     */
    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
        )
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
                presenter.abortCaptureImage(requireContext())
            }
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    fun captureImage() {
        if (!checkCameraAvailability(requireActivity())) {
            return
        }
        presenter.captureImage(this, config, RC_CAPTURE)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.abortLoad()
    }

    /**
     * @return true if the [Fragment] consume the back event
     */
    fun handleBack(): Boolean {
        if (recyclerViewManager.handleBack()) {
            presenter.setFolder(null)
            // Handled.
            return true
        }
        return false
    }

    val isShowDoneButton: Boolean
        get() = recyclerViewManager.isShowDoneButton

    val isFoldersMode: Boolean
        get() = presenter.getUiState().get().currentFolder == null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ImagePickerInteractionListener) {
            setInteractionListener(context)
        }
    }

    fun setInteractionListener(listener: ImagePickerInteractionListener) {
        interactionListener = listener
    }

    private fun showError(throwable: Throwable?) {
        var message = "Unknown Error"
        if (throwable is NullPointerException) {
            message = "Images do not exist"
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean, hideRecyclerView: Boolean = false) {
        binding?.run {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (hideRecyclerView) {
                recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
            tvEmptyImages.visibility = View.GONE
        }
    }

    private fun showEmpty() {
        binding?.run {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            tvEmptyImages.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val STATE_KEY_RECYCLER = "Key.Recycler"
        private const val STATE_KEY_SELECTED_IMAGES = "Key.SelectedImages"

        private const val RC_CAPTURE = 2000

        fun newInstance(config: ImagePickerConfig): ImagePickerFragment {
            val args = Bundle().apply {
                putParcelable(ImagePickerConfig::class.java.simpleName, config)
            }
            return ImagePickerFragment().apply {
                arguments = args
            }
        }

        private fun buildRegex(query: String): Regex {
            return Regex("(?is)" + ".*" + Pattern.quote(query) + ".*")
        }
    }
}