package com.esafirm.imagepicker.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public abstract class ImagePicker {

    public static final String EXTRA_SELECTED_IMAGES = "selectedImages";
    public static final String EXTRA_LIMIT = "limit";
    public static final String EXTRA_SHOW_CAMERA = "showCamera";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_FOLDER_MODE = "folderMode";
    public static final String EXTRA_FOLDER_TITLE = "folderTitle";
    public static final String EXTRA_IMAGE_TITLE = "imageTitle";
    public static final String EXTRA_IMAGE_DIRECTORY = "imageDirectory";

    public static final int MODE_SINGLE = 1;
    public static final int MODE_MULTIPLE = 2;

    private ImagePickerConfig config;

    public abstract void start(int requestCode);

    public static class ImagePickerWithActivity extends ImagePicker {

        private Activity activity;

        public ImagePickerWithActivity(Activity activity) {
            this.activity = activity;
            init(activity);
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
            init(fragment.getActivity());
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(fragment.getActivity());
            fragment.startActivityForResult(intent, requestCode);
        }
    }


    public void init(Context context) {
        config = new ImagePickerConfig(context);
    }


    public static ImagePickerWithActivity create(Activity activity) {
        return new ImagePickerWithActivity(activity);
    }

    public static ImagePickerWithFragment create(Fragment fragment) {
        return new ImagePickerWithFragment(fragment);
    }

    public ImagePicker single() {
        config.setMode(ImagePicker.MODE_SINGLE);
        return this;
    }

    public ImagePicker multi() {
        config.setMode(ImagePicker.MODE_MULTIPLE);
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

    public Intent getIntent(Context context) {
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
