package com.esafirm.imagepicker.features;

import android.content.Context;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader;

import java.util.ArrayList;

public class ImagePickerConfigFactory {

    public static ImagePickerConfig createDefault(Context context) {
        ImagePickerConfig config = new ImagePickerConfig();
        config.setMode(ImagePicker.MODE_MULTIPLE);
        config.setLimit(ImagePicker.MAX_LIMIT);
        config.setShowCamera(true);
        config.setFolderMode(false);
        config.setFolderTitle(context.getString(R.string.ef_title_folder));
        config.setImageTitle(context.getString(R.string.ef_title_select_image));
        config.setSelectedImages(new ArrayList<>());
        config.setSavePath(ImagePickerSavePath.DEFAULT);
        config.setReturnAfterFirst(true);
        config.setImageLoader(new DefaultImageLoader());
        return config;
    }
}
