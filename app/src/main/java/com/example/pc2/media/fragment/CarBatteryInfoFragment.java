package com.example.pc2.media.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pc2.media.R;
import com.example.pc2.media.adapter.CarBatteryInfoAdapter;
import com.example.pc2.media.constant.Constants;
import com.example.pc2.media.entity.CarBatteryInfoEntity;
import com.example.pc2.media.utils.LogUtil;
import com.example.pc2.media.utils.ToastUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrClassicDefaultFooter;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;

/**
 * A simple {@link Fragment} subclass.
 * 小车电量信息界面
 */
public class CarBatteryInfoFragment extends BaseFragment {

    @BindView(R.id.ptrFrameLayout) PtrFrameLayout ptrFrameLayout;
    @BindView(R.id.lsv_carBatteryInfo) ListView lsv_carBatteryInfo;// listview控件
    @BindView(R.id.tv_carNumber) TextView tv_carNumber;
    @BindView(R.id.tv_battery_fine) TextView tv_battery_fine;
    @BindView(R.id.tv_battery_crisis) TextView tv_battery_crisis;
    @BindView(R.id.tv_battery_low) TextView tv_battery_low;
    @BindView(R.id.tv_battery_pause) TextView tv_battery_pause;
    @BindView(R.id.et_search_car) TextView et_search_car;// 搜索框

    @BindView(R.id.iv_fragment_back)ImageView iv_fragment_back;
    @BindView(R.id.tv_fragment_title)TextView tv_fragment_title;

    private static final int WHAT_CLOSE_CONNECTION = 1;
    private static final int WHAT_CAR_BATTERY_INFO = 2;
    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象

    private Thread thread_car_battery_info = null;// 小车电量信息
    private Thread thread_close_connection = null;

        private Connection connection_car_battery_info = null;
    private int defaultLength = 20;// 默认显示数据

    private SpannableString spannableString;// 小车总数量
    private SpannableString spannableString_fine;// 电量良好
    private SpannableString spannableString_crisis;// 电量临界
    private SpannableString spannableString_low;// 电量不足
    private SpannableString spannableString_pause;// 电量暂停

    private List<CarBatteryInfoEntity> tempList = new ArrayList<>();// 展示listView的数据源
    private List<CarBatteryInfoEntity> batteryInfoList = new ArrayList<>();// 所有小车的电量数据
    private List<CarBatteryInfoEntity> searchList = new ArrayList<>();// 保存对应id的小车数据（搜索后的数据）
    private AlertDialog dialog_battery;
    String str_tip = "";// 提示内容

    public CarBatteryInfoFragment() {
        // Required empty public constructor
    }

    private CarBatteryInfoAdapter adapter = null;
    private CarBatteryInfoAdapter searchAdapter = null;
    AlertDialog dialog;

    @SuppressLint("HandlerLeak")
    private Handler comingMessageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_CLOSE_CONNECTION:
                    interruptThread(thread_close_connection);
                    break;
                case WHAT_CAR_BATTERY_INFO:
                    byte[] bodyCarBatteryInfo = (byte[]) msg.obj;
                    Map<String, Object> mapCarBatteryInfo = (Map<String, Object>) toObject(bodyCarBatteryInfo);
                    if (mapCarBatteryInfo != null && mapCarBatteryInfo.size() != 0){
                        setCarBatteryInfo(mapCarBatteryInfo);
                        setTempData(mapCarBatteryInfo);
                    }

                    // 小车的总数量设置
                    if(batteryInfoList.size() == 0){
                        tv_carNumber.setText("暂无小车");
                    }else {
                        String strNumber = "小车数量总计 " + batteryInfoList.size() + " 台";

                        spannableString = new SpannableString(strNumber);
                        // 设置字体20dp，字体颜色为蓝色
                        spannableString.setSpan(new AbsoluteSizeSpan(12, true), strNumber.indexOf(" ") + 1,strNumber.indexOf(" ") + 1 + String.valueOf(batteryInfoList.size()).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), strNumber.indexOf(" ") + 1,strNumber.indexOf(" ") + 1 + String.valueOf(batteryInfoList.size()).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                        tv_carNumber.setText(spannableString);

                    }

