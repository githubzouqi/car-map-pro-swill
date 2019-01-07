package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/2/27.
 * 小车电量信息实体类
 */

public class CarBatteryInfoEntity implements Parcelable {

    private long robotID;// 小车的id
    private long laveBattery;// 小车的剩余电量（千分比数值：0.2表示200，该值为200）
    private long voltage;// 小车的电池电压，单位mv

    public CarBatteryInfoEntity() {
    }

    public long getRobotID() {
        return robotID;
    }

    public void setRobotID(long robotID) {
        this.robotID = robotID;
    }

    public long getLaveBattery() {
        return laveBattery;
    }

    public void setLaveBattery(long laveBattery) {
        this.laveBattery = laveBattery;
    }

    public long getVoltage() {
        return voltage;
    }

    public void setVoltage(long voltage) {
        this.voltage = voltage;
    }

    @Override
    public String toString() {
        return "CarBatteryInfoEntity{" +
                "robotID=" + robotID +
                ", laveBattery=" + laveBattery +
                ", voltage=" + voltage +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.robotID);
        dest.writeLong(this.laveBattery);
        dest.writeLong(this.voltage);
    }

    protected CarBatteryInfoEntity(Parcel in) {
        this.robotID = in.readLong();
        this.laveBattery = in.readLong();
        this.voltage = in.readLong();
    }

    public static final Creator<CarBatteryInfoEntity> CREATOR = new Creator<CarBatteryInfoEntity>() {
        @Override
        public CarBatteryInfoEntity createFromParcel(Parcel source) {
            return new CarBatteryInfoEntity(source);
        }

        @Override
        public CarBatteryInfoEntity[] newArray(int size) {
            return new CarBatteryInfoEntity[size];
        }
    };
}
