package com.rickb.imagepicker.listeners

import com.rickb.imagepicker.model.Image

interface OnImageSelectedListener {
    fun onSelectionUpdate(selectedImage: List<Image?>?)
}