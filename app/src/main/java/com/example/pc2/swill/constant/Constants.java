package com.example.pc2.swill.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC-2 on 2018/1/23.
 * 保存一些常量
 */

public class Constants {

    public static int MAP_ROWS = 0;// 地图的行数
    public static int MAP_COLUMNS = 0;// 地图的列数

    // 费舍尔定制 非存储位货架声明及初始化
    public static final List<Integer> unStoragePodsList = new ArrayList<>();

    // https://api.mushiny.com/wcs/checkRobotStatus?sectionId=ec229eb7-7e2b-43a8-b1c7-91bd807e91cf&robotId=6
//    public static final String HTTP = "https://";// https请求 源汇版
    public static final String HTTP = "http://";// http请求：speedlog
//    public static String ROOT_ADDRESS = "api.mushiny.com";// 接口地址 源汇版
//    public static String ROOT_ADDRESS = "192.168.0.72:22009";// 接口地址：费舍尔版
//    public static String ROOT_ADDRESS = "192.168.10.211:12009";// 接口地址：美的版
    public static String ROOT_ADDRESS = "192.168.1.202:12009";// 接口地址：speedlog

    public static final int DEFAULT_BOX_SIZE = 45;// 设置地图绘制时格子的默认大小

    // EXCHANGE的值由exchange_begin和SECTION_RCS_ID组成，即exchange_begin + SECTION_RCS_ID
    public static String EXCHANGE;// 仓库初始化成功后，所选地图对应的交换机名称
    public static String exchange_begin = "section";// 交换机名称不变的部分
    public static String SECTION_RCS_ID = "";// sectionRcsId

    public static String SECTIONID = "";// sectionId，即wcs接口中sectionId参数的值

    public static String WAREHOUSEID = "";// 仓库的id，也即仓库名称。例如：JN1

    // RabbitMQ连接所需信息
//    public static String MQ_HOST = "52.83.227.228";// MQ主机地址 源汇版
//    public static String MQ_HOST = "192.168.0.72";// 主机地址：费舍尔版
//    public static String MQ_HOST = "192.168.10.212";// 主机地址：美的版
//    public static String MQ_HOST = "123.249.28.97";// 外网主机地址：speedlog
    public static String MQ_HOST = "192.168.1.202";// 内网主机地址：speedlog
    public static int MQ_PORT = 5672;// MQ端口号：源汇版、费舍尔版、美的版、speedlog
//    public static String MQ_USERNAME = "nt_yuan_hui";// MQ用户名 源汇版
    public static String MQ_USERNAME = "mushiny";// 用户名：费舍尔版、美的版、speedlog
//    public static String MQ_PASSWORD = "yhYHzaq";// MQ密码 源汇版
    public static String MQ_PASSWORD = "mushiny";// 密码：费舍尔版、美的版、speedlog

    public static final String MQ_ROUTINGKEY_MAP = "WCS_RCS_MAP_RESPONSE";// 地图的路由键测试
    //    public static final String MQ_EXCHANGE_MAP = "section1";// 地图的交换机测试
    public static final String MQ_ROUTINGKEY_MAP_REQUEST = "RCS_WCS_MAP_REQUEST";// 地图请求的对应的路由键名称

    // 小车实时包的交换机名称和路由键
//    public static final String MQ_EXCHANGE_CAR = "section1";
    public static final String MQ_QUEUE_CAR = "RCS_WCS_ROBOT_RT_MD";
//    public static final String MQ_ROUTINGKEY_CAR = "RCS_WCS_ROBOT_RT_MD";
    public static final String MQ_ROUTINGKEY_CAR = "RCS_WCS_ROBOT_RT";

    // 仓库和地图初始化
    public static final String MQ_EXCHANGE_STORAGEMAP = "ANY_WAREHOUSE_INIT";// 交换机名称
    public static final String MQ_ROUTINGKEY_STORAGEMAP_REQUEST = "ANY_WCS_WAREHOUSE_INIT_REQUEST";// 发布消息所绑定的路由键
    public static final String MQ_ROUTINGKEY_STORAGEMAP_RESPONSE = "WCS_ANY_WAREHOUSE_INIT_RESPONSE";// 获取消息所绑定的路由键
    public static final String MQ_QUEUENAME_STORAGEMAP_RESPONSE = "WCS_ANY_WAREHOUSE_INIT_RESPONSE";// MQ固定队列名

    public static final String MQ_ROUTINGKEY_CARPATH = "RCS_WCS_RESPONSE_ALL_AGV_INFO";// 获取小车锁格和尚未锁格的路径消息的路由键

    // 所有充电桩的充电任务信息
    public static final String MQ_EXCHANGE_CHARGINGPILE = "WCS_ANY_CHARGE_ORDER";
    public static final String MQ_ROUTINGKEY_CHARGINGPILE = "WCS_ANY_CHARGE_ORDER";

    // 锁格和解锁
    public static final String MQ_ROUTINGKEY_LOCK_UNLOCK = "WCS_RCS_UPDATE_CELLS";

    // 小车的电量实时显示
    public static final String MQ_EXCHANGE_CAR_BATTERY = "WCS_ANY_ROBOT_STATUS";
    public static final String MQ_ROUTINGKEY_CAR_BATTERY = "WCS_ANY_ROBOT_STATUS";

    // 小车扫不到pod
//    public static final String MQ_EXCHANGE_PROBLEM_FEEDBACK = "";
    public static final String MQ_ROUTINGKEY_PROBLEM_FEEDBACK = "RCS_WCS_ROBOT_ERROR";

    // 小车位置不改变超时
//    public static final String MQ_EXCHANGE_NOMOVE_TIMEOUT = "";
    public static final String MQ_ROUTINGKEY_NOMOVE_TIMEOUT = "RCS_WCS_AGV_NOMOVE_TIMEOUT";

    // 小车连接断开
    public static final String MQ_ROUTINGKEY_CLOSE_CONNECTION = "RCS_WCS_ROBOT_CLOSE_CONNECTION";

    // 清除小车所有路径
    public static final String MQ_ROUTINGKEY_CLEAR_PATH = "WCS_RCS_CLEAR_PATH_FOR_POSITION_NO_CHANGING";

    // 小车充电故障监听
    public static final String MQ_ROUTINGKEY_CHARGING_ERROR = "MAP_CHARGER_BOARD";

    // 清除充电桩故障
    public static final String MQ_ROUTINGKEY_CHARGING_PILE_CLEAR_ERROR = "RCS_CHARGING_PILE_CLEAR_ERROR";

    // 小车路径调度
    public static final String MQ_ROUTINGKEY_AGV_SERIESPATH = "WCS_RCS_AGV_SERIESPATH";

    // 改变货架的位置（将位于通道的货架更新到地图上，防止重车撞货架）
    public static final String MQ_ROUTINGKEY_CHANGING_POD_POSITION = "WCS_RCS_CHANGING_POD_POSITION";

    // 所有充电桩的状态
    public static final String MQ_ROUTINGKEY_CHARGING_PILE_WCS_CHARGERS_INFO_RESPONSE = "CHARGING_PILE_WCS_CHARGERS_INFO_RESPONSE";

    // 小车心跳包监听
    public static final String MQ_ROUTINGKEY_ROBOT_HEART_BEAT= "RCS_WCS_ROBOT_HEART_BEAT";
}
