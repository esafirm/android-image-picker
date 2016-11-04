package com.esafirm.imagepicker.features;

import android.app.Activity;
import android.support.v4.app.Fragment;

public class ImagePickerLauncher {

    private Activity activity;
    private Fragment fragment;

    private ImagePickerConfig config;

    private ImagePickerLauncher(Activity activity) {
        this.activity = activity;
        init();
    }

    private ImagePickerLauncher(Fragment fragment) {
        this.fragment = fragment;
        init();
    }

    private void init() {

    }

    public void setImagePickerConfig(ImagePickerConfig config) {
        this.config = config;
    }

    public void start(int requestCode) {

    }
}
