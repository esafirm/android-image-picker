package com.esafirm.imagepicker.helper

import androidx.core.content.FileProvider
import com.esafirm.imagepicker.features.DefaultImagePickerComponents
import com.esafirm.imagepicker.features.ImagePickerComponentsHolder

class ImagePickerFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        ImagePickerComponentsHolder.setInternalComponent(DefaultImagePickerComponents(context!!))
        return super.onCreate()
    }
}