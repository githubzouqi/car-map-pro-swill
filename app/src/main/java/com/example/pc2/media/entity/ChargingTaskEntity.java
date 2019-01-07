package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/2/5.
 * 充电桩的充电任务实体类，用来表示有充电任务小车和充电桩
 */

public class ChargingTaskEntity implements Parcelable {

    private String warehouseId;// 仓库id
    private String sectionUUID;// 仓库下所选地图的uuid
    private String orderId;// 任务id
    private String tripState;// 任务状态
    private String driveId;// 要来充电桩充电的小车id
    private String chargeUUID;// 充电桩的uuid
    private String robotAddressCodeId;// 小车不发送实时包时，用来标识小车在地图上的位置（此时小车在充电桩处充电）

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSectionUUID() {
        return sectionUUID;
    }

    public void setSectionUUID(String sectionUUID) {
        this.sectionUUID = sectionUUID;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTripState() {
        return tripState;
    }

    public void setTripState(String tripState) {
        this.tripState = tripState;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getChargeUUID() {
        return chargeUUID;
    }

    public void setChargeUUID(String chargeUUID) {
        this.chargeUUID = chargeUUID;
    }

    public String getRobotAddressCodeId() {
        return robotAddressCodeId;
    }

    public void setRobotAddressCodeId(String robotAddressCodeId) {
        this.robotAddressCodeId = robotAddressCodeId;
    }

    @Override
    public String toString() {
        return "ChargingTaskEntity{" +
                "warehouseId='" + warehouseId + '\'' +
                ", sectionUUID='" + sectionUUID + '\'' +
                ", orderId='" + orderId + '\'' +
                ", tripState='" + tripState + '\'' +
                ", driveId='" + driveId + '\'' +
                ", chargeUUID='" + chargeUUID + '\'' +
                ", robotAddressCodeId='" + robotAddressCodeId + '\'' +
                '}';
    }

    public ChargingTaskEntity() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.warehouseId);
        dest.writeString(this.sectionUUID);
        dest.writeString(this.orderId);
        dest.writeString(this.tripState);
        dest.writeString(this.driveId);
        dest.writeString(this.chargeUUID);
        dest.writeString(this.robotAddressCodeId);
    }

    protected ChargingTaskEntity(Parcel in) {
        this.warehouseId = in.readString();
        this.sectionUUID = in.readString();
        this.orderId = in.readString();
        this.tripState = in.readString();
        this.driveId = in.readString();
        this.chargeUUID = in.readString();
        this.robotAddressCodeId = in.readString();
    }

    public static final Creator<ChargingTaskEntity> CREATOR = new Creator<ChargingTaskEntity>() {
        @Override
        public ChargingTaskEntity createFromParcel(Parcel source) {
            return new ChargingTaskEntity(source);
        }

        @Override
        public ChargingTaskEntity[] newArray(int size) {
            return new ChargingTaskEntity[size];
        }
    };
}
