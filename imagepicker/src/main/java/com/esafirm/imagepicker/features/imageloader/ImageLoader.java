package com.esafirm.imagepicker.features.imageloader;

import android.widget.ImageView;

import com.esafirm.imagepicker.model.Image;

public interface ImageLoader {
    void loadImage(Image image, ImageView imageView, ImageType imageType);
}
