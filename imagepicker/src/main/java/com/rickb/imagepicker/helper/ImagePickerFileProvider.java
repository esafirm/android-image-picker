package com.rickb.imagepicker.helper;

import androidx.core.content.FileProvider;

import com.rickb.imagepicker.features.ImagePickerComponentHolder;

public class ImagePickerFileProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        ImagePickerComponentHolder.getInstance().init(getContext());
        return super.onCreate();
    }
}
