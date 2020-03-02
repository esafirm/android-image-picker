package com.esafirm.imagepicker.features.camera;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.esafirm.imagepicker.features.ImagePickerConfigFactory;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.model.ImageFactory;

import java.io.File;
import java.util.Calendar;

public class DefaultCameraModule implements CameraModule, Parcelable {

    private Uri uri;
    private String currentImagePath;

    public DefaultCameraModule() {

    }

    public static final Creator<DefaultCameraModule> CREATOR = new Creator<DefaultCameraModule>() {
        @Override
        public DefaultCameraModule createFromParcel(Parcel in) {
            return new DefaultCameraModule(in);
        }

        @Override
        public DefaultCameraModule[] newArray(int size) {
            return new DefaultCameraModule[size];
        }
    };

    public Intent getCameraIntent(Context context) {
        return getCameraIntent(context, ImagePickerConfigFactory.createDefault(context));
    }

    @Override
    public Intent getCameraIntent(Context context, BaseConfig config) {
        ContentValues values = new ContentValues(3);
        String displayName = ImagePickerUtils.getFileName(0);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, Calendar.getInstance().getTimeInMillis());
        values.put(MediaStore.Images.Media.DATE_ADDED, Calendar.getInstance().getTimeInMillis());

        uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if(uri != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
            IpLogger.getInstance().w("currentImagePath null. " +
                    "This happen if you haven't call #getCameraIntent() or the activity is being recreated");
            imageReadyListener.onImageReady(null);
            return;
        }

        imageReadyListener.onImageReady(ImageFactory.singleListFromPath(uri));
        ImagePickerUtils.revokeAppPermission(context, uri);
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

    private DefaultCameraModule(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        currentImagePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(currentImagePath);
    }
}
