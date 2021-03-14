package com.esafirm.imagepicker.features

import com.esafirm.imagepicker.features.common.MvpView
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.Image

interface ImagePickerView : MvpView {
    fun showLoading(isLoading: Boolean)
    fun showFetchCompleted(images: List<Image>, folders: List<Folder>)
    fun showError(throwable: Throwable?)
    fun showEmpty()
    fun showCapturedImage()
    fun finishPickImages(images: List<Image>?)
}