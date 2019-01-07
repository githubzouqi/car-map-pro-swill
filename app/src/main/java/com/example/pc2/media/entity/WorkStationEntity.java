package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 工作站实体类
 * 可扩展，后面可以添加新的属性
 */
public class WorkStationEntity implements Parcelable {

    private int stopPos;// 该工作站对应的停止点

    private int angle;// 工作站朝向停止点的角度（0、90、180、270）

    public WorkStationEntity() {
    }

    public int getStopPos() {
        return stopPos;
    }

    public void setStopPos(int stopPos) {
        this.stopPos = stopPos;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "WorkStationEntity{" +
                "stopPos=" + stopPos +
                ", angle=" + angle +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stopPos);
        dest.writeInt(this.angle);
    }

    protected WorkStationEntity(Parcel in) {
        this.stopPos = in.readInt();
        this.angle = in.readInt();
    }

    public static final Parcelable.Creator<WorkStationEntity> CREATOR = new Parcelable.Creator<WorkStationEntity>() {
        @Override
        public WorkStationEntity createFromParcel(Parcel source) {
            return new WorkStationEntity(source);
        }

        @Override
        public WorkStationEntity[] newArray(int size) {
            return new WorkStationEntity[size];
        }
    };
}
