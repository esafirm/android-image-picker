package com.esafirm.imagepicker.features;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.features.common.BasePresenter;
import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.features.fileloader.DefaultImageFileLoader;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    private DefaultImageFileLoader imageLoader;
    private DefaultCameraModule cameraModule;
    private Handler main = new Handler(Looper.getMainLooper());

    ImagePickerPresenter(DefaultImageFileLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    DefaultCameraModule getCameraModule() {
        if (cameraModule == null) {
            cameraModule = new DefaultCameraModule();
        }
        return cameraModule;
    }

    /* Set the camera module in onRestoreInstance */
    void setCameraModule(DefaultCameraModule cameraModule) {
        this.cameraModule = cameraModule;
    }

    void abortLoad() {
        imageLoader.abortLoadImages();
    }

    void loadImages(ImagePickerConfig config) {
        if (!isViewAttached()) return;

        boolean isFolder = config.isFolderMode();
        boolean includeVideo = config.isIncludeVideo();
        boolean onlyVideo = config.isOnlyVideo();
        boolean includeAnimation = config.isIncludeAnimation();
        ArrayList<File> excludedImages = config.getExcludedImages();

        runOnUiIfAvailable(() -> getView().showLoading(true));

        imageLoader.loadDeviceImages(isFolder, onlyVideo, includeVideo, includeAnimation, excludedImages, new ImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                runOnUiIfAvailable(() -> {
                    getView().showFetchCompleted(images, folders);

                    final boolean isEmpty = folders != null
                            ? folders.isEmpty()
                            : images.isEmpty();

                    if (isEmpty) {
                        getView().showEmpty();
                    } else {
                        getView().showLoading(false);
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                runOnUiIfAvailable(() -> getView().showError(throwable));
            }
        });
    }

    void onDoneSelectImages(List<Image> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {

            /* Scan selected images which not existed */
            for (int i = 0; i < selectedImages.size(); i++) {
                Image image = selectedImages.get(i);
                File file = new File(image.getPath());
                if (!file.exists()) {
                    selectedImages.remove(i);
                    i--;
                }
            }
            getView().finishPickImages(selectedImages);
        }
    }

    void captureImage(Fragment fragment, BaseConfig config, int requestCode) {
        Context context = fragment.getActivity().getApplicationContext();
        Intent intent = getCameraModule().getCameraIntent(fragment.getActivity(), config);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show();
            return;
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    void finishCaptureImage(Context context, Intent data, final BaseConfig config) {
        getCameraModule().getImage(context, data, images -> {
            if (ConfigUtils.shouldReturn(config, true)) {
                getView().finishPickImages(images);
            } else {
                getView().showCapturedImage();
            }
        });
    }

    void abortCaptureImage(Context context) {
        getCameraModule().removeImage(context);
    }

    private void runOnUiIfAvailable(Runnable runnable) {
        main.post(() -> {
            if (isViewAttached()) {
                runnable.run();
            }
        });
    }
}
