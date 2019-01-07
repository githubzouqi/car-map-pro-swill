package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zouqi on 2018/2/2.
 * 充电桩实体类，包含充电桩的一些信息
 * 该实体类用来标识充电桩在地图上的位置
 */

public class ChargingPileEntity implements Parcelable {

    int chargerType;// 类型
    int toward;// 充电桩的朝向（0：朝上、90：朝右、180：朝下、270：朝左）
    int chargerID;// 充电桩的id
    String UUID;// 充电桩的uuid
    String addrCodeID;// 充电桩朝向小车小车所在的位置，可用来确定充电桩的位置

    public ChargingPileEntity() {
    }

    public int getChargerType() {
        return chargerType;
    }

    public void setChargerType(int chargerType) {
        this.chargerType = chargerType;
    }

    public int getToward() {
        return toward;
    }

    public void setToward(int toward) {
        this.toward = toward;
    }

    public int getChargerID() {
        return chargerID;
    }

    public void setChargerID(int chargerID) {
        this.chargerID = chargerID;
    }

    public String getAddrCodeID() {
        return addrCodeID;
    }

    public void setAddrCodeID(String addrCodeID) {
        this.addrCodeID = addrCodeID;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public String toString() {
        return "ChargingPileEntity{" +
                "chargerType=" + chargerType +
                ", toward=" + toward +
                ", chargerID=" + chargerID +
                ", UUID='" + UUID + '\'' +
                ", addrCodeID='" + addrCodeID + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.chargerType);
        dest.writeInt(this.toward);
        dest.writeInt(this.chargerID);
        dest.writeString(this.UUID);
        dest.writeString(this.addrCodeID);
    }

    protected ChargingPileEntity(Parcel in) {
        this.chargerType = in.readInt();
        this.toward = in.readInt();
        this.chargerID = in.readInt();
        this.UUID = in.readString();
        this.addrCodeID = in.readString();
    }

    public static final Creator<ChargingPileEntity> CREATOR = new Creator<ChargingPileEntity>() {
        @Override
        public ChargingPileEntity createFromParcel(Parcel source) {
            return new ChargingPileEntity(source);
        }

        @Override
        public ChargingPileEntity[] newArray(int size) {
            return new ChargingPileEntity[size];
        }
    };
}
