package com.esafirm.imagepicker.features;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.esafirm.imagepicker.features.common.BasePresenter;
import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    private ImageLoader imageLoader;
    private Handler handler = new Handler(Looper.getMainLooper());

    public ImagePickerPresenter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void abortLoad() {
        imageLoader.abortLoadImages();
    }

    public void loadImages(boolean isFolderMode) {
        if (!isViewAttached()) return;

        getView().showLoading(true);
        imageLoader.loadDeviceImages(isFolderMode, new ImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showFetchCompleted(images, folders);

                            if (folders != null) {
                                if (folders.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            } else {
                                if (images.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showError(throwable);
                        }
                    }
                });
            }
        });
    }

    public void onDoneSelectImages(List<Image> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {

            /** Scan selected images which not existed */
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

    public void finishCaptureImage(Context context, String imagePath, final ImagePickerConfig config) {
        Uri imageUri = Uri.parse(imagePath);
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v("ImagePicker", "File " + path + " was scanned successfully: " + uri);

                            if (config.isReturnAfterCapture()) {
                                List<Image> images = new ArrayList<>();
                                images.add(new Image(0, ImagePickerUtils.getNameFromFilePath(path), path, true));
                                getView().finishPickImages(images);
                            } else {
                                getView().showCapturedImage();
                            }
                        }
                    });
        }

    }
}
