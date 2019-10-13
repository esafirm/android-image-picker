package com.esafirm.imagepicker.helper;

import androidx.core.content.FileProvider;

import com.esafirm.imagepicker.features.ImagePickerComponentHolder;

public class ImagePickerFileProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        ImagePickerComponentHolder.getInstance().init(getContext());
        return super.onCreate();
    }
}
