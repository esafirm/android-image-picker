package com.esafirm.imagepicker.helper;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ImagePickerConfig;

public class ConfigUtils {
    public static boolean isReturnAfterFirst(ImagePickerConfig config) {
        return config.getMode() == ImagePicker.MODE_SINGLE && config.isReturnAfterFirst();
    }
}
