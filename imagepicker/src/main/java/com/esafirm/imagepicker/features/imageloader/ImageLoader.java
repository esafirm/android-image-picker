package com.esafirm.imagepicker.features.imageloader;

import android.widget.ImageView;

public interface ImageLoader {
    void loadImage(String path, ImageView imageView, ImageType imageType);
}
