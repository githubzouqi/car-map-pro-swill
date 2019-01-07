package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-2 on 2018/1/23.
 */

public class RobotEntity implements Parcelable{

    private long robotID;// 小车id

    private long sectionID;

    private long podCodeInfoTheta;

    private long podCodeID;

    private long addressCodeID;// 小车在地图上的坐标

    private boolean carRouteIsShow;// 小车路径是否显示在地图上，false表示未显示，true表示已经显示

    public RobotEntity() {
    }

    public boolean isCarRouteIsShow() {
        return carRouteIsShow;
    }

    public void setCarRouteIsShow(boolean carRouteIsShow) {
        this.carRouteIsShow = carRouteIsShow;
    }

    public long getRobotID() {
        return robotID;
    }

    public void setRobotID(long robotID) {
        this.robotID = robotID;
    }

    public long getSectionID() {
        return sectionID;
    }

    public void setSectionID(long sectionID) {
        this.sectionID = sectionID;
    }

    public long getPodCodeInfoTheta() {
        return podCodeInfoTheta;
    }

    public void setPodCodeInfoTheta(long podCodeInfoTheta) {
        this.podCodeInfoTheta = podCodeInfoTheta;
    }

    public long getPodCodeID() {
        return podCodeID;
    }

    public void setPodCodeID(long podCodeID) {
        this.podCodeID = podCodeID;
    }

    public long getAddressCodeID() {
        return addressCodeID;
    }

    public void setAddressCodeID(long addressCodeID) {
        this.addressCodeID = addressCodeID;
    }

    @Override
    public String toString() {
        return "RobotEntity{" +
                "robotID=" + robotID +
                ", sectionID=" + sectionID +
                ", podCodeInfoTheta=" + podCodeInfoTheta +
                ", podCodeID=" + podCodeID +
                ", addressCodeID=" + addressCodeID +
                ", carRouteIsShow=" + carRouteIsShow +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.robotID);
        dest.writeLong(this.sectionID);
        dest.writeLong(this.podCodeInfoTheta);
        dest.writeLong(this.podCodeID);
        dest.writeLong(this.addressCodeID);
        dest.writeByte(this.carRouteIsShow ? (byte) 1 : (byte) 0);
    }

    protected RobotEntity(Parcel in) {
        this.robotID = in.readLong();
        this.sectionID = in.readLong();
        this.podCodeInfoTheta = in.readLong();
        this.podCodeID = in.readLong();
        this.addressCodeID = in.readLong();
        this.carRouteIsShow = in.readByte() != 0;
    }

    public static final Creator<RobotEntity> CREATOR = new Creator<RobotEntity>() {
        @Override
        public RobotEntity createFromParcel(Parcel source) {
            return new RobotEntity(source);
        }

        @Override
        public RobotEntity[] newArray(int size) {
            return new RobotEntity[size];
        }
    };

}
