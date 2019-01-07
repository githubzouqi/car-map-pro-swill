package com.example.pc2.media.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.pc2.media.R;
import com.example.pc2.media.constant.Constants;
import com.example.pc2.media.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * 地址配置界面
 */
public class AddressConfigFragment extends BaseFragment{

    @BindView(R.id.et_rabbitmq_host) EditText et_rabbitmq_host;// 主机地址
    @BindView(R.id.et_rabbitmq_port) EditText et_rabbitmq_port;// 端口
    @BindView(R.id.et_rabbitmq_username) EditText et_rabbitmq_username;// 用户名
    @BindView(R.id.et_rabbitmq_pasword) EditText et_rabbitmq_password;// 密码
    @BindView(R.id.check_rember_input) CheckBox check_rember_input;// checkbox控件

    @BindView(R.id.et_interface_address) EditText et_interface_address;// 接口地址

    // 配置信息的初始化值 TODO 为客户写死
    private String strHost = "52.83.227.228";
    private int strPort= 5672;
    private String strUsername = "nt_yuan_hui";
    private String strPassword = "yhYHzaq";
    private String strInterfaceAddress = "api.mushiny.com";

    private BoxFragment boxFragment = null;

    private SharedPreferences sp = null;// 声明SharedPreferences对象

    public static final String SP_FILE_NAME = "CONFIG";
    public static final String SP_KEY_HOST = "SP_KEY_HOST";// 主机地址
    public static final String SP_KEY_PORT = "SP_KEY_PORT";// 端口
    public static final String SP_KEY_USERNAME = "SP_KEY_USERNAME";// 用户名
    public static final String SP_KEY_PASSWORD = "SP_KEY_PASSWORD";// 密码
    public static final String SP_KEY_ROOT_ADDRESS = "SP_KEY_ROOT_ADDRESS";// 接口地址根路径
    public static final String SP_KEY_CHECK_STATUS = "SP_KEY_CHECK_STATUS";// 选择框选中状态

    public AddressConfigFragment() {
        // Required empty public constructor
    }


    /*
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        checkInput();
        show
        Fragment(AddressConfigFragment.this, new BoxFragment());
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_config, container, false);
        ButterKnife.bind(this, view);
        //setContent();// 设置保存的配置信息
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 光标出现的时候，隐藏软键盘

        return view;
    }

    /**
     * 是否需要加载配置信息，设置相应内容
     */
    private void    setContent() {
        sp = getContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        String sHost = sp.getString(SP_KEY_HOST, "");
        int iPort = sp.getInt(SP_KEY_PORT,0);
        String sUsername = sp.getString(SP_KEY_USERNAME, "");
        String sPassword = sp.getString(SP_KEY_PASSWORD, "");
        String sRootAddress = sp.getString(SP_KEY_ROOT_ADDRESS, "");
        boolean bl_check_status = sp.getBoolean(SP_KEY_CHECK_STATUS, false);

        if (!TextUtils.isEmpty(sHost)){// 主机地址
            et_rabbitmq_host.setText(sHost);
            et_rabbitmq_host.setSelection(sHost.length());
        }else {
            et_rabbitmq_host.setText("");
        }

        if (iPort != 0){// 端口
            et_rabbitmq_port.setText(iPort + "");
        }else {
            et_rabbitmq_port.setText("");
        }

        if (!TextUtils.isEmpty(sUsername)){// 用户名
            et_rabbitmq_username.setText(sUsername);
        }else {
            et_rabbitmq_username.setText("");
        }

        if (!TextUtils.isEmpty(sPassword)){// 密码
            et_rabbitmq_password.setText(sPassword);
        }else {
            et_rabbitmq_password.setText("");
        }

        if (!TextUtils.isEmpty(sRootAddress)){// 接口地址
            et_interface_address.setText(sRootAddress);
        }else {
            et_interface_address.setText("");
        }

        if (bl_check_status){// checkbox控件的选择状态
            check_rember_input.setChecked(true);
        }else {
            check_rember_input.setChecked(false);
        }
    }

    /**
     * 单击事件监听
     * @param view
     */
    @OnClick({R.id.btn_complete_address_input, R.id.check_rember_input})
    public void doClick(View view){
        switch (view.getId()){
            case R.id.btn_complete_address_input:
//                getHostContent();
//                getPort();
//                getUsername();
//                getPassword();
//                getInterfaceAddress();
//                checkInput();
                break;
        }
    }

    /**
     * 检查输入内容
     */
    private void checkInput() {

        if(!TextUtils.isEmpty(strHost)){
            Constants.MQ_HOST = strHost;
        }else {
            ToastUtil.showToast(getContext(),"主机地址不能为空");
            return;
        }

        if (strPort != 0){
            Constants.MQ_PORT = strPort;
        }else {
            ToastUtil.showToast(getContext(),"端口不能为空");
            return;
        }

        if (!TextUtils.isEmpty(strUsername)){
            Constants.MQ_USERNAME = strUsername;
        }else {
            ToastUtil.showToast(getContext(),"用户名不能为空");
            return;
        }

        if (!TextUtils.isEmpty(strPassword)){
            Constants.MQ_PASSWORD = strPassword;
        }else {
            ToastUtil.showToast(getContext(),"密码不能为空");
            return;
        }

        if (!TextUtils.isEmpty(strInterfaceAddress)){
            Constants.ROOT_ADDRESS = strInterfaceAddress;
        }else {
            ToastUtil.showToast(getContext(), "接口不能为空");
            return;
        }

        if(check_rember_input.isChecked()){// 需要记住信息
            sp = getContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(SP_KEY_HOST, Constants.MQ_HOST);
            editor.putInt(SP_KEY_PORT, Constants.MQ_PORT);
            editor.putString(SP_KEY_USERNAME, Constants.MQ_USERNAME);
            editor.putString(SP_KEY_PASSWORD, Constants.MQ_PASSWORD);
            editor.putString(SP_KEY_ROOT_ADDRESS, Constants.ROOT_ADDRESS);
            editor.putBoolean(SP_KEY_CHECK_STATUS, true);

            editor.commit();
        }else {// 不需要记住信息
            sp = getContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
            sp.edit().clear().commit();
        }

        if (boxFragment == null){
            boxFragment = new BoxFragment();
        }
        showFragment(AddressConfigFragment.this, boxFragment);
    }

    /**
     * 获取接口地址和端口
     */
    private void getInterfaceAddress() {
        strInterfaceAddress = et_interface_address.getText().toString();
    }

    /**
     * 获取密码
     */
    private void getPassword() {
        strPassword = et_rabbitmq_password.getText().toString();
    }

    /**
     * 获取用户名
     */
    private void getUsername() {
        strUsername = et_rabbitmq_username.getText().toString();
    }

    /**
     * 获取端口
     */
    private void getPort() {
        String str_port = et_rabbitmq_port.getText().toString();
        if (TextUtils.isEmpty(str_port)){
            ToastUtil.showToast(getContext(), "端口不能为空");
            return;
        }
        strPort = Integer.parseInt(str_port);
    }

    /**
     * 获取主机地址
     */
    private void getHostContent() {
        strHost = et_rabbitmq_host.getText().toString();
    }

    /**
     * 碎片之间的跳转
     * @param f_current
     * @param f_next
     */
    private void showFragment(Fragment f_current, Fragment f_next){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(f_current)
                .replace(R.id.frame_main_content, f_next)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

}
