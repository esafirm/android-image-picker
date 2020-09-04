package com.esafirm.imagepicker.features.imageloader;

import java.io.InputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.model.Image;

public class DefaultImageLoader implements ImageLoader {
    private static final String TAG = "ImagePicker";

    @Override
    public void loadImage(Image image, ImageView imageView, ImageType imageType) {
        Uri uri = image.getUri();
        Glide.with(imageView.getContext())
                .load(uri)
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
        InputStream inputStream;
        try {
          inputStream = imageView.getContext()
            .getContentResolver()
            .openInputStream(uri);
          ExifInterface exif = new ExifInterface(inputStream);
          int orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL);
          inputStream.close();
          imageView.setRotation(getDegrees(orientation));
        } catch (IOException e) {
          Log.e(TAG, e.toString());
        }
    }

    public float getDegrees(int orientation) {
        float degrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = -90;
                break;
        }
        return degrees;
    }
}
