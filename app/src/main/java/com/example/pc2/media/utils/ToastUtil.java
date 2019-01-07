package com.example.pc2.media.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by PC-2 on 2018/1/23.
 * 吐丝工具类
 */

public class ToastUtil {

    public static Toast toast;
    /**
     * 吐丝的方法，可以避免重复吐丝。当你点击多次按钮的时候，吐丝只出现一次。
     * @param context 上下文对象
     * @param string    吐丝的内容
     */
    public static void showToast(Context context, String string) {
        if(toast == null){
            // 如果Toast对象为空了，那么需要创建一个新的Toast对象
            toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
        }else {
            // 如果toast对象还存在，那么就不再创建新的Toast对象
            toast.setText(string);
        }
        // 最后调用show方法吐丝
        toast.show();
    }

}
