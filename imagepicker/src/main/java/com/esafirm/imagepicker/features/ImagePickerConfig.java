package com.esafirm.imagepicker.features;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StyleRes;

import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;

public class ImagePickerConfig extends BaseConfig implements Parcelable {

    public static final int NO_COLOR = -1;

    private ArrayList<Image> selectedImages;
    private ArrayList<File> excludedImages;

    private String folderTitle;
    private String imageTitle;
    private int arrowColor = NO_COLOR;


    private int mode;
    private int limit;
    private int theme;

    private boolean folderMode;
    private boolean showCamera;

    private ImageLoader imageLoader;

    public ImagePickerConfig() {
    }

    public int getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(int arrowColor) {
        this.arrowColor = arrowColor;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public String getFolderTitle() {
        return folderTitle;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public ArrayList<Image> getSelectedImages() {
        return selectedImages;
    }

    public void setSelectedImages(ArrayList<Image> selectedImages) {
        this.selectedImages = selectedImages;
    }

    public ArrayList<File> getExcludedImages() {
        return excludedImages;
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

    public boolean isFolderMode() {
        return folderMode;
    }

    public void setFolderMode(boolean folderMode) {
        this.folderMode = folderMode;
    }

    public void setTheme(@StyleRes int theme) {
        this.theme = theme;
    }

    public int getTheme() {
        return theme;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    /* --------------------------------------------------- */
    /* > Parcelable */
    /* --------------------------------------------------- */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(this.selectedImages);
        dest.writeList(this.excludedImages);
        dest.writeString(this.folderTitle);
        dest.writeString(this.imageTitle);
        dest.writeInt(this.arrowColor);
        dest.writeInt(this.mode);
        dest.writeInt(this.limit);
        dest.writeInt(this.theme);
        dest.writeByte(this.folderMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCamera ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.imageLoader);
    }

    protected ImagePickerConfig(Parcel in) {
        super(in);
        this.selectedImages = in.createTypedArrayList(Image.CREATOR);
        this.excludedImages = new ArrayList<File>();
        in.readList(this.excludedImages, File.class.getClassLoader());
        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.arrowColor = in.readInt();
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.theme = in.readInt();
        this.folderMode = in.readByte() != 0;
        this.showCamera = in.readByte() != 0;
        this.imageLoader = (ImageLoader) in.readSerializable();
    }

    public static final Creator<ImagePickerConfig> CREATOR = new Creator<ImagePickerConfig>() {
        @Override
        public ImagePickerConfig createFromParcel(Parcel source) {
            return new ImagePickerConfig(source);
        }

        @Override
        public ImagePickerConfig[] newArray(int size) {
            return new ImagePickerConfig[size];
        }
    };
}
