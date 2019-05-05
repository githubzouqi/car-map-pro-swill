package com.example.pc2.swill.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pc2.swill.R;
import com.example.pc2.swill.constant.Constants;
import com.example.pc2.swill.utils.LogUtil;
import com.example.pc2.swill.utils.ProgressBarUtil;
import com.example.pc2.swill.utils.ToastUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * wcs调用接口来实现小车的控制。
 * 现在包含大部分查询的接口，陆续会实现所有涉及的接口
 *
 * 史必诺 更名为：其他
 * 添加了rcs的清除充电桩故障功能并去掉了部分wcs的接口项
 *
 */
public class WcsCarOperateFragment extends BaseFragment{

    private static final String CURRENT_FRAGMENT_TITLE = "其他";
    @BindView(R.id.iv_fragment_back)ImageView iv_fragment_back;
    @BindView(R.id.tv_fragment_title)TextView tv_fragment_title;

    private static final int WHAT_CAR_STATUS = 0x10;// 查看小车的状态
    private static final int WHAT_POD_STATUS = 0x11;// 查看pod信息
    private static final int WHAT_ADDR_STATUS = 0x12;// 查看地址状态
    private static final int WHAT_RESEND_ORDER = 0x13;// 重发任务
    private static final int WHAT_OFFLINE = 0x14;// 下线某小车
    private static final int WHAT_DRIVE_POD = 0x15;// 驱动pod去某地
    private RequestQueue requestQueue;// volley请求队列
    private String rootAddress = "";// 请求地址根路径
    private View viewShowContent = null;

