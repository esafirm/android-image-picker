package com.esafirm.imagepicker.features.camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.esafirm.imagepicker.features.ImagePickerConfigFactory;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.model.ImageFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class DefaultCameraModule implements CameraModule, Serializable {

    private String currentImagePath;

    public Intent getCameraIntent(Context context) {
        return getCameraIntent(context, ImagePickerConfigFactory.createDefault(context));
    }

    @Override
    public Intent getCameraIntent(Context context, BaseConfig config) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = ImagePickerUtils.createImageFile(config.getImageDirectory(), context);

        if (imageFile != null) {
            Context appContext = context.getApplicationContext();
            Uri uri = createCameraUri(appContext, imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            ImagePickerUtils.grantAppPermission(context, intent, uri);
            return intent;
        }
        return null;
    }

    private Uri createCameraUri(Context appContext, File imageFile) {
        currentImagePath = "file:" + imageFile.getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            ContentResolver resolver = appContext.getContentResolver();
            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            return resolver.insert(collection, values);
        }
        String providerName = String.format(Locale.ENGLISH, "%s%s", appContext.getPackageName(), ".imagepicker.provider");
        return FileProvider.getUriForFile(appContext, providerName, imageFile);
    }

    @Override
    public void getImage(final Context context, Intent intent, final OnImageReadyListener imageReadyListener) {
        if (imageReadyListener == null) {
            throw new IllegalStateException("OnImageReadyListener must not be null");
        }

        if (currentImagePath == null) {
            IpLogger.getInstance().w("currentImagePath null. " +
                    "This happen if you haven't call #getCameraIntent() or the activity is being recreated");
            imageReadyListener.onImageReady(null);
            return;
        }

        final Uri imageUri = Uri.parse(currentImagePath);
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.getApplicationContext(),
                    new String[]{imageUri.getPath()}, null, (path, uri) -> {

                        IpLogger.getInstance().d("File " + path + " was scanned successfully: " + uri);

                        if (path == null) {
                            IpLogger.getInstance().d("This should not happen, go back to Immediate implemenation");
                            path = currentImagePath;
                        }

                        imageReadyListener.onImageReady(ImageFactory.singleListFromPath(path));
                        ImagePickerUtils.revokeAppPermission(context, imageUri);
                    });
        }
    }

    @Override
    public void removeImage() {
        if (currentImagePath != null) {
            File file = new File(currentImagePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
