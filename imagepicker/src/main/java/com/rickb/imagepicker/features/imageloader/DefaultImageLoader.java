package com.rickb.imagepicker.features.imageloader;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.rickb.imagepicker.R;
import com.rickb.imagepicker.model.Image;

public class DefaultImageLoader implements ImageLoader {

    @Override
    public void loadImage(Image image, ImageView imageView, ImageType imageType) {
        Glide.with(imageView.getContext())
                .load(image.getUri())
                .apply(RequestOptions
                        .placeholderOf(imageType == ImageType.FOLDER
                                ? R.drawable.ef_folder_placeholder
                                : R.drawable.ef_image_placeholder)
                        .error(imageType == ImageType.FOLDER
                                ? R.drawable.ef_folder_placeholder
                                : R.drawable.ef_image_placeholder)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }
}
