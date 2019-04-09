package com.example.pc2.swill.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 查看所有充电桩状态返回的数据实体
 */
public class ChargerStatusEntity implements Parcelable {

    /**
     {
     chargers=[
     {errorName=正常运行, errorIndex=0, type=1, id=2, statusName=充电中, statusIndex=5},
     {errorName=正常运行, errorIndex=0, type=1, id=4, statusName=闲置状态, statusIndex=2}
     ]
     }
     */

    private String errorName;// 充电桩运行情况

    private int errorIndex;// 充电桩运行情况码

    private int type;// 充电桩类型

    private int id;// 充电桩id

    private String statusName;// 充电桩工作状态

    private int statusIndex;// 充电桩状态码

    public ChargerStatusEntity() {

    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public int getErrorIndex() {
        return errorIndex;
    }

    public void setErrorIndex(int errorIndex) {
        this.errorIndex = errorIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    public void setStatusIndex(int statusIndex) {
        this.statusIndex = statusIndex;
    }

    @Override
    public String toString() {
        return "ChargerStatusEntity{" +
                "errorName='" + errorName + '\'' +
                ", errorIndex=" + errorIndex +
                ", type=" + type +
                ", id=" + id +
                ", statusName='" + statusName + '\'' +
                ", statusIndex=" + statusIndex +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.errorName);
        dest.writeInt(this.errorIndex);
        dest.writeInt(this.type);
        dest.writeInt(this.id);
        dest.writeString(this.statusName);
        dest.writeInt(this.statusIndex);
    }

    protected ChargerStatusEntity(Parcel in) {
        this.errorName = in.readString();
        this.errorIndex = in.readInt();
        this.type = in.readInt();
        this.id = in.readInt();
        this.statusName = in.readString();
        this.statusIndex = in.readInt();
    }

    public static final Parcelable.Creator<ChargerStatusEntity> CREATOR = new Parcelable.Creator<ChargerStatusEntity>() {
        @Override
        public ChargerStatusEntity createFromParcel(Parcel source) {
            return new ChargerStatusEntity(source);
        }

        @Override
        public ChargerStatusEntity[] newArray(int size) {
            return new ChargerStatusEntity[size];
        }
    };
}
