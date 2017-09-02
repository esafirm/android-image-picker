package com.esafirm.imagepicker.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public abstract class ImagePicker {

    public static final String EXTRA_SELECTED_IMAGES = "selectedImages";

    public static final int MAX_LIMIT = 999;

    public static final int MODE_SINGLE = 1;
    public static final int MODE_MULTIPLE = 2;

    private ImagePickerConfig config;

    public abstract void start(int requestCode);

    public static class ImagePickerWithActivity extends ImagePicker {

        private Activity activity;

        public ImagePickerWithActivity(Activity activity) {
            this.activity = activity;
            init();
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(activity);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static class ImagePickerWithFragment extends ImagePicker {

        private Fragment fragment;

        public ImagePickerWithFragment(Fragment fragment) {
            this.fragment = fragment;
            init();
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(fragment.getActivity());
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    /* --------------------------------------------------- */
    /* > Stater */
    /* --------------------------------------------------- */

    public void init() {
        config = ImagePickerConfigFactory.createDefault();
    }

    public static ImagePickerWithActivity create(Activity activity) {
        return new ImagePickerWithActivity(activity);
    }

    public static ImagePickerWithFragment create(Fragment fragment) {
        return new ImagePickerWithFragment(fragment);
    }

    /* --------------------------------------------------- */
    /* > Builder */
    /* --------------------------------------------------- */

    public ImagePicker single() {
        config.setMode(ImagePicker.MODE_SINGLE);
        return this;
    }

    public ImagePicker multi() {
        config.setMode(ImagePicker.MODE_MULTIPLE);
        return this;
    }

    public ImagePicker returnAfterFirst(boolean returnAfterFirst) {
        config.setReturnAfterFirst(returnAfterFirst);
        return this;
    }

    public ImagePicker limit(int count) {
        config.setLimit(count);
        return this;
    }

    public ImagePicker showCamera(boolean show) {
        config.setShowCamera(show);
        return this;
    }

    public ImagePicker folderTitle(String title) {
        config.setFolderTitle(title);
        return this;
    }

    public ImagePicker imageTitle(String title) {
        config.setImageTitle(title);
        return this;
    }

    public ImagePicker origin(ArrayList<Image> images) {
        config.setSelectedImages(images);
        return this;
    }

    public ImagePicker folderMode(boolean folderMode) {
        config.setFolderMode(folderMode);
        return this;
    }

    public ImagePicker imageDirectory(String directory) {
        config.setImageDirectory(directory);
        return this;
    }

    public ImagePicker imageFullDirectory(String fullPath) {
        config.setImageFullDirectory(fullPath);
        return this;
    }

    public ImagePicker theme(@StyleRes int theme) {
        config.setTheme(theme);
        return this;
    }

    public ImagePicker imageLoader(ImageLoader imageLoader) {
        config.setImageLoader(imageLoader);
        return this;
    }

    protected ImagePickerConfig getConfig() {
        return config;
    }

    public ImagePicker enableLog(boolean isEnable) {
        IpLogger.getInstance().setEnable(isEnable);
        return this;
    }

    public Intent getIntent(Context context) {
        ImagePickerConfig config = ConfigUtils.checkConfig(getConfig());
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(ImagePickerConfig.class.getSimpleName(), config);
        return intent;
    }

    /* --------------------------------------------------- */
    /* > Helper */
    /* --------------------------------------------------- */

    public static List<Image> getImages(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getParcelableArrayListExtra(ImagePicker.EXTRA_SELECTED_IMAGES);
    }
}
