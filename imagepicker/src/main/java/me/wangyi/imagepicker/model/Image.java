package me.wangyi.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 2017/5/8.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class Image implements Parcelable {
    private String path;
    private String name;
    private long dateTime;
    private String mimeType;
    private long size;
    private long id;
    private boolean selected;

    private Image(Parcel in) {
        path = in.readString();
        name = in.readString();
        dateTime = in.readLong();
        mimeType = in.readString();
        size = in.readLong();
        id = in.readLong();
        selected = in.readByte() != 0;
    }

    public Image() {
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    @SuppressWarnings("unused")
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Image)) {
            return false;
        }

        Image that = (Image) obj;
        //noinspection SimplifiableIfStatement
        if (this.path == null || that.getPath() == null) {
            return false;
        }

        return this.path.equals(that.getPath());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(dateTime);
        dest.writeString(mimeType);
        dest.writeLong(size);
        dest.writeLong(id);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
