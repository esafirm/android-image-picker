package com.esafirm.imagepicker.helper;

import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;

/**
 * Code written by Qandeel Abbassi on 9/20/2018 at 7:03 PM.
 */
public class ExcludedMediaSingleton {
    private static ExcludedMediaSingleton ourInstance;
    private ArrayList<File> excludedImages;

    private ExcludedMediaSingleton() {
    }

    public static ExcludedMediaSingleton getInstance() {
        if (ourInstance == null) { //if there is no instance available... create new one
            ourInstance = new ExcludedMediaSingleton();
        }
        return ourInstance;
    }

    public void resetExclusions() {
        if (excludedImages != null) {
            excludedImages.clear();
        }
        excludedImages = null;
    }

    public void setExcludedImages(ArrayList<Image> excludedImages) {
        if (excludedImages != null && !excludedImages.isEmpty()) {
            this.excludedImages = new ArrayList<>();
            for (Image image : excludedImages) {
                this.excludedImages.add(new File(image.getPath()));
            }
        } else {
            this.excludedImages = null;
        }
    }

    public void setExcludedImageFiles(ArrayList<File> excludedImages) {
        this.excludedImages = excludedImages;
    }

    public ArrayList<File> getExcludedImages() {
        return excludedImages;
    }
}
