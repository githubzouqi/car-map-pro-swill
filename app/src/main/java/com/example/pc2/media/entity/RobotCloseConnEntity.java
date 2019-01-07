package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/4/5.
 * 小车断开连接
 */

public class RobotCloseConnEntity implements Parcelable {

    private int robotID;// 小车的id
    private String ip;// ip地址
    private int port;// 端口
    private long time;// 小车断开连接故障时间

    public RobotCloseConnEntity() {
    }

    public int getRobotID() {
        return robotID;
    }

    public void setRobotID(int robotID) {
        this.robotID = robotID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "RobotCloseConnEntity{" +
                "robotID=" + robotID +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", time=" + time +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.robotID);
        dest.writeString(this.ip);
        dest.writeInt(this.port);
        dest.writeLong(this.time);
    }

    protected RobotCloseConnEntity(Parcel in) {
        this.robotID = in.readInt();
        this.ip = in.readString();
        this.port = in.readInt();
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<RobotCloseConnEntity> CREATOR = new Parcelable.Creator<RobotCloseConnEntity>() {
        @Override
        public RobotCloseConnEntity createFromParcel(Parcel source) {
            return new RobotCloseConnEntity(source);
        }

        @Override
        public RobotCloseConnEntity[] newArray(int size) {
            return new RobotCloseConnEntity[size];
        }
    };
}