                    // 电量数量显示
                    setBatteryNumber(batteryInfoList);

                    // listview设置adapter

                    if (!bl_isSearch){
                        if (orderList.size() != 0){
                            if (adapter == null){
//                            adapter = new CarBatteryInfoAdapter(getContext(), tempList);
                                adapter = new CarBatteryInfoAdapter(getContext(), orderList);
                                lsv_carBatteryInfo.setAdapter(adapter);
                            }else {
                                adapter.notifyDataSetChanged();
                            }
                        }

                    }else {
                        // 展示搜索的小车电量数据
                        if (searchList != null && searchList.size() != 0){
                            if (searchAdapter == null){
                                searchAdapter = new CarBatteryInfoAdapter(getContext(), searchList);
                                lsv_carBatteryInfo.setAdapter(searchAdapter);
                            }else {
                                searchAdapter.notifyDataSetChanged();
                            }
                        }else {
                            // 不符合搜索条件，不显示小车信息
                            searchAdapter = null;
                            searchAdapter = new CarBatteryInfoAdapter(getContext(), searchList);
                            lsv_carBatteryInfo.setAdapter(searchAdapter);
                        }
                    }

                    break;
            }
        }
    };

    /**
     * 小车电量数据显示
     * @param list
     */
    private void setBatteryNumber(List<CarBatteryInfoEntity> list) {
        if (list != null && list.size() != 0){
            int sum_fine = 0;// 良好
            int sum_crisis = 0;// 临界
            int sum_low = 0;// 电量不足
            int sum_pause = 0;// 暂停
            for (int i = 0;i < list.size();i++){
                long laveBattery = list.get(i).getLaveBattery();
                if(laveBattery <= 1000 && laveBattery >= 500){
                    sum_fine++;
                }else if(laveBattery < 500 && laveBattery >= 300){
                    sum_crisis++;
                }else if(laveBattery < 300 && laveBattery >= 100){
                    sum_low++;
                }else if(laveBattery < 100 && laveBattery >= 0){
                    sum_pause++;
                }
            }

            if (sum_crisis != 0){
                str_tip = "存在临界值电量小车，";

                if (sum_low != 0){
                    str_tip = str_tip + "\n" + "存在电量不足小车，";
                }

                if (sum_pause != 0){
                    str_tip = str_tip + "\n" + "存在电量暂停小车，";
                }

                if (dialog_battery == null){
                    dialog_battery = new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.app_icon)
                            .setTitle("提示")
                            .setCancelable(false)
                            .setMessage(str_tip + "\n" + "请注意查看小车的电量值情况")
                            .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();

                    dialog_battery.show();
                }

            }

            String str_fine = getResources().getString(R.string.str_battery_fine) + " " + sum_fine + " 台";
            LogUtil.e("str_fine","" + str_fine.length());
            spannableString_fine = new SpannableString(str_fine);
            // 设置字体16dp，字体颜色为黑色
            spannableString_fine.setSpan(new AbsoluteSizeSpan(12, true), str_fine.indexOf(" ") + 1,str_fine.indexOf(" ") + 1 + String.valueOf(sum_fine).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString_fine.setSpan(new ForegroundColorSpan(Color.BLACK), str_fine.indexOf(" ") + 1,str_fine.indexOf(" ") + 1 + String.valueOf(sum_fine).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

            String str_crisis = getResources().getString(R.string.str_battery_crisis) + " " + sum_crisis + " 台";
            spannableString_crisis = new SpannableString(str_crisis);
            // 设置字体16dp，字体颜色为黑色
            spannableString_crisis.setSpan(new AbsoluteSizeSpan(12, true), str_crisis.indexOf(" ") + 1,str_crisis.indexOf(" ") + 1 + String.valueOf(sum_crisis).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString_crisis.setSpan(new ForegroundColorSpan(Color.BLACK), str_crisis.indexOf(" ") + 1,str_crisis.indexOf(" ") + 1 + String.valueOf(sum_crisis).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

            String str_low = getResources().getString(R.string.str_battery_low) + " " + sum_low + " 台";
            spannableString_low = new SpannableString(str_low);
            // 设置字体16dp，字体颜色为黑色
            spannableString_low.setSpan(new AbsoluteSizeSpan(12, true), str_low.indexOf(" ") + 1,str_low.indexOf(" ") + 1 + String.valueOf(sum_low).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString_low.setSpan(new ForegroundColorSpan(Color.BLACK), str_low.indexOf(" ") + 1,str_low.indexOf(" ") + 1 + String.valueOf(sum_low).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

            String str_pause = getResources().getString(R.string.str_battery_pause) + " " + sum_pause + " 台";
            spannableString_pause = new SpannableString(str_pause);
            // 设置字体16dp，字体颜色为黑色
            spannableString_pause.setSpan(new AbsoluteSizeSpan(12, true), str_pause.indexOf(" ") + 1,str_pause.indexOf(" ") + 1 + String.valueOf(sum_pause).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString_pause.setSpan(new ForegroundColorSpan(Color.BLACK), str_pause.indexOf(" ") + 1,str_pause.indexOf(" ") + 1 + String.valueOf(sum_pause).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

            tv_battery_fine.setText(spannableString_fine);
            tv_battery_crisis.setText(spannableString_crisis);
            tv_battery_low.setText(spannableString_low);
            tv_battery_pause.setText(spannableString_pause);

        }
    }


    /**
     * 设置展示listview的数据源
     * @param mapCarBatteryInfo
     */
    private void setTempData(Map<String, Object> mapCarBatteryInfo) {
        try {
            long robotID = Long.parseLong(mapCarBatteryInfo.get("robotID").toString());
            long laveBattery = Long.parseLong(mapCarBatteryInfo.get("laveBattery").toString());
            long voltage = Long.parseLong(mapCarBatteryInfo.get("voltage").toString());

            CarBatteryInfoEntity entity = new CarBatteryInfoEntity();
            entity.setRobotID(robotID);
            entity.setLaveBattery(laveBattery);
            entity.setVoltage(voltage);

            if(tempList.size() == 0){
                tempList.add(entity);
            }else {

                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < tempList.size();i++){
                    if (tempList.get(i).getRobotID() == robotID){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                if(bl_isAdd && index != -1){
                    tempList.remove(index);
                    tempList.add(index, entity);
                }else {
                    if (tempList.size() < defaultLength){
                        tempList.add(entity);
                    }

                }

            }

            if (tempList.size() != 0 && tempList.size() != 1){
                orderList();// list数组按电量排序
            }

        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"小车电量信息数据异常");
        }
    }

    List<CarBatteryInfoEntity> orderList = new ArrayList<>();// 创建集合保存对实体对象某一属性进行大小排序后数据集
    /**
     * 根据电量值排序（由小到大）
     */
    private void orderList() {

        orderList = tempList;// 先将需要排序的实体对象集合赋值给orderList集合
        int len = orderList.size();// 长度
        for (int i = 0;i < len;i++){
            for (int j = 0;j < len - (i + 1);j++){
                // 将较大电量值的实体对象向后移动
                if (orderList.get(j).getLaveBattery() > orderList.get(j+1).getLaveBattery()){
                    // 获取j、j+1位置处的两个实体对象
                    CarBatteryInfoEntity entityJ = new CarBatteryInfoEntity();
                    entityJ.setRobotID(orderList.get(j).getRobotID());
                    entityJ.setLaveBattery(orderList.get(j).getLaveBattery());
                    entityJ.setVoltage(orderList.get(j).getVoltage());

                    CarBatteryInfoEntity entityJ1 = new CarBatteryInfoEntity();
                    entityJ1.setRobotID(orderList.get(j+1).getRobotID());
                    entityJ1.setLaveBattery(orderList.get(j+1).getLaveBattery());
                    entityJ1.setVoltage(orderList.get(j+1).getVoltage());

                    // 交换一下位置
                    orderList.remove(j);
                    orderList.add(j, entityJ1);

                    orderList.remove(j+1);
                    orderList.add(j+1, entityJ);
                }
            }

        }


    }


    /**
     * 设置小车电量信息
     * @param mapCarBatteryInfo
     */
    private void setCarBatteryInfo(Map<String, Object> mapCarBatteryInfo) {

        try {
            long robotID = Long.parseLong(mapCarBatteryInfo.get("robotID").toString());
            long laveBattery = Long.parseLong(mapCarBatteryInfo.get("laveBattery").toString());
            long voltage = Long.parseLong(mapCarBatteryInfo.get("voltage").toString());

            CarBatteryInfoEntity entity = new CarBatteryInfoEntity();
            entity.setRobotID(robotID);
            entity.setLaveBattery(laveBattery);
            entity.setVoltage(voltage);

            if(batteryInfoList.size() == 0){
                batteryInfoList.add(entity);
            }else {

                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < batteryInfoList.size();i++){
                    if (batteryInfoList.get(i).getRobotID() == robotID){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                if(bl_isAdd && index != -1){
                    batteryInfoList.remove(index);
                    batteryInfoList.add(index, entity);
                }else {
                    batteryInfoList.add(entity);
                }

            }


        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"小车电量信息数据异常");
        }

    }

    @OnClick(R.id.iv_fragment_back)
    public void doClick(View view){
        switch (view.getId()){
            case R.id.iv_fragment_back:// 返回
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car_battery_info, container, false);
        ButterKnife.bind(this, view);
        setListeners();
        loadMore();

        setTitleBar();

        return view;
    }

    /**
     * 设置标题栏样式
     */
    private void setTitleBar() {
        tv_fragment_title.setText("小车电量");
    }

    /**
     * 加载更多，每次新增10条
     */
    private void loadMore() {
        // 头部阻尼系数
        ptrFrameLayout.setResistanceHeader(1.7f);
        // 底部阻尼系数
        ptrFrameLayout.setResistanceFooter(1.7f);
        // 默认1.2f，移动达到头部高度1.2倍时触发刷新操作
        ptrFrameLayout.setRatioOfHeaderHeightToRefresh(1.2f);
        // 头部回弹时间
        ptrFrameLayout.setDurationToCloseHeader(1000);
        // 底部回弹时间
        ptrFrameLayout.setDurationToCloseFooter(1000);
        // 释放刷新
        ptrFrameLayout.setPullToRefresh(false);
        // 释放时恢复到刷新状态的时间
        ptrFrameLayout.setDurationToBackHeader(200);
        ptrFrameLayout.setDurationToBackFooter(200);

        // Matrial风格头部的实现
        final MaterialHeader header = new MaterialHeader(getContext());
        header.setPadding(0, PtrLocalDisplay.dp2px(15),0,0);
        ptrFrameLayout.setHeaderView(header);
        ptrFrameLayout.addPtrUIHandler(header);

        // 经典的底部布局实现
        PtrClassicDefaultFooter footer = new PtrClassicDefaultFooter(getContext());
        footer.setPadding(0, PtrLocalDisplay.dp2px(15),0,0);
        ptrFrameLayout.setFooterView(footer);
        ptrFrameLayout.addPtrUIHandler(footer);

        // 设置支持刷新和加载更多 可以任意开启或者关闭某一个特性（开关）
        ptrFrameLayout.setMode(PtrFrameLayout.Mode.LOAD_MORE);
        ptrFrameLayout.setPinContent(false);

        ptrFrameLayout.setPtrHandler(new PtrDefaultHandler2() {
            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {

                if(tempList.size() != 0){
                    if (tempList.size() == batteryInfoList.size()){
                        ToastUtil.showToast(getContext(),"数据已经全部加载");
                        ptrFrameLayout.refreshComplete();
                        return;
                    }
                }

                if(defaultLength + 10 <= batteryInfoList.size()){
                    defaultLength += 10;// 每次加载增加10
                }else {
                    defaultLength = batteryInfoList.size();
                }
                ptrFrameLayout.refreshComplete();
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {

                ptrFrameLayout.refreshComplete();
            }
        });
    }

    private boolean bl_isSearch = false;
    /**
     * 设置监听
     */
    private void setListeners() {

        // 实时监听小车电量信息变化
        setUpConnectionFactory();
        subscribeCarBatteryInfo(comingMessageHandler);

        // 搜索框的文本内容改变监听
        et_search_car.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())){
                    setSearchList(Integer.parseInt(s.toString()));
                    bl_isSearch = true;
                    adapter = null;
                    ptrFrameLayout.setMode(PtrFrameLayout.Mode.NONE);
                }else {
                    defaultLength = 20;//重设默认显示条数
                    if (tempList.size() > defaultLength){
                        List<CarBatteryInfoEntity> list = new ArrayList<>();
                        for (int i = 0;i < defaultLength;i++){
                            list.add(i, tempList.get(i));
                        }

                        if (list != null && list.size() != 0){
                            tempList.clear();
                            tempList = list;
                        }
                    }

                    bl_isSearch = false;
                    searchAdapter = null;
                    ptrFrameLayout.setMode(PtrFrameLayout.Mode.LOAD_MORE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * 根据id获取对应的小车信息
     * @param id
     */
    private void setSearchList(int id) {
        searchList.clear();
        int index = -1;
        for (int i = 0;i < batteryInfoList.size();i++){
            if (id == batteryInfoList.get(i).getRobotID()){
                index = i;
            }
        }
        if (index != -1){
            searchList.add(batteryInfoList.get(index));
        }
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

    /**
     * 小车电量信息消费线程
     * @param handler
     */
    private void subscribeCarBatteryInfo(final Handler handler){

        thread_car_battery_info = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 需要再次初始化数据的时候就关闭上一个连接
                    if(connection_car_battery_info != null){
                        connection_car_battery_info.close();
                    }
                    // 创建新的连接
                    connection_car_battery_info = factory.newConnection();
                    // 创建通道
                    Channel channel = connection_car_battery_info.createChannel();
                    // 处理完一个消息，再接收下一个消息
                    channel.basicQos(0, 1, false);

                    // 随机命名一个队列名称
                    String queueName = System.currentTimeMillis() + "QN_CAR_BATTERY";
//                    String queueName = "1522474994468queueNameCarBatteryInfo";
                    // 声明交换机类型
                    channel.exchangeDeclare(Constants.MQ_EXCHANGE_CAR_BATTERY, "direct", true);
                    // 声明队列（持久的、非独占的、连接断开后队列会自动删除）
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);// 声明共享队列
                    // 根据路由键将队列绑定到交换机上（需要知道交换机名称和路由键名称）
                    channel.queueBind(q.getQueue(), Constants.MQ_EXCHANGE_CAR_BATTERY, Constants.MQ_ROUTINGKEY_CAR_BATTERY);
//                    channel.queueBind(queueName, Constants.MQ_EXCHANGE_CAR_BATTERY, Constants.MQ_ROUTINGKEY_CAR_BATTERY);
                    // 创建消费者获取rabbitMQ上的消息。每当获取到一条消息后，就会回调handleDelivery（）方法，该方法可以获取到消息数据并进行相应处理
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            // 发消息通知UI更新
                            Message message = handler.obtainMessage();
//                            Bundle bundle = new Bundle();
//                            bundle.putByteArray("body", body);
                            message.what = WHAT_CAR_BATTERY_INFO;
//                            message.obj = bundle;
                            message.obj = body;
                            handler.sendMessage(message);
                        }
                    };
                    channel.basicConsume(q.getQueue(), true, consumer);
//                    channel.basicConsume(queueName, true, consumer);

                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        thread_car_battery_info.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        interruptThread(thread_car_battery_info);
        closeConnection(connection_car_battery_info);
        // 移除所有的回调和消息，防止Handler泄露
        comingMessageHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 关闭连接
     * @param connection
     */
    private void closeConnection(final Connection connection) {
        thread_close_connection = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection != null){
                        connection_car_battery_info.close();
                        connection_car_battery_info = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message message = comingMessageHandler.obtainMessage();
                message.what = WHAT_CLOSE_CONNECTION;
                comingMessageHandler.sendMessage(message);
            }
        });
        thread_close_connection.start();
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

    /**
     * 将byte数组转化为Object对象
     * @return
     */
    private Object toObject(byte[] bytes){
        Object object = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);// 创建ByteArrayInputStream对象
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);// 创建ObjectInputStream对象
            object = objectInputStream.readObject();// 从objectInputStream流中读取一个对象
            byteArrayInputStream.close();// 关闭输入流
            objectInputStream.close();// 关闭输入流
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;// 返回对象
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            setTitleBar();
        }
    }
}
