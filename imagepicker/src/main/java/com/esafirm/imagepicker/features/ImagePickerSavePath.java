package com.esafirm.imagepicker.features;

import android.os.Parcel;
import android.os.Parcelable;

public class ImagePickerSavePath implements Parcelable {

    public static final ImagePickerSavePath DEFAULT = new ImagePickerSavePath("Camera", false);

    private final String path;
    private final boolean isFullPath;

    public ImagePickerSavePath(String path, boolean isFullPath) {
        this.path = path;
        this.isFullPath = isFullPath;
    }

    public String getPath() {
        return path;
    }

    public boolean isFullPath() {
        return isFullPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeByte(this.isFullPath ? (byte) 1 : (byte) 0);
    }

    protected ImagePickerSavePath(Parcel in) {
        this.path = in.readString();
        this.isFullPath = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ImagePickerSavePath> CREATOR = new Parcelable.Creator<ImagePickerSavePath>() {
        @Override
        public ImagePickerSavePath createFromParcel(Parcel source) {
            return new ImagePickerSavePath(source);
        }

        @Override
        public ImagePickerSavePath[] newArray(int size) {
            return new ImagePickerSavePath[size];
        }
    };
}
