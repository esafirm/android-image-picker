package com.esafirm.imagepicker.features;

import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader;

import java.util.ArrayList;

public class ImagePickerConfigFactory {

    public static ImagePickerConfig createDefault() {
        ImagePickerConfig config = new ImagePickerConfig();
        config.setMode(ImagePicker.MODE_MULTIPLE);
        config.setLimit(ImagePicker.MAX_LIMIT);
        config.setShowCamera(true);
        config.setFolderMode(false);
        config.setSelectedImages(new ArrayList<>());
        config.setSavePath(ImagePickerSavePath.DEFAULT);
        config.setReturnAfterFirst(false);
        config.setImageLoader(new DefaultImageLoader());
        return config;
    }
}
