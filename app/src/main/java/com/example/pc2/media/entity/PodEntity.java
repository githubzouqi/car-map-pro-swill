package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-2 on 2018/1/23.
 * pod实体类
 */

public class PodEntity implements Parcelable{

    private int podId;// pod的id

    private int podPos;// pod的地图上的坐标

    private int podAngle;// pod面的朝向 0°朝上、90°朝右、180°朝下、270°朝左

    private boolean isStoragePod;// true表示是存储区的货架，false表示不是存储区的货架

    public PodEntity() {
    }

    public int getPodAngle() {
        return podAngle;
    }

    public void setPodAngle(int podAngle) {
        this.podAngle = podAngle;
    }

    public int getPodId() {
        return podId;
    }

    public void setPodId(int podId) {
        this.podId = podId;
    }

    public int getPodPos() {
        return podPos;
    }

    public void setPodPos(int podPos) {
        this.podPos = podPos;
    }

    public boolean isStoragePod() {
        return isStoragePod;
    }

    public void setStoragePod(boolean storagePod) {
        isStoragePod = storagePod;
    }

    @Override
    public String toString() {
        return "PodEntity{" +
                "podId=" + podId +
                ", podPos=" + podPos +
                ", podAngle=" + podAngle +
                ", isStoragePod=" + isStoragePod +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.podId);
        dest.writeInt(this.podPos);
        dest.writeInt(this.podAngle);
        dest.writeByte(this.isStoragePod ? (byte) 1 : (byte) 0);
    }

    protected PodEntity(Parcel in) {
        this.podId = in.readInt();
        this.podPos = in.readInt();
        this.podAngle = in.readInt();
        this.isStoragePod = in.readByte() != 0;
    }

    public static final Creator<PodEntity> CREATOR = new Creator<PodEntity>() {
        @Override
        public PodEntity createFromParcel(Parcel source) {
            return new PodEntity(source);
        }

        @Override
        public PodEntity[] newArray(int size) {
            return new PodEntity[size];
        }
    };
}
