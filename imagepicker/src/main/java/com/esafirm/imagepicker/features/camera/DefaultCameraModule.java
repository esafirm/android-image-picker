package com.esafirm.imagepicker.features.camera;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.esafirm.imagepicker.features.ImagePickerConfig;
import com.esafirm.imagepicker.features.ImagePickerConfigFactory;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.model.ImageFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class DefaultCameraModule implements CameraModule, Serializable {

    protected String currentImagePath;

    public Intent getCameraIntent(Context context) {
        return getCameraIntent(context, ImagePickerConfigFactory.createDefault(context));
    }

    @Override
    public Intent getCameraIntent(Context context, ImagePickerConfig config) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = ImagePickerUtils.createImageFile(config.getImageDirectory());
        if (imageFile != null) {
            Context appContext = context.getApplicationContext();
            String providerName = String.format(Locale.ENGLISH, "%s%s", appContext.getPackageName(), ".imagepicker.provider");
            Uri uri = FileProvider.getUriForFile(appContext, providerName, imageFile);
            currentImagePath = "file:" + imageFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            ImagePickerUtils.grantAppPermission(context, intent, uri);

            return intent;
        }
        return null;
    }

    @Override
    public void getImage(final Context context, Intent intent, final OnImageReadyListener imageReadyListener) {
        if (imageReadyListener == null) {
            throw new IllegalStateException("OnImageReadyListener must not be null");
        }

        if (currentImagePath == null) {
            imageReadyListener.onImageReady(null);
            return;
        }

        final Uri imageUri = Uri.parse(currentImagePath);
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.getApplicationContext(),
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v("ImagePicker", "File " + path + " was scanned successfully: " + uri);

                            if (path == null) {
                                path = currentImagePath;
                            }
                            imageReadyListener.onImageReady(ImageFactory.singleListFromPath(path));
                            ImagePickerUtils.revokeAppPermission(context, imageUri);
                        }
                    });
        }
    }

}
