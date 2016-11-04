package com.esafirm.imagepicker.features.camera;

import com.esafirm.imagepicker.model.Image;

import java.util.List;

public interface OnImageReadyListener {
    void onImageReady(List<Image> image);
}
