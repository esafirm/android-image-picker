package com.esafirm.imagepicker.listeners;

import com.esafirm.imagepicker.model.Image;

import java.util.List;

public interface OnImageSelectedListener {
    void onSelectionUpdate(List<Image> selectedImage);
}
