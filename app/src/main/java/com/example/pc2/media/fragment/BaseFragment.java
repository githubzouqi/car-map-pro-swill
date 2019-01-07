package com.example.pc2.media.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by zouqi on 2018/3/25.
 * Fragment的基类，为解决fragment嵌套时出现的重影问题
 */

public class BaseFragment extends Fragment{

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            boolean isHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);// 获取保存fragment前的可视状态

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if(isHidden){
                transaction.hide(this);
            }else {
                transaction.show(this);
            }

            transaction.commit();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN,isHidden());// 保存当前fragment的可视状态
    }
}
