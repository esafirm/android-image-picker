package com.esafirm.sample;

import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.esafirm.imagepicker.features.imageloader.GlideApp;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;

public class GrayscaleImageLoader implements ImageLoader {
    @Override
    public void loadImage(String path, ImageView imageView, ImageType imageType) {
        GlideApp.with(imageView.getContext())
                .asBitmap()
                .load(path)
                .transition(BitmapTransitionOptions.withCrossFade())
                .transform(new GrayscaleTransformation())
                .into(imageView);
    }
}
