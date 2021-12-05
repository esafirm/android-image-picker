package com.esafirm.imagepicker.features

import com.esafirm.imagepicker.helper.state.ObservableState
import com.esafirm.imagepicker.helper.state.SingleEvent
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image

data class ImagePickerState(
    val images: List<Image> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isFoldersMode: SingleEvent<Boolean>? = null,
    val currentFolder: Folder? = null,
    val isLoading: Boolean = false,
    val error: SingleEvent<Throwable>? = null,
    val finishPickImage: SingleEvent<List<Image>>? = null,
    val showCapturedImage: SingleEvent<Unit>? = null
)

interface ImagePickerAction {
    fun getUiState(): ObservableState<ImagePickerState>
    fun loadData(config: ImagePickerConfig)
}