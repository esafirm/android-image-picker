package com.esafirm.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    private long id;
    private String name;
    private String path;

    public Image(long id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;
        return image.getPath().equalsIgnoreCase(getPath());
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
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.path);
    }

    protected Image(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.path = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
