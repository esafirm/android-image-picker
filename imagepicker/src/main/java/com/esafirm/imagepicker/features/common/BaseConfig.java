package com.esafirm.imagepicker.features.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.esafirm.imagepicker.features.ImagePickerSavePath;

public class BaseConfig implements Parcelable {

    private ImagePickerSavePath savePath;
    private boolean returnAfterFirst;

    public static final Creator<BaseConfig> CREATOR = new Creator<BaseConfig>() {
        @Override
        public BaseConfig createFromParcel(Parcel in) {
            return new BaseConfig(in);
        }

        @Override
        public BaseConfig[] newArray(int size) {
            return new BaseConfig[size];
        }
    };

    public boolean isReturnAfterFirst() {
        return returnAfterFirst;
    }

    public ImagePickerSavePath getImageDirectory() {
        return savePath;
    }

    public void setSavePath(ImagePickerSavePath savePath) {
        this.savePath = savePath;
    }

    public void setImageDirectory(String dirName) {
        savePath = new ImagePickerSavePath(dirName, false);
    }

    public void setImageFullDirectory(String path) {
        savePath = new ImagePickerSavePath(path, true);
    }

    public void setReturnAfterFirst(boolean returnAfterFirst) {
        this.returnAfterFirst = returnAfterFirst;
    }

    public BaseConfig() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.savePath, flags);
        dest.writeByte(this.returnAfterFirst ? (byte) 1 : (byte) 0);
    }

    protected BaseConfig(Parcel in) {
        this.savePath = in.readParcelable(ImagePickerSavePath.class.getClassLoader());
        this.returnAfterFirst = in.readByte() != 0;
    }
}
