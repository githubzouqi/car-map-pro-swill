package com.example.pc2.media.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pc2.media.R;
import com.example.pc2.media.constant.Constants;
import com.example.pc2.media.utils.LogUtil;
import com.example.pc2.media.utils.ToastUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
 * RCS部分，小车的控制部分
 *
 */
public class RcsCarOperateFragment extends Fragment {

    @BindView(R.id.iv_fragment_back)ImageView iv_fragment_back;
    @BindView(R.id.tv_fragment_title)TextView tv_fragment_title;

    private static final String CHARGING_ERROR = "clear_charging_error";

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象
    private View view_rcs_input;// 输入内容的view
    private Thread publishThread = null;// 发布消息消费线程
    private String str_charging_error = "";

    public RcsCarOperateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rcs_car_operate, container, false);
        ButterKnife.bind(this, view);

        getArgumentData();

        setTitleBar();
        return view;
    }

    /**
     * 设置标题栏样式
     */
    private void setTitleBar() {
        tv_fragment_title.setText("RCS部分");
    }

    /**
     * 获取数据
     */
    private void getArgumentData() {
        Bundle bundle_charging_error = getArguments();
        if (bundle_charging_error != null){
            str_charging_error = bundle_charging_error.getString(CHARGING_ERROR,"");
        }

    }

    /**
     * 单击事件监听
     * @param view
     */
    @OnClick({R.id.btn_rcs_clear_path, R.id.btn_rcs_clear_chargingPileError, R.id.iv_fragment_back
    , R.id.btn_rcs_changing_pod_position})
    public void doClick(View view){
        switch (view.getId()){
            case R.id.iv_fragment_back:// 返回上一界面
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btn_rcs_clear_path:// 清除小车路径

                alertDialogClearPath();

                break;

            case R.id.btn_rcs_clear_chargingPileError:// 清除充电桩故障
                if (TextUtils.isEmpty(str_charging_error)){
                    ToastUtil.showToast(getContext(),"充电桩暂无故障");
                    return;
                }

                setDialogView();

                TextView tv_chargingError = view_rcs_input.findViewById(R.id.tv_chargingPileError);
                final EditText et_chargerType = view_rcs_input.findViewById(R.id.et_rcs_chargerType);
                final EditText et_chargerId = view_rcs_input.findViewById(R.id.et_rcs_chargerId);

                tv_chargingError.setVisibility(View.VISIBLE);
                et_chargerType.setVisibility(View.VISIBLE);
                et_chargerId.setVisibility(View.VISIBLE);

                tv_chargingError.setText(str_charging_error);// 设置充电桩故障数据

                view_rcs_input.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String type = et_chargerType.getText().toString().trim();// 获取充电桩类型
                        final String id = et_chargerId.getText().toString().trim();// 获取充电桩id

                        if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(id)){
                            setUpConnectionFactory();

                            publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHARGING_PILE_CLEAR_ERROR);
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("清除充电桩故障" + "（类型：" + type
                                    + "，充电桩ID：" + id + "）？"
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
                                                message.put("deviceType", (Object)type);
                                                message.put("deviceId", (Object)id);
                                                queue.putLast(message);// 发送消息到MQ
                                                ToastUtil.showToast(getContext(),"清除充电桩故障指令已发布");
                                                dialog.dismiss();
                                                if (callBackListener != null){
                                                    callBackListener.clearChargingError();// 清除充电故障信息
                                                    getActivity().getSupportFragmentManager().popBackStack();// 回退上一界面
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();

                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入充电桩类型或者充电桩Id");
                        }
                    }
                });

                break;

            case R.id.btn_rcs_changing_pod_position:// 将通道上的空货架更新到地图上，防止重车撞空货架

                setDialogView();

                final EditText et_rcs_podId = view_rcs_input.findViewById(R.id.et_rcs_podId);
                final EditText et_rcs_addressCodeId = view_rcs_input.findViewById(R.id.et_rcs_addressCodeId);

                et_rcs_podId.setVisibility(View.VISIBLE);
                et_rcs_addressCodeId.setVisibility(View.VISIBLE);

                view_rcs_input.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String podCodeId = et_rcs_podId.getText().toString().trim();// 获取货架的ID
                        final String addressCodeId = et_rcs_addressCodeId.getText().toString().trim();// 获取目标地址码

                        if (!TextUtils.isEmpty(podCodeId) && !TextUtils.isEmpty(addressCodeId)){
                            setUpConnectionFactory();

                            publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHANGING_POD_POSITION);
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage(
                                            "将通道的空货架：" + podCodeId + "更新到目标地址："
                                            + addressCodeId + "？"
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
                                                message.put("podCodeID", (Object)podCodeId);
                                                message.put("addressCodeID", (Object)addressCodeId);
                                                queue.putLast(message);// 发送消息到MQ
                                                ToastUtil.showToast(getContext(),"更新通道空货架到地图上的指令已发布");
                                                dialog.dismiss();
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();

                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入货架ID或者目标地址码");
                        }
                    }
                });

                break;

        }
    }

    /**
     * 清除路径，输入小车ID的弹框
     */
    private void alertDialogClearPath() {
        setDialogView();

        final EditText et_rcs_carIdInput = view_rcs_input.findViewById(R.id.et_rcs_carIdInput);
        et_rcs_carIdInput.setVisibility(View.VISIBLE);

        view_rcs_input.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String str_carId = et_rcs_carIdInput.getText().toString().trim();// 获取输入值
                if (TextUtils.isEmpty(str_carId)){
                    ToastUtil.showToast(getContext(),"请输入小车的ID");
                    return;
                }

                setUpConnectionFactory();// MQ连接设置
                // 给RabbitMQ发送消息清除小车路径
                publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CLEAR_PATH);
                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.app_icon)
                        .setTitle("提示")
                        .setMessage("确定清除小车路径？")
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
                                    message.put("robotID", Long.parseLong(str_carId));
                                    queue.putLast(message);// 发送消息到MQ
                                    ToastUtil.showToast(getContext(),"清除小车路径指令已发布");
                                    dialog.dismiss();// 取消输入界面
                                    getActivity().getSupportFragmentManager().popBackStack();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();

                            }
                        }).create().show();

            }
        });

    }

    private AlertDialog dialog = null;
    /**
     * 设置弹出框的view
     */
    private void setDialogView(){
        view_rcs_input = getLayoutInflater().from(getContext()).inflate(R.layout.dialog_rcs_car,null);

        dialog = new AlertDialog.Builder(getContext())
                .setView(view_rcs_input)
                .create();
        dialog.show();
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
        interruptThread(publishThread);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            setTitleBar();
        }
    }

    private CallBackListener callBackListener = null;

    public interface CallBackListener{
        void clearChargingError();// 清除充电桩故障
    }

    /**
     * 注册监听器方法
     * @param callBackListener
     */
    public void setCallBack(CallBackListener callBackListener){
        this.callBackListener = callBackListener;
    }
}
