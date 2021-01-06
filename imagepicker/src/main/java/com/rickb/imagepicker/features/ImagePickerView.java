package com.rickb.imagepicker.features;

import com.rickb.imagepicker.features.common.MvpView;
import com.rickb.imagepicker.model.Folder;
import com.rickb.imagepicker.model.Image;

import java.util.List;

public interface ImagePickerView extends MvpView {
    void showLoading(boolean isLoading);
    void showFetchCompleted(List<Image> images, List<Folder> folders);
    void showError(Throwable throwable);
    void showEmpty();
    void showCapturedImage();
    void finishPickImages(List<Image> images);
}
