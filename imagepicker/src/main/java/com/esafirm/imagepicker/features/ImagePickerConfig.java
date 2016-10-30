package com.esafirm.imagepicker.features;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;

public class ImagePickerConfig implements Parcelable {

    private ArrayList<Image> selectedImages;

    private String folderTitle;
    private String imageTitle;
    private String imageDirectory;

    private int mode;
    private int limit;

    private boolean folderMode;
    private boolean showCamera;
    private boolean returnAfterCapture;

    public ImagePickerConfig(Context context) {
        this.mode = ImagePicker.MODE_MULTIPLE;
        this.limit = ImagePicker.MAX_LIMIT;
        this.showCamera = true;
        this.folderTitle = context.getString(R.string.title_folder);
        this.imageTitle = context.getString(R.string.title_select_image);
        this.selectedImages = new ArrayList<>();
        this.folderMode = false;
        this.imageDirectory = context.getString(R.string.image_directory);
        this.returnAfterCapture = true;
    }

    public boolean isReturnAfterCapture() {
        return returnAfterCapture;
    }

    public void setReturnAfterCapture(boolean returnAfterCapture) {
        this.returnAfterCapture = returnAfterCapture;
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
        dest.writeByte(this.folderMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.returnAfterCapture ? (byte) 1 : (byte) 0);
    }

    protected ImagePickerConfig(Parcel in) {
        this.selectedImages = in.createTypedArrayList(Image.CREATOR);
        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.imageDirectory = in.readString();
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.folderMode = in.readByte() != 0;
        this.showCamera = in.readByte() != 0;
        this.returnAfterCapture = in.readByte() != 0;
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
