package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zouqi on 2018/4/4.
 * 小车位置不改变超时
 */

public class NoMoveTimeoutEntity implements Parcelable {

    private int robotID;// 小车id
    private int port;// 端口
    private String ip;// ip地址
    private long currentAddress;

    public NoMoveTimeoutEntity() {

    }

    public int getRobotID() {
        return robotID;
    }

    public void setRobotID(int robotID) {
        this.robotID = robotID;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(long currentAddress) {
        this.currentAddress = currentAddress;
    }

    @Override
    public String toString() {
        return "NoMoveTimeoutEntity{" +
                "robotID=" + robotID +
                ", port=" + port +
                ", ip='" + ip + '\'' +
                ", currentAddress=" + currentAddress +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.robotID);
        dest.writeInt(this.port);
        dest.writeString(this.ip);
        dest.writeLong(this.currentAddress);
    }

    protected NoMoveTimeoutEntity(Parcel in) {
        this.robotID = in.readInt();
        this.port = in.readInt();
        this.ip = in.readString();
        this.currentAddress = in.readLong();
    }

    public static final Parcelable.Creator<NoMoveTimeoutEntity> CREATOR = new Parcelable.Creator<NoMoveTimeoutEntity>() {
        @Override
        public NoMoveTimeoutEntity createFromParcel(Parcel source) {
            return new NoMoveTimeoutEntity(source);
        }

        @Override
        public NoMoveTimeoutEntity[] newArray(int size) {
            return new NoMoveTimeoutEntity[size];
        }
    };
}
