package com.esafirm.imagepicker.features;

import com.esafirm.imagepicker.features.common.MvpView;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.util.List;

public interface ImagePickerView extends MvpView {
    void showLoading(boolean isLoading);
    void showFetchCompleted(List<Image> images, List<Folder> folders);
    void showError(Throwable throwable);
    void showEmpty();
    void showCapturedImage();
    void finishPickImages(List<Image> images);
}
