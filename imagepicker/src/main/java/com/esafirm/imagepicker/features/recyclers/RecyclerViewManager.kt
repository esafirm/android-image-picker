package com.esafirm.imagepicker.features.recyclers

import android.content.Context
import android.content.res.Configuration
import android.os.Parcelable
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.adapter.FolderPickerAdapter
import com.esafirm.imagepicker.adapter.ImagePickerAdapter
import com.esafirm.imagepicker.features.ImagePickerComponentsHolder
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.IpCons
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.listeners.OnFolderClickListener
import com.esafirm.imagepicker.listeners.OnImageClickListener
import com.esafirm.imagepicker.listeners.OnImageSelectedListener
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image
import com.esafirm.imagepicker.view.GridSpacingItemDecoration

class RecyclerViewManager(
    private val recyclerView: RecyclerView,
    private val config: ImagePickerConfig,
    orientation: Int
) {

    private val context: Context get() = recyclerView.context

    private var layoutManager: GridLayoutManager? = null
    private var itemOffsetDecoration: GridSpacingItemDecoration? = null

    private lateinit var imageAdapter: ImagePickerAdapter
    private lateinit var folderAdapter: FolderPickerAdapter

    private var foldersState: Parcelable? = null

    private var imageColumns = 0
    private var folderColumns = 0

    init {
        changeOrientation(orientation)
    }

    fun onRestoreState(recyclerState: Parcelable?) {
        layoutManager!!.onRestoreInstanceState(recyclerState)
    }

    val recyclerState: Parcelable?
        get() = layoutManager!!.onSaveInstanceState()

    /**
     * Set item size, column size base on the screen orientation
     */
    fun changeOrientation(orientation: Int) {
        imageColumns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 5
        folderColumns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        val shouldShowFolder = config.isFolderMode && isDisplayingFolderView
        val columns = if (shouldShowFolder) folderColumns else imageColumns
        layoutManager = GridLayoutManager(context, columns)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        setItemDecoration(columns)
    }

    fun setupAdapters(
        passedSelectedImage: List<Image>?,
        onImageClick: OnImageClickListener,
        onFolderClick: OnFolderClickListener
    ) {
        val isSingleMode = config.mode == ImagePickerMode.SINGLE
        val isSelectedNotEmpty = passedSelectedImage != null && passedSelectedImage.size > 1
        val selectedImages = if (isSingleMode && isSelectedNotEmpty) {
            emptyList()
        } else {
            passedSelectedImage
        }

        /* Init folder and image adapter */
        val imageLoader = ImagePickerComponentsHolder.imageLoader
        imageAdapter = ImagePickerAdapter(
            context, imageLoader, selectedImages
                ?: emptyList(), onImageClick
        )
        folderAdapter = FolderPickerAdapter(context, imageLoader) {
            foldersState = recyclerView.layoutManager?.onSaveInstanceState()
            onFolderClick(it)
        }
    }

    private fun setItemDecoration(columns: Int) {
        val currentDecoration = itemOffsetDecoration
        if (currentDecoration != null) {
            recyclerView.removeItemDecoration(currentDecoration)
        }

        val newItemDecoration = GridSpacingItemDecoration(
            columns,
            context.resources.getDimensionPixelSize(R.dimen.ef_item_padding),
            false
        )

        itemOffsetDecoration = newItemDecoration
        recyclerView.addItemDecoration(newItemDecoration)
        layoutManager?.spanCount = columns
    }

    // Returns true if a back action was handled by going back a folder; false otherwise.
    fun handleBack(): Boolean {
        if (config.isFolderMode && !isDisplayingFolderView) {
            setFolderAdapter(null)
            imageAdapter.setData(emptyList())
            return true
        }
        return false
    }

    private val isDisplayingFolderView: Boolean
        get() = recyclerView.adapter == null || recyclerView.adapter is FolderPickerAdapter

    val title: String
        get() {
            if (isDisplayingFolderView) {
                return ConfigUtils.getFolderTitle(context, config)
            }
            if (config.mode == ImagePickerMode.SINGLE) {
                return ConfigUtils.getImageTitle(context, config)
            }
            val imageSize = imageAdapter.selectedImages.size
            val useDefaultTitle = config.imageTitle.isNullOrBlank().not() && imageSize == 0
            if (useDefaultTitle) {
                return ConfigUtils.getImageTitle(context, config)
            }
            return if (config.limit == IpCons.MAX_LIMIT) {
                String.format(context.getString(R.string.ef_selected), imageSize)
            } else {
                String.format(
                    context.getString(R.string.ef_selected_with_limit),
                    imageSize,
                    config.limit
                )
            }
        }

    fun setImageAdapter(images: List<Image> = emptyList()) {
        imageAdapter.setData(images)
        setItemDecoration(imageColumns)
        recyclerView.adapter = imageAdapter
    }

    fun setFolderAdapter(folders: List<Folder>?) {
        folderAdapter.setData(folders)
        setItemDecoration(folderColumns)
        recyclerView.adapter = folderAdapter
        if (foldersState != null) {
            layoutManager!!.spanCount = folderColumns
            recyclerView.layoutManager!!.onRestoreInstanceState(foldersState)
        }
    }

    /* --------------------------------------------------- */
    /* > Images */
    /* --------------------------------------------------- */

    private fun checkAdapterIsInitialized() {
        if (!::imageAdapter.isInitialized) {
            error("Must call setupAdapters first!")
        }
    }

    val selectedImages: List<Image>
        get() {
            checkAdapterIsInitialized()
            return imageAdapter.selectedImages
        }

    fun setImageSelectedListener(listener: OnImageSelectedListener) {
        checkAdapterIsInitialized()
        imageAdapter.setImageSelectedListener(listener)
    }

    fun selectImage(isSelected: Boolean): Boolean {
        if (config.mode == ImagePickerMode.MULTIPLE) {
            if (imageAdapter.selectedImages.size >= config.limit && !isSelected) {
                Toast.makeText(context, R.string.ef_msg_limit_images, Toast.LENGTH_SHORT).show()
                return false
            }
        } else if (config.mode == ImagePickerMode.SINGLE) {
            if (imageAdapter.selectedImages.size > 0) {
                imageAdapter.removeAllSelectedSingleClick()
            }
        }
        return true
    }

    val isShowDoneButton: Boolean
        get() = (!isDisplayingFolderView
            && (imageAdapter.selectedImages.isNotEmpty() || config.showDoneButtonAlways)
            && config.returnMode !== ReturnMode.ALL && config.returnMode !== ReturnMode.GALLERY_ONLY)

}