package com.esafirm.imagepicker.features.cameraonly;

import android.os.Parcel;

import com.esafirm.imagepicker.features.common.BaseConfig;

public class CameraOnlyConfig extends BaseConfig {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public CameraOnlyConfig() {
    }

    private CameraOnlyConfig(Parcel in) {
        super(in);
    }

    public static final Creator<CameraOnlyConfig> CREATOR = new Creator<CameraOnlyConfig>() {
        @Override
        public CameraOnlyConfig createFromParcel(Parcel source) {
            return new CameraOnlyConfig(source);
        }

        @Override
        public CameraOnlyConfig[] newArray(int size) {
            return new CameraOnlyConfig[size];
        }
    };
}
