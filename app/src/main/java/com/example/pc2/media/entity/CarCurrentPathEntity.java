package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/5.
 * 小车当前路径实体类。用来表示小车的锁格和未锁格区域
 */

public class CarCurrentPathEntity implements Parcelable {

    private long robotID;// 小车的id
    private List<Long> lockPath;// 锁格路径区域
    private List<Long> allPath;// 锁格和未锁格全路径区域

    public CarCurrentPathEntity() {
    }

    public long getRobotID() {
        return robotID;
    }

    public void setRobotID(long robotID) {
        this.robotID = robotID;
    }

    public List<Long> getLockPath() {
        return lockPath;
    }

    public void setLockPath(List<Long> lockPath) {
        this.lockPath = lockPath;
    }

    public List<Long> getAllPath() {
        return allPath;
    }

    public void setAllPath(List<Long> allPath) {
        this.allPath = allPath;
    }

    @Override
    public String toString() {
        return "CarCurrentPathEntity{" +
                "robotID=" + robotID +
                ", lockPath=" + lockPath +
                ", unlockPath=" + allPath +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.robotID);
        dest.writeList(this.lockPath);
        dest.writeList(this.allPath);
    }

    protected CarCurrentPathEntity(Parcel in) {
        this.robotID = in.readLong();
        this.lockPath = new ArrayList<Long>();
        in.readList(this.lockPath, Long.class.getClassLoader());
        this.allPath = new ArrayList<Long>();
        in.readList(this.allPath, Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<CarCurrentPathEntity> CREATOR = new Parcelable.Creator<CarCurrentPathEntity>() {
        @Override
        public CarCurrentPathEntity createFromParcel(Parcel source) {
            return new CarCurrentPathEntity(source);
        }

        @Override
        public CarCurrentPathEntity[] newArray(int size) {
            return new CarCurrentPathEntity[size];
        }
    };
}
