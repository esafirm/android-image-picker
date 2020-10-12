package com.esafirm.imagepicker.listeners

import com.esafirm.imagepicker.model.Image

interface OnImageSelectedListener {
    fun onSelectionUpdate(selectedImage: List<Image?>?)
}