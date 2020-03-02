package com.esafirm.imagepicker.features.imageloader;

import android.net.Uri;
import android.widget.ImageView;

public interface ImageLoader {
    void loadImage(Uri contentUri, ImageView imageView, ImageType imageType);
}
