package com.esafirm.imagepicker.features.imageloader;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.R;

public class DefaultImageLoader implements ImageLoader {

    @Override
    public void loadImage(String path, ImageView imageView, ImageType imageType) {
        Glide.with(imageView.getContext())
                .load(path)
                .placeholder(imageType == ImageType.FOLDER ? R.drawable.folder_placeholder : R.drawable.image_placeholder)
                .error(imageType == ImageType.FOLDER ? R.drawable.folder_placeholder : R.drawable.image_placeholder)
                .into(imageView);
    }
}
