package com.example.pc2.media.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.pc2.media.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ProgressBarUtil {

    private static AlertDialog dialog = null;
    private static View view;


    /**
     * 加载进度
     * @param context 上下文对象
     * @param loadInfoHints 加载进度提示内容
     * @param tintColor 进度条颜色
     */
    public static void showProgressBar(Context context, String loadInfoHints, @ColorInt int tintColor){

        view = LayoutInflater.from(context).inflate(R.layout.custom_progress_bar_view, null);
        TextView tv_load_progress_hint = view.findViewById(R.id.tv_load_progress_hint);
        // 设置加载进度提示内容
        if (!TextUtils.isEmpty(loadInfoHints)){
            tv_load_progress_hint.setText(loadInfoHints);
        }else {
            tv_load_progress_hint.setText("加载中...");
        }

        MaterialProgressBar progressBar = view.findViewById(R.id.custom_material_circular);
        // 设置进度条着色颜色
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(tintColor));

        if (dialog == null){
            showDialog(context);// 创建对话框展示自定义进度条
        }else {
            dialog.show();
        }

    }

    public static void showProgressBar(Context context, String loadInfoHints){
        view = LayoutInflater.from(context).inflate(R.layout.custom_progress_bar_view, null);
        TextView tv_load_progress_hint = view.findViewById(R.id.tv_load_progress_hint);
        // 设置加载进度提示内容
        if (!TextUtils.isEmpty(loadInfoHints)){
            tv_load_progress_hint.setText(loadInfoHints);
        }else {
            tv_load_progress_hint.setText("加载中...");
        }

        if (dialog == null){
            showDialog(context);// 创建对话框展示自定义进度条
        }else {
            dialog.show();
        }

    }

    /**
     * 显示自定义进度对话框
     * @param context
     */
    private static void showDialog(Context context) {
        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        dialog.show();
    }


    /**
     * 进度框消失
     */
    public static void dissmissProgressBar(){
        if (dialog != null){
            dialog.dismiss();
        }
    }

}
