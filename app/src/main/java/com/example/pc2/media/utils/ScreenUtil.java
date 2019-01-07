package com.example.pc2.media.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by PC-2 on 2018/1/23.
 */

public class ScreenUtil {

    public static final String WIDTH = "width";// 宽度常量
    public static final String HEIGHT = "height";// 高度常量

    private WindowManager windowManager;
    private DisplayMetrics metrics = new DisplayMetrics();

    public ScreenUtil(Context context) {
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 获取手机屏幕的宽度，单位px
     * @param s 宽或高类型
     * @return
     */
    public int getScreenSize(String s) {
        // 获取显示度量，该显示度量描述了显示的大小和密度
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int w_or_h = 0;
        switch (s){
            case WIDTH://宽度
                w_or_h = metrics.widthPixels;
                break;
            case HEIGHT:// 高度
                w_or_h = metrics.heightPixels;
                break;
        }
        return w_or_h;// 返回手机屏幕的宽度或者高度
    }

    /**
     * 根据设备类型（手机或平板）来判断地图主界面的横竖屏显示方式
     * @param activity
     */
    public static void selectScreentDirection(Activity activity){
        if(!isTabletDevice(activity)){
//            ToastUtil.showToast(activity, "当前设备是手机设备");
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 设置竖屏显示
        }else {
//            ToastUtil.showToast(activity, "当前设备是平板设备");
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 设置横屏显示
        }
    }

    /**
     * 判断当前设备是否是平板
     * @param context
     * @return true 平板、false 手机
     */
    private static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
