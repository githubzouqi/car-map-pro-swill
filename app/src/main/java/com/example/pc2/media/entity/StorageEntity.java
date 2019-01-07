package com.example.pc2.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC-2 on 2018/1/23.
 */

public class StorageEntity implements Parcelable{

    private String warehouseId;// 仓库id
    private String warehouseName;// 仓库名称

    private List<SectionEntity> sectionMap;// 某个仓库中包含的所有地图

    public StorageEntity() {
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public List<SectionEntity> getSectionMap() {
        return sectionMap;
    }

    public void setSectionMap(List<SectionEntity> sectionMap) {
        this.sectionMap = sectionMap;
    }

    @Override
    public String toString() {
        return "StorageEntity{" +
                "warehouseId='" + warehouseId + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", sectionMap=" + sectionMap +
                '}';
    }

    public static class SectionEntity {
        private String sectionName;// 地图名称
        private String sectionUUID;
        private String sectionMapId;
        private long sectionRcsId;// rcsid，用来绘制地图用，发布消息时携带的数据

        public SectionEntity() {
        }

        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getSectionUUID() {
            return sectionUUID;
        }

        public void setSectionUUID(String sectionUUID) {
            this.sectionUUID = sectionUUID;
        }

        public String getSectionMapId() {
            return sectionMapId;
        }

        public void setSectionMapId(String sectionMapId) {
            this.sectionMapId = sectionMapId;
        }

        public long getSectionRcsId() {
            return sectionRcsId;
        }

        public void setSectionRcsId(long sectionRcsId) {
            this.sectionRcsId = sectionRcsId;
        }

        @Override
        public String toString() {
            return "SectionEntity{" +
                    "sectionName='" + sectionName + '\'' +
                    ", sectionUUID='" + sectionUUID + '\'' +
                    ", sectionMapId='" + sectionMapId + '\'' +
                    ", sectionRcsId=" + sectionRcsId +
                    '}';
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.warehouseId);
        dest.writeString(this.warehouseName);
        dest.writeList(this.sectionMap);
    }

    protected StorageEntity(Parcel in) {
        this.warehouseId = in.readString();
        this.warehouseName = in.readString();
        this.sectionMap = new ArrayList<SectionEntity>();
        in.readList(this.sectionMap, SectionEntity.class.getClassLoader());
    }

    public static final Parcelable.Creator<StorageEntity> CREATOR = new Parcelable.Creator<StorageEntity>() {
        @Override
        public StorageEntity createFromParcel(Parcel source) {
            return new StorageEntity(source);
        }

        @Override
        public StorageEntity[] newArray(int size) {
            return new StorageEntity[size];
        }
    };

}
