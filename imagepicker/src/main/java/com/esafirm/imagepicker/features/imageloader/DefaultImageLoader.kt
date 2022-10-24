package com.esafirm.imagepicker.features.imageloader

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.R
import com.esafirm.imagepicker.helper.getImageSize
import com.esafirm.imagepicker.model.Image

class DefaultImageLoader : ImageLoader {
    override fun loadImage(image: Image, imageView: ImageView, imageType: ImageType) {
        Glide.with(imageView.context)
            .load(image.uri)
            .override(imageView.context.getImageSize())
            .apply(RequestOptions
                .placeholderOf(if (imageType == ImageType.FOLDER) R.drawable.ef_folder_placeholder else R.drawable.ef_image_placeholder)
                .error(if (imageType == ImageType.FOLDER) R.drawable.ef_folder_placeholder else R.drawable.ef_image_placeholder)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}