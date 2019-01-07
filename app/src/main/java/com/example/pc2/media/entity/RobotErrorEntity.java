package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zouqi on 2018/4/4.
 * 小车错误反馈实体类（扫不到pod）
 */

public class RobotErrorEntity implements Parcelable {

    private int robotID;// 小车的id
    private long errorTime;// 错误反馈时间
    private String errorID;// 错误id
    private String errorStatus;// 错误状态
    private int podCodeID;// 将要扫的pod
    private int curPodID;// 当前扫到的pod
    private String sectionID;

    public RobotErrorEntity() {
    }

    public int getRobotID() {
        return robotID;
    }

    public void setRobotID(int robotID) {
        this.robotID = robotID;
    }

    public long getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(long errorTime) {
        this.errorTime = errorTime;
    }

    public String getErrorID() {
        return errorID;
    }

    public void setErrorID(String errorID) {
        this.errorID = errorID;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    public int getPodCodeID() {
        return podCodeID;
    }

    public void setPodCodeID(int podCodeID) {
        this.podCodeID = podCodeID;
    }

    public int getCurPodID() {
        return curPodID;
    }

    public void setCurPodID(int curPodID) {
        this.curPodID = curPodID;
    }

    public String getSectionID() {
        return sectionID;
    }

    public void setSectionID(String sectionID) {
        this.sectionID = sectionID;
    }

    @Override
    public String toString() {
        return "RobotErrorEntity{" +
                "robotID=" + robotID +
                ", errorTime=" + errorTime +
                ", errorID='" + errorID + '\'' +
                ", errorStatus='" + errorStatus + '\'' +
                ", podCodeID=" + podCodeID +
                ", curPodID=" + curPodID +
                ", sectionID='" + sectionID + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.robotID);
        dest.writeLong(this.errorTime);
        dest.writeString(this.errorID);
        dest.writeString(this.errorStatus);
        dest.writeInt(this.podCodeID);
        dest.writeString(this.sectionID);
        dest.writeInt(this.curPodID);
    }

    protected RobotErrorEntity(Parcel in) {
        this.robotID = in.readInt();
        this.errorTime = in.readLong();
        this.errorID = in.readString();
        this.errorStatus = in.readString();
        this.podCodeID = in.readInt();
        this.sectionID = in.readString();
        this.curPodID = in.readInt();
    }

    public static final Parcelable.Creator<RobotErrorEntity> CREATOR = new Parcelable.Creator<RobotErrorEntity>() {
        @Override
        public RobotErrorEntity createFromParcel(Parcel source) {
            return new RobotErrorEntity(source);
        }

        @Override
        public RobotErrorEntity[] newArray(int size) {
            return new RobotErrorEntity[size];
        }
    };
}
