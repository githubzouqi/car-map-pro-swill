package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 充电桩故障实体类
 */
public class ErrorCharging implements Parcelable {

    private int number;// 充电桩的id
    private int statusIndex;// 充电桩的故障id
    private String statusName;// 充电桩的故障名称
    private String type;// 充电桩类型
    private long time;// 时间

    public ErrorCharging() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    public void setStatusIndex(int statusIndex) {
        this.statusIndex = statusIndex;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ErrorCharging{" +
                "number=" + number +
                ", statusIndex=" + statusIndex +
                ", statusName='" + statusName + '\'' +
                ", type='" + type + '\'' +
                ", time=" + time +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.number);
        dest.writeInt(this.statusIndex);
        dest.writeString(this.statusName);
        dest.writeString(this.type);
        dest.writeLong(this.time);
    }

    protected ErrorCharging(Parcel in) {
        this.number = in.readInt();
        this.statusIndex = in.readInt();
        this.statusName = in.readString();
        this.type = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<ErrorCharging> CREATOR = new Creator<ErrorCharging>() {
        @Override
        public ErrorCharging createFromParcel(Parcel source) {
            return new ErrorCharging(source);
        }

        @Override
        public ErrorCharging[] newArray(int size) {
            return new ErrorCharging[size];
        }
    };
}
