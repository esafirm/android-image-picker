package com.esafirm.imagepicker.features;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StyleRes;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;

public class ImagePickerConfig implements Parcelable {

    private ArrayList<Image> selectedImages;

    private String folderTitle;
    private String imageTitle;
    private String imageDirectory;

    private int mode;
    private int limit;
    private int theme;

    private boolean folderMode;
    private boolean showCamera;
    private boolean returnAfterFirst;

    private ImageLoader imageLoader;

    public ImagePickerConfig(Context context) {
        this.mode = ImagePicker.MODE_MULTIPLE;
        this.limit = ImagePicker.MAX_LIMIT;
        this.showCamera = true;
        this.folderTitle = context.getString(R.string.ef_title_folder);
        this.imageTitle = context.getString(R.string.ef_title_select_image);
        this.selectedImages = new ArrayList<>();
        this.folderMode = false;
        this.imageDirectory = context.getString(R.string.ef_image_directory);
        this.returnAfterFirst = true;
        this.imageLoader = new DefaultImageLoader();
    }

    public boolean isReturnAfterFirst() {
        return returnAfterFirst;
    }

    public void setReturnAfterFirst(boolean returnAfterFirst) {
        this.returnAfterFirst = returnAfterFirst;
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

    public boolean isFolderMode() {
        return folderMode;
    }

    public void setFolderMode(boolean folderMode) {
        this.folderMode = folderMode;
    }

    public String getImageDirectory() {
        return imageDirectory;
    }

    public void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }

    public void setTheme(@StyleRes int theme) {
        this.theme = theme;
    }

    public int getTheme() {
        return theme;
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
        dest.writeTypedList(this.selectedImages);
        dest.writeString(this.folderTitle);
        dest.writeString(this.imageTitle);
        dest.writeString(this.imageDirectory);
        dest.writeInt(this.mode);
        dest.writeInt(this.limit);
        dest.writeInt(this.theme);
        dest.writeByte(this.folderMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.returnAfterFirst ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.imageLoader);
    }

    protected ImagePickerConfig(Parcel in) {
        this.selectedImages = in.createTypedArrayList(Image.CREATOR);
        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.imageDirectory = in.readString();
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.theme = in.readInt();
        this.folderMode = in.readByte() != 0;
        this.showCamera = in.readByte() != 0;
        this.returnAfterFirst = in.readByte() != 0;
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
