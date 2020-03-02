package com.esafirm.sample;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;

public class GrayscaleImageLoader implements ImageLoader {

    private static final RequestOptions REQUEST_OPTIONS = new RequestOptions().transform(new GrayscaleTransformation());

    @Override
    public void loadImage(Uri contentUri, ImageView imageView, ImageType imageType) {
        Glide.with(imageView)
                .asBitmap()
                .load(contentUri)
                .transition(BitmapTransitionOptions.withCrossFade())
                .apply(REQUEST_OPTIONS)
                .into(imageView);
    }
}
