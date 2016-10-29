package com.esafirm.imagepicker.features;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.helper.Constants;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;

public class ImagePickerConfig implements Parcelable {

    private int mode;
    private int limit;
    private boolean showCamera;
    private String folderTitle;
    private String imageTitle;
    private ArrayList<Image> selectedImages;
    private boolean folderMode;
    private String imageDirectory;

    public ImagePickerConfig(Context context) {
        this.mode = ImagePicker.MODE_MULTIPLE;
        this.limit = Constants.MAX_LIMIT;
        this.showCamera = true;
        this.folderTitle = context.getString(R.string.title_folder);
        this.imageTitle = context.getString(R.string.title_select_image);
        this.selectedImages = new ArrayList<>();
        this.folderMode = false;
        this.imageDirectory = context.getString(R.string.image_directory);
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
        dest.writeInt(this.mode);
        dest.writeInt(this.limit);
        dest.writeByte(this.showCamera ? (byte) 1 : (byte) 0);
        dest.writeString(this.folderTitle);
        dest.writeString(this.imageTitle);
        dest.writeTypedList(this.selectedImages);
        dest.writeByte(this.folderMode ? (byte) 1 : (byte) 0);
        dest.writeString(this.imageDirectory);
    }

    protected ImagePickerConfig(Parcel in) {
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.showCamera = in.readByte() != 0;
        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.selectedImages = in.createTypedArrayList(Image.CREATOR);
        this.folderMode = in.readByte() != 0;
        this.imageDirectory = in.readString();
    }

    public static final Parcelable.Creator<ImagePickerConfig> CREATOR = new Parcelable.Creator<ImagePickerConfig>() {
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
