package com.esafirm.imagepicker.features;

import android.content.Intent;

import com.esafirm.imagepicker.model.Image;

import java.util.List;

public interface ImagePickerInteractionListener {
    void setTitle(String title);
    void cancel();
    // Get this callback by calling an ImagePickerFragment's finishPickImages() method. It
    // removes Images whose files no longer exist.
    void finishPickImages(Intent result);
    // May include Images whose files no longer exist. This is called every time the selection
    // changes. Doing this is technically O(N^2), but since N is the number of times a user tapped
    // individual photos, it isn't going to get too slow.
    void selectionChanged(List<Image> imageList);
}
