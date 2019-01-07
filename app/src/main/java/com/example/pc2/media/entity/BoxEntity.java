package com.example.pc2.media.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by PC-2 on 2018/1/23.
 */

public class BoxEntity implements Parcelable{

    private List<Pod> podList;// 货架集合

    private List<RobotCar> robotCarList;// 小车集合


    public BoxEntity() {
    }

    public BoxEntity(List<Pod> podList, List<RobotCar> robotCarList) {
        this.podList = podList;
        this.robotCarList = robotCarList;
    }

    public List<Pod> getPodList() {
        return podList;
    }

    public void setPodList(List<Pod> podList) {
        this.podList = podList;
    }

    public List<RobotCar> getRobotCarList() {
        return robotCarList;
    }

    public void setRobotCarList(List<RobotCar> robotCarList) {
        this.robotCarList = robotCarList;
    }

    @Override
    public String toString() {
        return "BoxEntity{" +
                "podList=" + podList +
                ", robotCarList=" + robotCarList +
                '}';
    }

    /**
     * 一个pod（货架）拥有的属性
     */
    public static class Pod implements Parcelable {

        private int podId;// 货架的id
        private int podPos;// 货架在地图上的位置


        public Pod() {
        }

        public int getPodId() {
            return podId;
        }

        public void setPodId(int podId) {
            this.podId = podId;
        }

        public int getPodPos() {
            return podPos;
        }

        public void setPodPos(int podPos) {
            this.podPos = podPos;
        }

        @Override
        public String toString() {
            return "Pod{" +
                    "podId=" + podId +
                    ", podPos=" + podPos +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.podId);
            dest.writeInt(this.podPos);
        }

        protected Pod(Parcel in) {
            this.podId = in.readInt();
            this.podPos = in.readInt();
        }

        public static final Parcelable.Creator<Pod> CREATOR = new Parcelable.Creator<Pod>() {
            @Override
            public Pod createFromParcel(Parcel source) {
                return new Pod(source);
            }

            @Override
            public Pod[] newArray(int size) {
                return new Pod[size];
            }
        };
    }

    /**
     * 一个小车拥有的属性
     */
    public static class RobotCar implements Parcelable {
        private int robotCarId;// 小车的id
        private int robotCarPos;// 小车在地图上的位置

        public RobotCar() {
        }

        public int getRobotCarId() {
            return robotCarId;
        }

        public void setRobotCarId(int robotCarId) {
            this.robotCarId = robotCarId;
        }

        public int getRobotCarPos() {
            return robotCarPos;
        }

        public void setRobotCarPos(int robotCarPos) {
            this.robotCarPos = robotCarPos;
        }

        @Override
        public String toString() {
            return "RobotCar{" +
                    "robotCarId=" + robotCarId +
                    ", robotCarPos=" + robotCarPos +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.robotCarId);
            dest.writeInt(this.robotCarPos);
        }

        protected RobotCar(Parcel in) {
            this.robotCarId = in.readInt();
            this.robotCarPos = in.readInt();
        }

        public static final Parcelable.Creator<RobotCar> CREATOR = new Parcelable.Creator<RobotCar>() {
            @Override
            public RobotCar createFromParcel(Parcel source) {
                return new RobotCar(source);
            }

            @Override
            public RobotCar[] newArray(int size) {
                return new RobotCar[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.podList);
        dest.writeTypedList(this.robotCarList);
    }

    protected BoxEntity(Parcel in) {
        this.podList = in.createTypedArrayList(Pod.CREATOR);
        this.robotCarList = in.createTypedArrayList(RobotCar.CREATOR);
    }

    public static final Parcelable.Creator<BoxEntity> CREATOR = new Parcelable.Creator<BoxEntity>() {
        @Override
        public BoxEntity createFromParcel(Parcel source) {
            return new BoxEntity(source);
        }

        @Override
        public BoxEntity[] newArray(int size) {
            return new BoxEntity[size];
        }
    };

}
