package com.example.pc2.swill.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {

    // 更新pod地址
    public static final String NAME_UPDATE_POD_SUCCESS = "NAME_UPDATE_POD_SUCCESS";
    public static final String KEY_UPDATE_POD_SUCCESS_PODID = "KEY_UPDATE_POD_SUCCESS_PODID";
    public static final String KEY_UPDATE_POD_SUCCESS_ADDRESS = "KEY_UPDATE_POD_SUCCESS_ADDRESS";

    // 下线小车（空闲或者正在充电桩充电）
    public static final String NAME_OFFLINE_AGV = "NAME_OFFLINE_AGV";
    public static final String KEY_OFFLINE_AGV_ID = "KEY_OFFLINE_AGV_ID";

    // 地图移除货架
    public static final String NAME_REMOVE_POD = "NAME_REMOVE_POD";
    public static final String KEY_REMOVE_PODID = "KEY_REMOVE_PODID";

    private Context context;
    private SharedPreferences sp = null;// 声明SharedPreferences对象

    public SPUtil(Context context) {
        this.context = context;
    }

    public SharedPreferences getSPByName(String name){
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp;
    }

    public void clearDataByName(String name){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

}
