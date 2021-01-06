package com.rickb.imagepicker.features.camera;

import com.rickb.imagepicker.model.Image;

import java.util.List;

public interface OnImageReadyListener {
    void onImageReady(List<Image> image);
}
