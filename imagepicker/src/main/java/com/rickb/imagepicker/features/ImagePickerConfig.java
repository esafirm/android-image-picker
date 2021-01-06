package com.rickb.imagepicker.features;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StyleRes;

import com.rickb.imagepicker.features.common.BaseConfig;
import com.rickb.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;

public class ImagePickerConfig extends BaseConfig implements Parcelable {

    public static final int NO_COLOR = -1;

    private ArrayList<Image> selectedImages;
    private ArrayList<File> excludedImages;

    private String folderTitle;
    private String imageTitle;
    private String doneButtonText;
    private int arrowColor = NO_COLOR;

    private int mode;
    private int limit;
    private int theme;

    private boolean folderMode;
    private boolean includeVideo;
    private boolean onlyVideo;
    private boolean includeAnimation;
    private boolean showCamera;

    private transient String language;

    private Boolean shouldShowSelectionLimitBottomView;

    private Double totalSizeLimit;

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

    public boolean isIncludeVideo() {
        return includeVideo;
    }

    public void setIncludeVideo(boolean includeVideo) {
        this.includeVideo = includeVideo;
    }

    public boolean isOnlyVideo() {
        return onlyVideo;
    }

    public void setOnlyVideo(boolean onlyVideo) {
        this.onlyVideo = onlyVideo;
    }

    public boolean isIncludeAnimation() {
        return includeAnimation;
    }

    public void setIncludeAnimation(boolean includeAnimation) {
        this.includeAnimation = includeAnimation;
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

    public String getDoneButtonText() {
        return doneButtonText;
    }

    public void setDoneButtonText(String doneButtonText) {
        this.doneButtonText = doneButtonText;
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

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void showSelectionLimitBottomView(boolean shouldShow) {
        this.shouldShowSelectionLimitBottomView = shouldShow;
    }

    public boolean shouldShowSelectionLimitBottomView() {
        if (this.shouldShowSelectionLimitBottomView == null) return false;
        return this.shouldShowSelectionLimitBottomView;
    }

    public void totalSizeLimit(Double shouldShow) {
        this.totalSizeLimit = shouldShow;
    }

    public Double totalSizeLimit() {
        return this.totalSizeLimit;
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

        dest.writeByte((byte) (excludedImages != null ? 1 : 0));
        if (excludedImages != null) {
            dest.writeList(this.excludedImages);
        }

        dest.writeString(this.folderTitle);
        dest.writeString(this.imageTitle);
        dest.writeString(this.doneButtonText);
        dest.writeInt(this.arrowColor);
        dest.writeInt(this.mode);
        dest.writeInt(this.limit);
        dest.writeInt(this.theme);
        dest.writeDouble(this.totalSizeLimit);
        dest.writeByte(this.folderMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.includeVideo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.onlyVideo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.includeAnimation ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shouldShowSelectionLimitBottomView ? (byte) 1 : (byte) 0);
    }

    protected ImagePickerConfig(Parcel in) {
        super(in);
        this.selectedImages = in.createTypedArrayList(Image.CREATOR);

        boolean isPresent = in.readByte() != 0;
        if (isPresent) {
            this.excludedImages = new ArrayList<>();
            in.readList(this.excludedImages, File.class.getClassLoader());
        }

        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.doneButtonText = in.readString();
        this.arrowColor = in.readInt();
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.theme = in.readInt();
        this.totalSizeLimit = in.readDouble();
        this.folderMode = in.readByte() != 0;
        this.includeVideo = in.readByte() != 0;
        this.onlyVideo = in.readByte() != 0;
        this.includeAnimation = in.readByte() != 0;
        this.showCamera = in.readByte() != 0;
        this.shouldShowSelectionLimitBottomView = in.readByte() != 0;
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
