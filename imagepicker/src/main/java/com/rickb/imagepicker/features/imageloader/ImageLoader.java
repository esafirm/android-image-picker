package com.rickb.imagepicker.features.imageloader;

import android.widget.ImageView;

import com.rickb.imagepicker.model.Image;

public interface ImageLoader {
    void loadImage(Image image, ImageView imageView, ImageType imageType);
}