    private String sectionId = "";// 地图的sectionId
    private AlertDialog dialog_operate;// 输入信息的弹框
    private AlertDialog dialog_response;// 查看返回结果弹框
    private ProgressDialog pDialog;// 进度框
    private View viewOperate;
    private TextView tv_other_tip_title;

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象
    private Thread publishThread = null;// 发布消息消费线程

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WHAT_CAR_STATUS:// 查看小车的状态
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    String strObj = (String) msg.obj;
                    try {
                        JSONObject objCarStatus = new JSONObject(strObj);
                        int robotId1 = msg.arg1;
                        alertDialogShowCarStatus(objCarStatus, robotId1);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case WHAT_POD_STATUS:// 查看pod信息
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    String objPodInfo = (String) msg.obj;
                    int podId1 = msg.arg1;
                    alertDialogShowPodStatus(objPodInfo, podId1);
                    break;
                case WHAT_ADDR_STATUS:// 查看地址状态
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    String objAddr = (String) msg.obj;
                    int addr1 = msg.arg1;
                    alertDialogShowAddrStatus(objAddr, addr1);
                    break;
                case WHAT_RESEND_ORDER:// 重发任务
                    ToastUtil.showToast(getContext(),"重发任务成功");
                    String objResend = (String) msg.obj;
                    int robotId2 = msg.arg1;
                    alertDialogShowResendOrder(objResend, robotId2);
                    break;
                case WHAT_OFFLINE:// 下线小车
                    ToastUtil.showToast(getContext(),"小车下线成功");
                    break;
                case WHAT_DRIVE_POD:// 驱动POD去某地
                    ToastUtil.showToast(getContext(),"POD驱动去目标点位成功");
                    break;
            }
        }
    };

    /**
     * 小车重发任务
     * @param objResend
     * @param robotId
     */
    private void alertDialogShowResendOrder(String objResend, int robotId) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);
        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);

        if (!TextUtils.isEmpty(objResend)){
            tv_showContent.setText(objResend);
        }

        viewShowContent.findViewById(R.id.btn_refresh).setVisibility(View.GONE);
        showResponseDialog("重发任务返回数据");
    }

    /**
     * 查看地址状态
     * @param objAddr
     * @param addr
     */
    private void alertDialogShowAddrStatus(String objAddr, final int addr) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
        if (!TextUtils.isEmpty(objAddr)){
            tv_showContent.setText(objAddr);
        }else {
            ToastUtil.showToast(getContext(), "地址不存在");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckAddrStatus(addr);
            }
        });

        showResponseDialog("查看地址状态");
    }

    /**
     * 显示pod信息
     * @param objPodInfo
     * @param podId1
     */
    private void alertDialogShowPodStatus(String objPodInfo, final int podId1) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
        if (!TextUtils.isEmpty(objPodInfo)){
            objPodInfo = "👀返回数据中 address 字段取值为 pod（货架） 当前在地图上的位置坐标👀️"
                    + "\n返回数据如下：\n👀" + objPodInfo + "👀";
            tv_showContent.setText(objPodInfo);
        }else {
            ToastUtil.showToast(getContext(), "POD信息为空，惊了！");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckPodStatus(podId1);
            }
        });

        showResponseDialog("查看POD信息");
    }

    /**
     * 显示小车的状态
     * @param objCarStatus
     * @param robotId1
     */
    private void alertDialogShowCarStatus(JSONObject objCarStatus, final int robotId1) {

        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);

        String strRobotStatus = "";
        try {

            JSONObject reInfo = objCarStatus.optJSONObject("reInfo");

            JSONObject robotMessage = reInfo.optJSONObject("robotMessage");

            int robotID = robotMessage.optInt("robotID");
            strRobotStatus += "小车 id：" + robotID + "\n";

            int addressCodeID = robotMessage.optInt("addressCodeID");
            strRobotStatus += "小车所在位置：" + addressCodeID + "\n";

            int podCodeID = robotMessage.optInt("podCodeID");
            if (podCodeID == 0){
                strRobotStatus += "小车所驮货架号： " + "\n";
            }else {
                strRobotStatus += "小车所驮货架号：" + podCodeID + "\n";
            }

            int robotStatus = reInfo.optInt("robotStatus");
            strRobotStatus += "小车当前状态：" + robotStatus + "（1=空闲，2=执行任务中，3=充电中，21=离线）" + "\n";

            if (!reInfo.isNull("orderType")){
                String orderType = reInfo.optString("orderType").toLowerCase();
                String typeDesc = "";
                if (orderType.equals("emptyrun")){
                    typeDesc = "（小车空跑任务）";
                }
                if (orderType.equals("podrun")){
                    typeDesc = "（货架回存储位任务）";
                }
                if (orderType.equals("chargerdrive")){
                    typeDesc = "（小车充电任务）";
                }
                if (orderType.equals("podscan")){
                    typeDesc = "（货架扫描任务）";
                }
                if (orderType.equals("stowpod")){
                    typeDesc = "（上架任务）";
                }
                if (orderType.equals("pickpod")){
                    typeDesc = "（拣货任务）";
                }
                if (orderType.equals("obppod")){
                    typeDesc = "（拣货问题处理）";
                }
                if (orderType.equals("ibppod")){
                    typeDesc = "（上架问题处理）";
                }
                if (orderType.equals("icqapod")){
                    typeDesc = "（盘点任务）";
                }
                if (orderType.equals("carraypod")){
                    typeDesc = "（搬运货架任务）";
                }

                strRobotStatus += "任务类型：" + orderType + typeDesc + "\n";

            }else {
                strRobotStatus += "任务类型：" + " " + "\n";
            }

            if (!reInfo.isNull("orderPath")){

                String orderPath = String.valueOf(reInfo.opt("orderPath"));
                strRobotStatus += "任务路径：" + orderPath + "\n";

            }else {
                strRobotStatus += "任务路径：" + "" + "\n";
            }


        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "小车状态信息数据解析异常：" + e.getMessage());
        }

        if (!TextUtils.isEmpty(strRobotStatus)){
            tv_showContent.setText(strRobotStatus);
        }else {
            ToastUtil.showToast(getContext(), "小车信息为空，惊了！");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckCarStatus(robotId1);
            }
        });

        showResponseDialog("查看某小车状态");

    }

    public WcsCarOperateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wcs_car_operate, container, false);
        ButterKnife.bind(this, view);// 控件绑定
        init();// 初始化
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 光标出现的时候，隐藏软键盘
        setListeners();
        setTitleBar();
        return view;
    }

    /**
     * 设置标题栏样式
     */
    private void setTitleBar() {
        tv_fragment_title.setText(CURRENT_FRAGMENT_TITLE);
    }

    /**
     * 初始化数据
     */
    private void init() {
        requestQueue  = Volley.newRequestQueue(getContext());// 创建RequestQueue对象

        rootAddress = Constants.HTTP + Constants.ROOT_ADDRESS;// 请求地址根路径赋值

        sectionId = Constants.SECTIONID;// 地图的sectionId

        pDialog = new ProgressDialog(getContext());
        pDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 显示加载进度框
     * @param s 描述内容
     */
    private void showDialog(String s) {
        pDialog.setMessage(s);
        pDialog.show();
    }

    /**
     * 消失对话框，将其从屏幕上移除
     */
    private void dissMissDialog(){
        pDialog.dismiss();
    }

    /**
     * 展示返回结果弹框
     * @param message
     */
    private void showResponseDialog(String message){
        if (dialog_response != null && dialog_response.isShowing()){
            dialog_response.dismiss();
        }

        dialog_response = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setView(viewShowContent)
                .create();

        dialog_response.show();
    }

    /**
     * 设置监听
     */
    private void setListeners() {


    }

    /**
     * 设置dialog展示的view（用户选择操作弹框）
     */
    private void setDialogView(String tipTitle){
        viewOperate = getLayoutInflater().from(getContext()).inflate(R.layout.dialog_wcs_car, null);

        // 设置提示标题
        tv_other_tip_title = viewOperate.findViewById(R.id.tv_other_tip_title);
        tv_other_tip_title.setText(tipTitle);

        dialog_operate = new AlertDialog.Builder(getContext())
                .setView(viewOperate)
                .create();

        dialog_operate.show();
    }

    /**
     * 单击事件监听
     * @param view
     */
    @OnClick({R.id.tv_checkCarStatus, R.id.tv_checkPodStatus, R.id.tv_checkAddrStatus, R.id.tv_resendOrder
    , R.id.btn_robotAct, R.id.btn_robotOffline, R.id.btn_drivePod, R.id.btn_updatePodStatus
    ,R.id.btn_releasePodStatus, R.id.btn_updateAddrState, R.id.btn_robot2Charge, R.id.btn_autoDrivePod
            , R.id.btn_driveRobotCarryPod, R.id.iv_fragment_back, R.id.btn_driveRobot
    ,R.id.btn_clearChargeError, R.id.btn_updatePodOnMap, R.id.btn_carUp, R.id.btn_carDown
    , R.id.btn_carLeft, R.id.btn_carRight, R.id.btn_updateRobotStatus, R.id.btn_speedlogFinishTripByRobot
    , R.id.btn_speedlogFinishTripByPod})
    public void doClick(View view){

        switch (view.getId()){
            case R.id.iv_fragment_back:// 返回上一界面
                getActivity().getSupportFragmentManager().popBackStack();
                break;

            case R.id.tv_checkCarStatus:// 查看某小车的状态

                setDialogView("查看某小车的状态");

                final EditText et_carIdInput = viewOperate.findViewById(R.id.et_carIdInput);
                et_carIdInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strCarId = et_carIdInput.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            methodCheckCarStatus(Integer.parseInt(strCarId));

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.tv_checkPodStatus:// 查看pod信息

                setDialogView("查看pod信息");

                final EditText et_podIdInput = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strPodId = et_podIdInput.getText().toString().trim();// 取pod的id
                        if (!TextUtils.isEmpty(strPodId)){
                            methodCheckPodStatus(Integer.parseInt(strPodId));
                        }else {
                            ToastUtil.showToast(getContext(), "请输入POD的id");
                        }
                    }
                });


                break;

            case R.id.tv_checkAddrStatus:// 查看地址状态

                setDialogView("查看地址状态");

                final EditText et_addrInput = viewOperate.findViewById(R.id.et_addrInput);
                et_addrInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strAddr = et_addrInput.getText().toString().trim();// 取地址
                        if (!TextUtils.isEmpty(strAddr)){
                            methodCheckAddrStatus(Integer.parseInt(strAddr));
                        }else {
                            ToastUtil.showToast(getContext(), "请输入地址");
                        }
                    }
                });


                break;

            case R.id.tv_resendOrder:// 重发任务

                setDialogView("重发任务");

                final EditText et_resendOrder = viewOperate.findViewById(R.id.et_carIdInput);
                et_resendOrder.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strCarId = et_resendOrder.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            new AlertDialog.Builder(getContext())
                                    .setMessage("重发任务？（请谨慎操作！！！）")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodResendOrder(Integer.parseInt(strCarId));
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.btn_robotAct:// 控制小车上下左右移动一格

                setDialogView("控制小车移动一格");

                EditText et_robotAct = viewOperate.findViewById(R.id.et_carIdInput);
                Spinner sp_robotAct = viewOperate.findViewById(R.id.sp_robotAct);

                et_robotAct.setVisibility(View.VISIBLE);
                sp_robotAct.setVisibility(View.VISIBLE);

                break;

            case R.id.btn_robotOffline:// 下线某小车

                setDialogView("下线某小车");

                final EditText et_robotOffline = viewOperate.findViewById(R.id.et_carIdInput);
                et_robotOffline.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strCarId = et_robotOffline.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            new AlertDialog.Builder(getContext())
                                    .setMessage("下线" + strCarId + "号小车？（请谨慎操作！！！）")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodOffline(Integer.parseInt(strCarId));
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.btn_drivePod:// 驱动pod去目标点位
                setDialogView("驱动pod去目标点位（可用存储区）");

                final EditText et_podIdInput_drivePod = viewOperate.findViewById(R.id.et_podIdInput);
                final EditText et_addrInput_drivePod = viewOperate.findViewById(R.id.et_addrInput);

                et_podIdInput_drivePod.setVisibility(View.VISIBLE);// pod的id
                et_addrInput_drivePod.setVisibility(View.VISIBLE);// 目标点位

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_drivePod.getText().toString().trim();// 取pod的id
                        final String strAddr = et_addrInput_drivePod.getText().toString().trim();// 取目标点位

                        if (!TextUtils.isEmpty(strPodId) && !TextUtils.isEmpty(strAddr)){

                            // 非存储区的货架不让调用该接口
                            if ((Constants.unStoragePodsList.size() != 0) &&
                                    Constants.unStoragePodsList.contains(Integer.parseInt(strPodId))){
                                ToastUtil.showToast(getContext(), "输入货架必须为存储区的货架，请重新输入");
                            }else{
                                new AlertDialog.Builder(getContext())
                                        .setMessage("驱动" + strPodId + "号POD去目标点位" + strAddr + "？（请谨慎操作！！！）")
                                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                methodDrivePod(strPodId, strAddr);
                                                dialog.dismiss();
                                            }
                                        })
                                        .create().show();
                            }

                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空 或 目标点位不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_updatePodStatus:// 更新pod地址
                setDialogView("更新pod地址");

                final EditText et_podIdInput_updatePod = viewOperate.findViewById(R.id.et_podIdInput);
                final EditText et_addrInput_updatePod = viewOperate.findViewById(R.id.et_addrInput);

                et_podIdInput_updatePod.setVisibility(View.VISIBLE);// pod的id
                et_addrInput_updatePod.setVisibility(View.VISIBLE);// 更新地址
                
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_updatePod.getText().toString().trim();// 取pod的id
                        final String strAddr = et_addrInput_updatePod.getText().toString().trim();// 取更新地址

                        if (!TextUtils.isEmpty(strPodId) && !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setMessage("更新 " + strPodId + "号POD的地址为 " + strAddr + "？（请谨慎操作！！！）")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodUpdatePodStatus(strPodId, strAddr);
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空 或 更新地址不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_releasePodStatus:// 释放pod状态

                setDialogView("释放pod状态");

                final EditText et_podIdInput_releasePod = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput_releasePod.setVisibility(View.VISIBLE);// pod的id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_releasePod.getText().toString().trim();// 取pod的id

                        if (!TextUtils.isEmpty(strPodId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("释放" + strPodId + "POD状态？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodReleasePodStatus(strPodId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_updateAddrState:// 更新地址状态
                setDialogView("更新地址状态");

                final EditText et_podIdInput_updateAddr = viewOperate.findViewById(R.id.et_addrInput);
                et_podIdInput_updateAddr.setVisibility(View.VISIBLE);// 地址坐标

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strAddrId = et_podIdInput_updateAddr.getText().toString().trim();// 取地址

                        if (!TextUtils.isEmpty(strAddrId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("更新地址" + strAddrId + "状态？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodUpdateAddrState(strAddrId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "地址输入不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_robot2Charge:// 下发充电任务
                setDialogView("下发充电任务");

                final EditText et_robot2Charge = viewOperate.findViewById(R.id.et_carIdInput);
                et_robot2Charge.setVisibility(View.VISIBLE);// 小车id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strRobotId = et_robot2Charge.getText().toString().trim();// 取小车id
                        final int robotId = Integer.parseInt(strRobotId.trim());// 转为整数

                        if (!TextUtils.isEmpty(strRobotId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("给" + robotId + "号小车下发充电任务？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            if (robotId == 0){
                                                ToastUtil.showToast(getContext(), "小车id不能为0");
                                                return;
                                            }
                                            method2Charge(robotId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "地址输入不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_autoDrivePod:// 自动分配POD回存储区

                setDialogView("自动分配pod回存储区");

                final EditText et_podIdInput_autoDrive = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput_autoDrive.setVisibility(View.VISIBLE);// pod的id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_autoDrive.getText().toString().trim();// 取pod的id

                        if (!TextUtils.isEmpty(strPodId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("自动分配POD：" + strPodId + "回存储区？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodAutoDrivePod(strPodId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_driveRobotCarryPod:// 驱动小车驮pod去某地
                setDialogView("驱动小车驮pod去某地");

                final EditText et_robotId = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                final EditText et_podId = viewOperate.findViewById(R.id.et_podIdInput);// pod的id
                final EditText et_addrId = viewOperate.findViewById(R.id.et_addrInput);// 地址

                et_robotId.setVisibility(View.VISIBLE);
                et_podId.setVisibility(View.VISIBLE);
                et_addrId.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId.getText().toString().trim();
                        final String strPod = et_podId.getText().toString().trim();
                        final String strAddr = et_addrId.getText().toString().trim();

                        if (!TextUtils.isEmpty(strRobot) &&
                                !TextUtils.isEmpty(strPod) &&
                                !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("驱动小车 " + strRobot + "驮pod " + strPod + "去地址 " + strAddr + "?")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodDriveRobotCarryPod(strRobot, strPod, strAddr);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入小车id、pod或者地址");
                        }

                    }
                });

                break;

            case R.id.btn_driveRobot:// 驱动小车去某地

                setDialogView("驱动小车去某地");

                final EditText et_robotId_driveRobot = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                final EditText et_addrId_driveRobot = viewOperate.findViewById(R.id.et_addrInput);// 地址

                et_robotId_driveRobot.setVisibility(View.VISIBLE);
                et_addrId_driveRobot.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId_driveRobot.getText().toString().trim();
                        final String strAddr = et_addrId_driveRobot.getText().toString().trim();

                        if (!TextUtils.isEmpty(strRobot) &&
                                !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("驱动小车 " + strRobot + "去地址 " + strAddr + "?")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodDriveRobot(strRobot, strAddr);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入小车id或者地址");
                        }
                    }
                });

                break;

            case R.id.btn_clearChargeError:// 清除充电桩故障
                setDialogView("清除充电桩故障");

                final EditText et_other_chargerId = viewOperate.findViewById(R.id.et_other_chargerId);// 充电桩id
                final EditText et_other_chargerType = viewOperate.findViewById(R.id.et_other_chargerType);// 充电桩类型

                et_other_chargerId.setVisibility(View.VISIBLE);
                et_other_chargerType.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String str_charger_id = et_other_chargerId.getText().toString().trim();
                        final String str_charger_type = et_other_chargerType.getText().toString().trim();

                        if (!TextUtils.isEmpty(str_charger_id) &&
                                !TextUtils.isEmpty(str_charger_type)){

                            setUpConnectionFactory();// 设置连接
                            publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHARGING_PILE_CLEAR_ERROR);
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！清除充电桩故障" + "（类型：" + str_charger_type
                                            + "，充电桩ID：" + str_charger_id + "）？"
                                    )
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            interruptThread(publishThread);
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Map<String, Object> message = new HashMap<>();
                                                message.put("deviceType", Integer.parseInt(str_charger_type));
                                                message.put("deviceId", Integer.parseInt(str_charger_id));
                                                queue.putLast(message);// 发送消息到MQ
                                                ToastUtil.showToast(getContext(),"清除充电桩故障指令已发布");

                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            getActivity().getSupportFragmentManager().popBackStack();
                                            dialog.dismiss();

                                        }
                                    }).create().show();

                        }else {
                            ToastUtil.showToast(getContext(),"请输入充电桩类型或者充电桩id");
                        }
                    }
                });

                break;

            case R.id.btn_updatePodOnMap:// 将通道的空货架更新到地图上
                setDialogView("将通道的空货架更新到地图上");

                final EditText et_other_update_pod = viewOperate.findViewById(R.id.et_podIdInput);// 货架id
                final EditText et_other_update_address = viewOperate.findViewById(R.id.et_addrInput);// 地址码

                et_other_update_pod.setVisibility(View.VISIBLE);// pod的id
                et_other_update_address.setVisibility(View.VISIBLE);// 更新地址

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strPod = et_other_update_pod.getText().toString().trim();
                        final String strAddress = et_other_update_address.getText().toString().trim();

                        if (!TextUtils.isEmpty(strPod) && !TextUtils.isEmpty(strAddress)){
                            setUpConnectionFactory();

                            publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHANGING_POD_POSITION);
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage(
                                            "亲爱的工程师！确定将通道的空货架：" + strPod + "更新到目标地址："
                                                    + strAddress + "？"
                                    )
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            interruptThread(publishThread);
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Map<String, Object> message = new HashMap<>();
                                                message.put("podCodeID", Integer.parseInt(strPod));
                                                message.put("addressCodeID", Integer.parseInt(strAddress));
                                                queue.putLast(message);// 发送消息到MQ
                                                ToastUtil.showToast(getContext(),"更新通道空货架到地图上的指令已发布");
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            getActivity().getSupportFragmentManager().popBackStack();
                                            dialog.dismiss();

                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入货架ID或者目标地址码");
                        }

                    }
                });

                break;

            case R.id.btn_carUp:// 小车上移一格

                setDialogView("小车上移一格，由 A 位置移至 A-" + Constants.MAP_COLUMNS + " 位置");

                final EditText et_carId_up = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                et_carId_up.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String str_carIdUp = et_carId_up.getText().toString().trim();
                        if (!TextUtils.isEmpty(str_carIdUp)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("确定上移一格？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            call_carMoveUp(Integer.parseInt(str_carIdUp));
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车 id");
                        }

                    }
                });

                break;

            case R.id.btn_carDown:// 小车下移一格

                setDialogView("小车下移一格，由 A 位置移至 A+" + Constants.MAP_COLUMNS + " 位置");

                final EditText et_carId_down = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                et_carId_down.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String str_carIdDown = et_carId_down.getText().toString().trim();
                        if (!TextUtils.isEmpty(str_carIdDown)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("确定下移一格？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            call_carMoveDown(Integer.parseInt(str_carIdDown));
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车 id");
                        }

                    }
                });

                break;

            case R.id.btn_carLeft:// 小车左移一格

                setDialogView("小车左移一格，由 A 位置移至 A-" + 1 + " 位置");

                final EditText et_carId_left = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                et_carId_left.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String str_carIdLeft = et_carId_left.getText().toString().trim();
                        if (!TextUtils.isEmpty(str_carIdLeft)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("确定左移一格？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            call_carMoveLeft(Integer.parseInt(str_carIdLeft));
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车 id");
                        }

                    }
                });

                break;

            case R.id.btn_carRight:// 右移一格

                setDialogView("小车右移一格，由 A 位置移至 A+" + 1 + " 位置");

                final EditText et_carId_right = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                et_carId_right.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String str_carIdRight = et_carId_right.getText().toString().trim();
                        if (!TextUtils.isEmpty(str_carIdRight)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("确定右移一格？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            call_carMoveRight(Integer.parseInt(str_carIdRight));
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车 id");
                        }

                    }
                });

                break;

            case R.id.btn_updateRobotStatus:// 更新小车

                setDialogView("更新小车");
                final EditText et_car_id = viewOperate.findViewById(R.id.et_carIdInput);// 小车的id
                et_car_id.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String robotId = et_car_id.getText().toString().trim();
                        if (!TextUtils.isEmpty(robotId)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("确定更新小车 " + robotId + " 可用（Status 为 1、Available 为 true 以及 CancelOrder 为 true）？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodUpdateRobotStatus(robotId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的 id");
                            return;
                        }
                    }
                });

                break;

            case R.id.btn_speedlogFinishTripByRobot:// speedlog特有接口

                setDialogView("输入AGV号码结束任务");
                final EditText et_speedlogFinishTripByRobotId = viewOperate.findViewById(R.id.et_carIdInput);// 小车的id
                et_speedlogFinishTripByRobotId.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String robotId = et_speedlogFinishTripByRobotId.getText().toString().trim();
                        if (!TextUtils.isEmpty(robotId)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("温馨提示")
                                    .setMessage("确定结束任务？")
                                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            method2FinishTripByRobotId(robotId);
                                        }
                                    })
                                    .setNegativeButton("好的", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入AGV号码");
                            return;
                        }
                    }
                });

                break;

            case R.id.btn_speedlogFinishTripByPod:// speedlog特有接口

                setDialogView("输入POD号码结束任务");
                final EditText et_speedlogFinishTripByPod = viewOperate.findViewById(R.id.et_podIdInput);// 货架id
                et_speedlogFinishTripByPod.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String podId = et_speedlogFinishTripByPod.getText().toString().trim();
                        if (!TextUtils.isEmpty(podId)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("温馨提示")
                                    .setMessage("确定结束任务？")
                                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            method2FinishTripByPodId(podId);
                                        }
                                    })
                                    .setNegativeButton("好的", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入POD号码");
                            return;
                        }
                    }
                });

                break;


        }

    }

    /**
     * speedlog中通过输入POD号码来结束任务
     * @param podId
     */
    private void method2FinishTripByPodId(String podId) {

        ProgressBarUtil.showProgressBar(getContext(), "结束任务......");
        String url = rootAddress + getResources().getString(R.string.url_speedlog_finishTripByRobot)
                + "sectionId=" + sectionId + "&podId=" + podId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ProgressBarUtil.dissmissProgressBar();
                        try{

                            String result = response.optString("result");

                            if ("success".equals(result)){
                                ToastUtil.showToast(getContext(), "结束任务成功");
                            }else if("failure".equals(result)){
                                ToastUtil.showToast(getContext(), "结束任务失败：" + response.optString("msg"));
                            }
                        }catch (Exception e){
                            ToastUtil.showToast(getContext(), "解析异常：" + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressBarUtil.dissmissProgressBar();
                        ToastUtil.showToast(getContext(), "结束任务异常：" + error.getMessage());
                        return;
                    }
                });

        requestQueue.add(request);
    }

    /**
     * speedlog中通过输入AGV号码来结束任务
     * @param robotId
     */
    private void method2FinishTripByRobotId(String robotId) {

        ProgressBarUtil.showProgressBar(getContext(), "结束任务......");
        String url = rootAddress + getResources().getString(R.string.url_speedlog_finishTripByRobot)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ProgressBarUtil.dissmissProgressBar();
                        try{

                            String result = response.optString("result");

                            if ("success".equals(result)){
                                ToastUtil.showToast(getContext(), "结束任务成功");
                            }else if("failure".equals(result)){
                                ToastUtil.showToast(getContext(), "结束任务失败：" + response.optString("msg"));
                            }
                        }catch (Exception e){
                            ToastUtil.showToast(getContext(), "解析异常：" + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressBarUtil.dissmissProgressBar();
                        ToastUtil.showToast(getContext(), "结束任务异常：" + error.getMessage());
                        return;
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 更新小车（status为1、available为true、cancelOrder为true）
     * @param robotId
     */
    private void methodUpdateRobotStatus(final String robotId) {

        showDialog("更新小车...");

        String status = "1";
        boolean available = true;
        boolean cancelOrder = true;

        String url = rootAddress + getResources().getString(R.string.url_updateRobotStatus)
                + "sectionId=" + sectionId + "&robotId=" + robotId + "&status=" + status
                + "&available=" + available + "&cancelOrder=" + cancelOrder;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (response.contains("小车不在同一个section")){
                            ToastUtil.showToast(getContext(), "小车不在同一个地图，请输入正确的小车 ID");
                            return;
                        }

                        ToastUtil.showToast(getContext(), "更新小车" + robotId + "成功");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新小车over");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 驱动小车去某地
     * @param robotId
     * @param addrCodeId
     */
    private void methodDriveRobot(String robotId, String addrCodeId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_driveRobot)
                + "sectionId=" + sectionId + "&addrCodeId=" + addrCodeId
                + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查小车和地址输入是否正确");
                            return;
                        }else if ("ok".equals(response)){
                            ToastUtil.showToast(getContext(),"小车驱动成功");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动小车over");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 小车上移一格
     */
    private void call_carMoveUp(int robotId){
        String act = "up";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车下移一格
     */
    private void call_carMoveDown(int robotId){
        String act = "down";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车左移一格
     */
    private void call_carMoveLeft(int robotId){
        String act = "left";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车右移一格
     */
    private void call_carMoveRight(int robotId){
        String act = "right";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 驱动小车驮pod去某地
     * @param robotId
     * @param podId
     * @param addrCodeId
     */
    private void methodDriveRobotCarryPod(String robotId, String podId, String addrCodeId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_driveRobotCarryPod)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId
                + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "返回信息为空");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"操作成功");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动小车驮pod_over");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 自动分配pod回存储区
     * @param podId
     */
    private void methodAutoDrivePod(String podId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_autoAssignAdnDrivePod)
                + "sectionId=" + sectionId + "&podId=" + podId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查pod是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"PodRun调度任务已生成");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "自动分配pod回存储区over_e");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 小车下发充电任务
     * @param robotId
     */
    private void method2Charge(int robotId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_robot2Charge)
                + "robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查小车是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"下发充电任务成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "下发充电任务over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 更新地址状态
     * @param addrCodeId
     */
    private void methodUpdateAddrState(String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_updateAddrState)
                + "sectionId=" + sectionId + "&addrCodeId=" + addrCodeId
                + "&status=" + "Available" + "&lockedby=" + 0;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查地址是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"更新地址状态成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新地址状态over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 释放pod状态
     * @param podId
     */
    private void methodReleasePodStatus(String podId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_releasePodStatus)
                + "sectionId=" + sectionId + "&podId=" + podId + "&lockedBy=" + 0;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查pod是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"POD状态释放成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "释放pod状态over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 更新pod地址
     * @param podId
     * @param addrCodeId
     */
    private void methodUpdatePodStatus(String podId, String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_updatePodPos)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "POD或更新地址填写不正确");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新POD地址异常");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 驱动pod去某地
     * @param podId
     * @param addrCodeId
     */
    private void methodDrivePod(String podId, String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_drivePod)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "POD或目标点位填写不正确");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_DRIVE_POD;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动pod异常");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 下线某小车
     * @param robotId
     */
    private void methodOffline(final int robotId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_robotOffline)
                + "robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (response.toString().contains("没有这辆小车")){
                            ToastUtil.showToast(getContext(), response);
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_OFFLINE;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "下线小车over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 重发任务
     * @param robotId
     */
    private void methodResendOrder(final int robotId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_resendOrder)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();

                        ToastUtil.showToast(getContext(), "重发任务over_s");
                        if (!TextUtils.isEmpty(response.toString())){

                            String strRes = response.toString();
                            if (strRes.contains("未注册")){
                                ToastUtil.showToast(getContext(), response.toString());
                                return;
                            }else {
                                Message message = handler.obtainMessage();
                                message.what = WHAT_RESEND_ORDER;
                                message.obj = response.toString();
                                message.arg1 = robotId;
                                handler.sendMessage(message);
                            }

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "重发任务over_e");
                    }
                });

        requestQueue.add(request);


    }

    /**
     * 产看地址状态
     * @param addr
     */
    private void methodCheckAddrStatus(final int addr) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkAddrStatus)
                + "sectionId=" + sectionId + "&addrCodeId=" + addr;

        LogUtil.e("url","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "地址不存在");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_ADDR_STATUS;
                        message.obj = response.toString();
                        message.arg1 = addr;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看地址状态error");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 查看pod信息
     * @param podId
     */
    private void methodCheckPodStatus(final int podId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkPodStatus)
                + "sectionId=" + sectionId + "&podId=" + podId;

        LogUtil.e("url","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "pod信息为空");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_POD_STATUS;
                        message.obj = response.toString();
                        message.arg1 = podId;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看POD信息error");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 查看某小车的状态
     * @param robotId 小车的id
     */
    private void methodCheckCarStatus(final int robotId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkCarState)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        LogUtil.e("url","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dissMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "地图上没有此小车");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_CAR_STATUS;
                        message.obj = response.toString();
                        message.arg1 = robotId;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dissMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看小车状态error");
                    }
        });

        requestQueue.add(request);

    }

    /**
     * 连接设置
     */
    private void setUpConnectionFactory() {
        factory.setHost(Constants.MQ_HOST);//主机地址
        factory.setPort(Constants.MQ_PORT);// 端口号
        factory.setUsername(Constants.MQ_USERNAME);// 用户名
        factory.setPassword(Constants.MQ_PASSWORD);// 密码
        factory.setAutomaticRecoveryEnabled(false);
    }

    // 创建BlockingDeque对象
    private BlockingDeque<Map<String, Object>> queue = new LinkedBlockingDeque<>();
    private void publishToAMPQ(final String exchange, final String routingKey) {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();
                        while (true) {
                            Map<String, Object> message = queue.takeFirst();
                            try {
                                ch.basicPublish(exchange, routingKey, null, serialize((Serializable)message));
                                ch.waitForConfirmsOrDie();

                            } catch (Exception e) {
                                queue.putFirst(message);
                                throw e;
                            }

                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        LogUtil.d("TAG_Publish", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;

                        }

                    }

                }

            }

        });

        publishThread.start();
    }

    /**
     * 将map对象转换为byte[]
     * @param obj
     * @return
     */
    private byte[] serialize(Serializable obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
        serialize(obj, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 写对象到输出流
     * @param obj
     * @param outputStream
     */
    private void serialize(Serializable obj, OutputStream outputStream) {
        if(outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(obj);
            } catch (IOException var11) {
                var11.printStackTrace();
            } finally {
                try {
                    if(out != null) {
                        out.close();
                    }
                } catch (IOException var10) {
                    var10.printStackTrace();
                }

            }

        }
    }

    /**
     * 中断线程
     * @param thread
     */
    private void interruptThread(Thread thread) {
        if(thread != null){
            thread.interrupt();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 移除所有的回调和消息，防止Handler泄露
        handler.removeCallbacksAndMessages(null);
        if(requestQueue != null){
            requestQueue.stop();// 停止缓存和网络调度程序
        }
        interruptThread(publishThread);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            setTitleBar();
        }
    }
}
