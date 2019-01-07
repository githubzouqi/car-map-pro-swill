package com.example.pc2.media.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.pc2.media.R;
import com.example.pc2.media.constant.Constants;
import com.example.pc2.media.entity.BoxEntity;
import com.example.pc2.media.entity.CarCurrentPathEntity;
import com.example.pc2.media.entity.ChargingPileEntity;
import com.example.pc2.media.entity.ChargingTaskEntity;
import com.example.pc2.media.entity.PodEntity;
import com.example.pc2.media.entity.RobotEntity;
import com.example.pc2.media.utils.DensityUtil;
import com.example.pc2.media.utils.LogUtil;
import com.example.pc2.media.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by PC-2 on 2018/1/23.
 */

public class BoxSurfaceView extends SurfaceView implements SurfaceHolder.Callback2, Runnable{

    private int defaultBoxSize;

    private Paint paint, paintNumber, paintRobotCarId, paintPodId;
    private int row = 0;// 行
    private int column = 0;// 列
    private int boxSize;// 一个格子的大小
    private int dynamicHeight;// 方格图的动态高度，画行时用
    private int dynamicWidth;// 方格图的动态宽度，画列时用

    private int boxTotalWidth;// 方格图总宽度
    private int boxTotalHeight;// 方格图总高度
    private int offsetX, offsetY;// 手势移动的坐标偏移量
    private BoxEntity boxEntity = null;// 实时格子信息实体类

    private Paint paintCircle;// 圆形画笔
    private Paint paintCarRoundRect;// 小车绘制（圆角矩形描边）

    private Paint paintRect;// 矩形画笔

    private List<PodEntity> podList = null;// pod信息集合
    private List<RobotEntity> carList = null;// 小车信息集合
    private List<Long> unWalkedList = null;// 不可走区域坐标点的集合
    private List<Integer> workStackList = null;// 停止点坐标集合，可用来标识出工作栈
    private List<List<Long>> rotateList = null;// 旋转区的坐标集，与每一个停止点相关联
    private List<Long> storageList = null;// 存储区坐标集，用来标识存储区域
    private Map<String, String> map_work_site_uuid = null;// 存储工作站的uuid，key对应的是停止点的坐标
    private List<ChargingPileEntity> chargingPileList = null;// 存储充电桩的数据
    private List<CarCurrentPathEntity> carCurrentPathEntityList = null;// 小车当前路径信息（锁格和未锁格）
    private List<Object> allLockedAreaList = null;// 地图上所有的锁格区域地标
    private List<Object> manualLockList = null;// 地图上所有手动锁格的区域坐标（不包含小车的锁格坐标）
    private List<ChargingTaskEntity> chargingTaskEntityList = null;// 所有的充电任务
    private List<Integer> lock_unlock_pos = null;// 用户选择的锁格或者解锁区域

    private Map<Long, List<Long>> carRouteMap = null;// 小车路径信息

    private Paint paintUnWalkedRect;// 不可走区域的颜色填充
    private Paint paintUnWalkedNumber;// 不可走区域的坐标

    private Paint paintRotateRect;// 旋转区的颜色填充
    private Paint paintRotateNumber;// 旋转区的坐标

    private int boxSizeChange = 0;// 按钮动态设置地图大小
    private Paint paintPodSide;// pod面所在点

    private Paint paintStorageRect;// 存储区域填充
    private Paint paintStorageNumber;// 存储区域坐标

    private Paint paintRouteRect;// 小车当前路径区域填充
    private Paint paintRouteNumber;// 小车当前路径区域坐标

    private Paint paintCarPathLock;// 小车当前路径锁格
    private Paint paintCarPathAll;// 小车当前路径未锁格和锁格

    private Paint paintChargeCarId;// 充电桩上显示的小车id

    private Paint paintLockUnLock;// 用户选择的锁格或解锁区域

    // 将图片资源转成bitmap
    Bitmap bitmap_charger = null;
    Bitmap bitmap = null;
    private String sectionId = null;// 当前地图的uuid

    public BoxSurfaceView(Context context) {
        super(context);
        init();
    }

    public BoxSurfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxSurfaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mSurfaceHolder = getHolder();// 获取SurfaceHolder对象
        mSurfaceHolder.addCallback(this);// 注册SurfaceHolder
        setFocusable(true);
        setFocusableInTouchMode(true);
//        this.setZOrderOnTop(true);
//        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        this.setKeepScreenOn(true);// 设置屏幕常亮


        // 创建Paint对象
        paint = new Paint();
        // 设置paint的颜色
        paint.setColor(Color.BLACK);
        // 设置连接点处的样式
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        // 设置笔帽的样式
        paint.setStrokeCap(Paint.Cap.ROUND);
        // 设置笔画的宽度
        paint.setStrokeWidth(1);

        setBoxSize();

        // 绘制地图坐标的画笔样式设置
        paintNumber = new Paint();
        paintNumber.setColor(Color.BLACK);
        paintNumber.setTextSize(boxSize/5);
        paintNumber.setAntiAlias(true);
        paintNumber.setStyle(Paint.Style.FILL);
        paintNumber.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintNumber.setStrokeWidth(1);

        // 不可走区域填充
        paintUnWalkedRect = new Paint();
        paintUnWalkedRect.setColor(getResources().getColor(R.color.unWalkedCell));
        paintUnWalkedRect.setAntiAlias(true);
        paintUnWalkedRect.setStyle(Paint.Style.FILL_AND_STROKE);

        // 不可走区域的坐标
        paintUnWalkedNumber = new Paint();
        paintUnWalkedNumber.setColor(Color.BLACK);
        paintUnWalkedNumber.setStyle(Paint.Style.FILL);
        paintUnWalkedNumber.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintUnWalkedNumber.setStrokeWidth(1);
        paintUnWalkedNumber.setTextSize(boxSize/5);

        // 存储区区域填充
        paintStorageRect = new Paint();
        paintStorageRect.setColor(getResources().getColor(R.color.storage));
        paintStorageRect.setAntiAlias(true);
        paintStorageRect.setStyle(Paint.Style.FILL_AND_STROKE);

        // 存储区区域的坐标
        paintStorageNumber = new Paint();
        paintStorageNumber.setColor(Color.BLACK);
        paintStorageNumber.setStyle(Paint.Style.FILL);
        paintStorageNumber.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintStorageNumber.setStrokeWidth(1);
        paintStorageNumber.setTextSize(boxSize/5);

        // 旋转区域填充
        paintRotateRect = new Paint();
        paintRotateRect.setColor(getResources().getColor(R.color.rotate));
        paintRotateRect.setAntiAlias(true);
        paintRotateRect.setStyle(Paint.Style.FILL_AND_STROKE);

        // 旋转区域的坐标
        paintRotateNumber = new Paint();
        paintRotateNumber.setColor(Color.WHITE);
        paintRotateNumber.setStyle(Paint.Style.FILL);
        paintRotateNumber.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintRotateNumber.setStrokeWidth(1);
        paintRotateNumber.setTextSize(boxSize/5);

        // 小车当前路径区域填充
        paintRouteRect = new Paint();
//        paintRouteRect.setColor(getResources().getColor(R.color.carRoute));
        paintRouteRect.setAntiAlias(true);
        paintRouteRect.setStyle(Paint.Style.FILL_AND_STROKE);

        // 小车当前路径坐标
        paintRouteNumber = new Paint();
        paintRouteNumber.setColor(Color.BLACK);
        paintRouteNumber.setStyle(Paint.Style.FILL);
        paintRouteNumber.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintRouteNumber.setStrokeWidth(1);
        paintRouteNumber.setTextSize(boxSize/5);

        // 绘制小车的id
        paintRobotCarId = new Paint();
        paintRobotCarId.setTextSize(boxSize/6);// 大小20px
        paintRobotCarId.setStyle(Paint.Style.FILL_AND_STROKE);// 小车的id字体加粗
        paintRobotCarId.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintRobotCarId.setStrokeWidth(1);
        // 绘制小车为圆形
        paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCircle.setAntiAlias(true);// 设置边缘平滑化
        paintCircle.setColor(Color.BLACK);
        // 小车，圆角矩形画笔
        paintCarRoundRect = new Paint();
        paintCarRoundRect.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCarRoundRect.setAntiAlias(true);
        paintCarRoundRect.setColor(getResources().getColor(R.color.car));

        // 绘制pod的id
        paintPodId = new Paint();
        paintPodId.setTextSize(boxSize/6);// 大小20px
        paintPodId.setStyle(Paint.Style.FILL);
        paintPodId.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintPodId.setStrokeWidth(1);
        // 绘制pod为矩形
        paintRect = new Paint();
        paintRect.setAntiAlias(true);
        paintRect.setColor(getResources().getColor(R.color.pod));
        paintRect.setStyle(Paint.Style.FILL_AND_STROKE);
        // pod的面，现在以三个点标识
        paintPodSide = new Paint();
        paintPodSide.setAntiAlias(true);
        paintPodSide.setColor(Color.GREEN);
        paintPodSide.setStyle(Paint.Style.FILL_AND_STROKE);

        // 小车当前路径（锁格和未锁格）
        paintCarPathLock = new Paint();
        paintCarPathLock.setAntiAlias(true);
        paintCarPathLock.setColor(getResources().getColor(R.color.color_carLock));
        paintCarPathLock.setStyle(Paint.Style.FILL_AND_STROKE);

        paintCarPathAll = new Paint();
        paintCarPathAll.setAntiAlias(true);
        paintCarPathAll.setColor(getResources().getColor(R.color.color_carall));
        paintCarPathAll.setStyle(Paint.Style.FILL_AND_STROKE);

        // 充电桩要来充电的小车id
        paintChargeCarId = new Paint();
        paintChargeCarId.setTextSize(boxSize/4);// 大小20px
        paintChargeCarId.setStyle(Paint.Style.FILL_AND_STROKE);
        paintChargeCarId.setAntiAlias(true);// 设置画笔的边缘平滑化
        paintChargeCarId.setStrokeWidth(1);
        paintChargeCarId.setTextAlign(Paint.Align.CENTER);
        paintChargeCarId.setColor(Color.WHITE);

        // 用户选择的锁格或解锁区域
        paintLockUnLock = new Paint();
        paintLockUnLock.setAntiAlias(true);
        paintLockUnLock.setColor(Color.BLACK);
        paintLockUnLock.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * 初始化变量并设置格子大小
     */
    private void setBoxSize() {
        // 初始化
        dynamicHeight = 0;
        dynamicWidth = 0;

        defaultBoxSize = DensityUtil.dp2px(getContext(), Constants.DEFAULT_BOX_SIZE);// 格子默认大小
        if(boxSizeChange != 0){
            boxSize = DensityUtil.dp2px(getContext(), boxSizeChange);// 将dp换算成px。
        }else {
            boxSize = defaultBoxSize;// 将dp换算成px。现在默认是一个格子大小50dp
        }

        // 设置网格的宽和高
        boxTotalWidth = boxSize * column;
        boxTotalHeight = boxSize * row;
    }

    /**
     * 测量view的大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtil.e("Demo", "onMeasure");
        if(boxSizeChange != 0){
            boxSize = DensityUtil.dp2px(getContext(), boxSizeChange);
        }else {
            boxSize = defaultBoxSize;
        }
        // 设置网格的宽和高
        boxTotalWidth = boxSize * column;
        boxTotalHeight = boxSize * row;

        int width_mode = MeasureSpec.getMode(widthMeasureSpec);
        int width_size = MeasureSpec.getSize(widthMeasureSpec);

        int height_mode = MeasureSpec.getMode(heightMeasureSpec);
        int height_size = MeasureSpec.getSize(heightMeasureSpec);

        if(width_mode == MeasureSpec.AT_MOST && height_mode == MeasureSpec.AT_MOST){

//            setMeasuredDimension(boxTotalWidth + 1, boxTotalHeight + 1);
            setMeasuredDimension(boxTotalWidth + 2*boxSize + 1, boxTotalHeight + 2*boxSize + 1);
        }else if(width_mode == MeasureSpec.AT_MOST){

//            setMeasuredDimension(boxTotalWidth + 1, height_size);
            setMeasuredDimension(boxTotalWidth + 2*boxSize + 1, height_size);
        }else if(height_mode == MeasureSpec.AT_MOST){

//            setMeasuredDimension(width_size, boxTotalHeight + 1);
            setMeasuredDimension(width_size, boxTotalHeight + 2*boxSize + 1);
        }
        LogUtil.e("boxTotalWidth", "" + (boxTotalWidth + 2*boxSize + 1));
        LogUtil.e("boxTotalHeight", "" + (boxTotalHeight + 2*boxSize + 1));

    }

    private Rect rect = new Rect();// 矩形区域
    private Rect rect_charge_pile = new Rect();// 充电桩
    private Rect rect_work_site = new Rect();// 工作站
    private RectF rectF_car = new RectF();// 小车的圆角边框
    private Rect rectMap = new Rect();// 地图的外边框
    private Rect rectCarCurrentPath = new Rect();// 小车当前路径
    private List<Map<String, Object>> list_charge_pile = new ArrayList<>();// 保存了充电桩的位置和充电桩的uuid数据

    /**
     * 重新设置绘制数字画笔的大小
     */
    private void reSetPaintSize() {
        paintNumber.setTextSize(boxSize/5);
        paintUnWalkedNumber.setTextSize(boxSize/5);
        paintStorageNumber.setTextSize(boxSize/5);
        paintRotateNumber.setTextSize(boxSize/5);
        paintRouteNumber.setTextSize(boxSize/5);
        paintRobotCarId.setTextSize(boxSize/6);
        paintPodId.setTextSize(boxSize/6);
        paintChargeCarId.setTextSize(boxSize/4);
    }

    private int downX = 0, downY = 0;// 手机按下屏幕时的x、y坐标

    private int dX = 0, dY = 0;
    /**
     * 设置view超出手机屏幕后可以滑动
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取滑动的动作
        int action = event.getAction();

        switch (action){
            // 当手势按下的时候
            case MotionEvent.ACTION_DOWN:

                //获取手指当前触摸时的坐标
                downX = (int)event.getRawX();
                downY = (int)event.getRawY();

                dX = (int)event.getRawX();
                dY = (int)event.getRawY();
                break;

            // 当手势滑动的时候
            case MotionEvent.ACTION_MOVE:

                // 滑动时实时获取手指触屏的坐标
                int moveX = (int) event.getRawX();
                int moveY = (int) event.getRawY();

                offsetX = moveX - downX;
                offsetY = moveY - downY;

                // 手势移动就进行view的移动
                if(Math.abs(offsetX) > 0 || Math.abs(offsetY) > 0){

                    moveView(offsetX, offsetY);
                }
                // 将downX、downY重新赋值
                downX = moveX;
                downY = moveY;
                break;

            case MotionEvent.ACTION_UP:// 手势抬起时候

                // 根据坐标偏移量判断手指是否进行了移动
                int varX = (int) (event.getRawX() - dX);
                int varY = (int) (event.getRawY() - dY);

                if(Math.abs(varX) == 0 && Math.abs(varY) == 0){
                    // 相对于view左上角的相对坐标
                    int up_x = (int) event.getX();
                    int up_y = (int) event.getY();

                    // 根据up_x和up_y确定点击的是绘制出来view上的第几个格子。注意：这里点击的是地图上的格子
                    int boxNo = sureBoxNo(up_x, up_y);
                    LogUtil.e("ACTION_UPNo","" + boxNo);

                    int stopPos = sureStopPos(up_x, up_y);// 根据up_x和up_y确定点击的工作站所对应的停止点的坐标

                    boolean bl_isWorkSite = false;// false表示点击处没有工作站
                    String workSiteUUID = "";
                    if(stopPos != 0){
                        // 接下来需要确定是否点击了工作站，然后获取点击的工作站的uuid，释放pod时需要用到pod的uuid
                        if(map_work_site_uuid != null){
                            Iterator<String> iterator = map_work_site_uuid.keySet().iterator();
                            while (iterator.hasNext()){
                                String key = iterator.next();
                                if(Integer.parseInt(key) == stopPos){// 是否点击了工作站
                                    bl_isWorkSite = true;// 置true，点击的是工作站
                                    workSiteUUID = map_work_site_uuid.get(key);// 获取工作站的uuid
                                }
                            }
                        }
                    }

                    // 回调监听(确保在绘制的地图区域内)
                    if(listener != null){
                        if((up_x > boxSize && up_x < boxSize + column * boxSize) && (up_y > boxSize && up_y < boxSize + row * boxSize)){
                            if(!TextUtils.isEmpty(workSiteUUID)){
                                // 此时工作站在地图内且点击了工作站
                                listener.workSiteClick(bl_isWorkSite, workSiteUUID);
                            }else {
                                listener.doClick(boxNo, carList, podList, unWalkedList);
                            }
                        }else {
                            // 此时工作站在不在地图内
                            listener.workSiteClick(bl_isWorkSite, workSiteUUID);
                        }

                    }
                }
                break;



        }

        LogUtil.e("MAP_STOP_WORK",map_stop_workSite.toString());

        // 返回父类的方法
        return true;
//        return super.onTouchEvent(event);
    }


    private Map<Integer, Integer> map_stop_workSite = new HashMap<>();// 创建map，key是停止点坐标，key对应的value是工作站坐标
    /**
     * 确定工作栈对应的停止点坐标。坐标获取后还需要判断是否点击了工作站
     * @param up_x
     * @param up_y
     * @return
     */
    private int sureStopPos(int up_x, int up_y) {
        int stop_Pos = 0;

        // 工作站在地图内时
        if((up_x > boxSize && up_x < (column + 1)*boxSize) && (up_y > boxSize && up_y < (row + 1)*boxSize)){

            int workSitePos = sureBoxNo(up_x, up_y);
            if(!TextUtils.isEmpty(map_stop_workSite.toString())){
                Iterator<Integer> iterator = map_stop_workSite.keySet().iterator();
                while (iterator.hasNext()){
                    int key = iterator.next();
                    int value = map_stop_workSite.get(key);
                    if(value == workSitePos){
                        stop_Pos = key;
                    }
                }
            }

        }else {// 工作站不在地图内时
            if(up_y > 0 && up_y < boxSize){// 工作站在地图的上侧
                int r = up_x / boxSize;
                if(r <= column){
                    stop_Pos = r;
                }

            }
            if(up_y > (row + 1)*boxSize && up_y < (row + 2)*boxSize){// 工作站在地图的下侧
                int r = up_x / boxSize;
                if(r <= column){
                    stop_Pos = (row - 1)*column + r;
                }
            }
            if(up_x > 0 && up_x < boxSize){// 工作站在地图的左侧
                int c = up_y / boxSize;
                if(c <= row){
                    stop_Pos = (c - 1)*column + 1;
                }
            }
            if(up_x > (column + 1)*boxSize && up_x < (column + 2)*boxSize){// 工作站在地图的右侧
                int c = up_y / boxSize;
                if(c <= row){
                    stop_Pos = (c -1)*column + column;
                }
            }
        }

        return stop_Pos;
    }

    private int oldMoveLeft, oldMoveTop;// 记录view移动后的位置信息
    /**
     * 当view的大小发生变化时，会回调该方法
     * @param w 变化后view的宽
     * @param h 变化后view的高
     * @param oldw 变化前view的宽
     * @param oldh 变化前view的高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int newMoveLeft,newMoveTop;
        if(oldMoveLeft == 0){
            newMoveLeft = 0;
        }else {
            float newMoveLeft_float = (((float)w / (float)oldw) * oldMoveLeft);
            newMoveLeft = (int) newMoveLeft_float;
        }
        if(oldMoveTop == 0){
            newMoveTop = 0;
        }else {
            float newMoveTop_float = ((float) h / (float) oldh) * oldMoveTop;
            newMoveTop = (int) newMoveTop_float;
        }
        this.layout(newMoveLeft, newMoveTop, w + newMoveLeft, h + newMoveTop);// 当地图放大或者缩小时，保持在移动后的位置处放大或缩小
    }

    /**
     * 根据地图上的坐标来获取所在行数
     * @param pos
     * @return
     */
    private int getRowWithPos(int pos){
        if(pos % column == 0){
            return pos/column;
        }else {
            return pos/column + 1;
        }
    }

    /**
     * 根据地图上的坐标来获取所在列数
     * @param pos
     * @return
     */
    private int getColumnithPos(int pos){

        if(pos % column == 0){
            return column;
        }else {
            return pos%column;
        }
    }


    /**
     * 根据点击了view上的相对坐标确定点击的是第几个格子
     * @param up_x
     * @param up_y
     * @return
     */
    private int sureBoxNo(int up_x, int up_y) {

        // 根据up_y确定格子在第几行
        int box_row = up_y / boxSize;
        // 根据up_x确定格子在第几列
        int box_column = up_x / boxSize;
        // 根据行数和列数来确定点击的是第几个格子
        return (box_row - 1) * column + box_column;
    }

    /**
     * 移动view的方法
     * @param offsetX
     * @param offsetY
     */
    private void moveView(int offsetX, int offsetY) {

        if(offsetX == 0 && offsetY == 0){

            // 手指没有移动，不进行view的滑动
            return;
        }else {

            int l = this.getLeft() + offsetX;
            int r = this.getRight() + offsetX;
            int b = this.getBottom() + offsetY;
            int t = this.getTop() + offsetY;

            oldMoveLeft = l;
            oldMoveTop = t;

            // 重新布局显示滑动后的view
            this.layout(l, t, r, b);
        }
    }

    // 设置行数和列数
    public void setRowAndColumn(int row, int column, int boxSizeChange) {

        synchronized (mSurfaceHolder){
            isDrawing = false;
            if(mapSizeThread != null){
                mapSizeThread.interrupt();
                mapSizeThread = null;
            }
            this.row = row;
            this.column = column;
            this.boxSizeChange = boxSizeChange;

            // 当仓库和地图重新选择后重绘地图时，需要重置view的位置信息
            oldMoveLeft = 0;
            oldMoveTop = 0;

            // 当view确定自身已经不再适合现有的区域时，
            // 该view本身调用这个方法要求parent view（父类的视图）重新调用他的onMeasure onLayout来重新设置自己位置。
            // 特别是当view的layoutparameter发生改变，并且它的值还没能应用到view上时，这时候适合调用这个方法。
            requestLayout();
        }


    }


    /**
     * 初始化地图时设置货架信息
     * @param podList 货架
     * @param unWalkedList 不可走区域
     * @param workStackList 停止点坐标集合，可用来标识出工作站
     * @param rotateList 旋转区
     * @param storageList 存储区
     * @param map_work_site_uuid 工作站uuid的map，key对应的是停止点的坐标
     * @param chargingPileList 充电桩
     */
    public void setPodData(List<PodEntity> podList,
                           List<Long> unWalkedList,
                           List<Integer> workStackList,
                           List<List<Long>> rotateList,
                           List<Long> storageList,
                           Map<String, String> map_work_site_uuid,
                           List<ChargingPileEntity> chargingPileList) {
        isDrawing = false;
        synchronized (mSurfaceHolder){
            this.podList = podList;
            this.unWalkedList = unWalkedList;
            this.workStackList = workStackList;
            this.rotateList = rotateList;
            this.storageList = storageList;
            this.map_work_site_uuid = map_work_site_uuid;
            this.chargingPileList = chargingPileList;
            isDrawing = true;
        }

//        invalidate();
    }

    /**
     * 实时设置小车的信息并显示
     * @param carList
     */
    public void setCarAndPodData(List<RobotEntity> carList, List<PodEntity> podList) {
        isDrawing = false;
        synchronized (mSurfaceHolder){
            this.carList = carList;
            this.podList = podList;
        }

//        invalidate();
    }

    /**
     * 地图复原
     */
    public void reset() {
        // 地图复原后，自定义view恢复到手机屏幕的左上角，需要重置view的位置信息
        oldMoveLeft = 0;
        oldMoveTop = 0;
        requestLayout();
    }

    /**
     * 放大或者缩小地图
     * @param boxSizeChange
     */
    public void zoomInOut(int boxSizeChange) {
            this.boxSizeChange = boxSizeChange;
            requestLayout();// 重新回调onMeasure方法计算布局的大小

//        invalidate();// 进行重绘
    }

    /**
     * 小车的所有路径（可以是一辆，也可以是多辆）
     * @param carRouteMap
     */
    public void setCarRouteData(Map<Long, List<Long>> carRouteMap) {
        this.carRouteMap = carRouteMap;
//        invalidate();
    }

    /**
     * 所有小车的当前路径
     * @param carCurrentPathEntityList 包含所有车的锁格和未锁格区域信息
     * @param allLockedAreaList 所有的锁格信息（也包括车的和其他的锁格区域）
     */
    public void setCarCurrentPath(List<CarCurrentPathEntity> carCurrentPathEntityList,
                                  List<Object> allLockedAreaList,
                                  List<Object> manualLockList) {
        isDrawing = false;
        synchronized (mSurfaceHolder){
            this.carCurrentPathEntityList = carCurrentPathEntityList;
            this.allLockedAreaList = allLockedAreaList;
            this.manualLockList = manualLockList;
            isDrawing = true;
        }

//        invalidate();
    }

    /**
     * 所有充电任务数据
     * @param chargingTaskEntityList
     * @param sectionId 当前地图的
     */
    public void setChargeData(List<ChargingTaskEntity> chargingTaskEntityList, String sectionId) {
        isDrawing = false;
        synchronized (mSurfaceHolder){
            this.chargingTaskEntityList = chargingTaskEntityList;
            this.sectionId = sectionId;
            isDrawing = true;
        }

//        invalidate();
    }

    /**
     * 用户选择锁格或者解锁区域
     * @param lock_unlock_pos
     */
    public void setLockUnLockArea(List<Integer> lock_unlock_pos) {
        isDrawing = false;
        synchronized (mSurfaceHolder){
            this.lock_unlock_pos = lock_unlock_pos;
            isDrawing = true;
        }

    }

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }

    private OnClickListener listener = null;


    public interface OnClickListener {
        void doClick(int boxNo, List<RobotEntity> carList, List<PodEntity> podList, List<Long> unWalkedList);// 点击事件
        void workSiteClick(boolean bl_isWorkSite, String workSiteUUID);// 点击了工作站
    }

    private int testL = -1, testT = -1, testL1 = -1, testT1 = -1;
    private SurfaceHolder mSurfaceHolder;// 声明SurfaceHolder对象
    private Canvas mapSizeCanvas;// 声明Canvas对象
    private boolean isDrawing;// 循环绘制的布尔变量

    private Thread mapSizeThread;// 控制地图大小线程

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        LogUtil.e("Demo","surfaceRedrawNeeded");
    }

    @Override
    public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable drawingFinished) {
        LogUtil.e("Demo","surfaceRedrawNeededAsync");
        drawingFinished.run();
    }

    /**
     * 创建
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.e("Demo","surfaceCreated");
        isDrawing = true;
        mapSizeThread = new Thread(this);
        mapSizeThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.e("Demo","surfaceChanged");
        if(mapSizeThread == null){
            isDrawing = true;
            mapSizeThread = new Thread(this);
            mapSizeThread.start();
        }
    }


    /**
     * 销毁
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.e("Demo","surfaceDestroyed");
        isDrawing = false;
    }

    /**
     * 控制地图大小线程
     */

        @Override
        public void run() {
            while (isDrawing){
                synchronized (mSurfaceHolder){
                    mapSizeCanvas = mSurfaceHolder.lockCanvas();

                    drawing();

                    if (mapSizeCanvas != null){
                        LogUtil.e("debug","draw1");
                        mSurfaceHolder.unlockCanvasAndPost(mapSizeCanvas);
                    }

                }
            }
        }

    /**
     * 线程
     */
    /*
    @Override
    public void run() {

//        while (isDrawing){

//            long startTime = System.currentTimeMillis();

            synchronized (mSurfaceHolder){
                mCanvas = mSurfaceHolder.lockCanvas();

                drawing();
                if (mCanvas != null){
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }

            }

//            long endTime = System.currentTimeMillis();
//
//            int diffTime = (int) (endTime - startTime);
//
//            while (diffTime <= 30){
//                diffTime = (int)(System.currentTimeMillis() - startTime);
//                Thread.yield();
//            }

//        }
    }
    */

    /**
     * 绘制
     */
    private void drawing() {

        if (mapSizeCanvas == null){
            return;
        }
        mapSizeCanvas.drawColor(Color.WHITE);
//        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        setBoxSize();
        reSetPaintSize();

        if(column != 0 && row != 0){
            //画最外层的边框
            rectMap.set(0, 0, (column + 2)*boxSize, (row + 2)*boxSize);// l,t,r,b
            mapSizeCanvas.drawRect(rectMap, paint);

            // 画行
            for(int i = 0;i < row + 1; i++){
                // startX startY stopX stopY
                mapSizeCanvas.drawLine(boxSize, dynamicHeight + boxSize, boxTotalWidth + boxSize, dynamicHeight + boxSize, paint);
                dynamicHeight += boxSize;
            }

            // 画列
            for(int j = 0;j < column + 1;j++){
                // startX startY stopX stopY
                mapSizeCanvas.drawLine(dynamicWidth + boxSize, boxSize, dynamicWidth + boxSize, boxTotalHeight + boxSize, paint);
                dynamicWidth += boxSize;
            }

        }

        if(testL != -1 && testT != -1){
            if(bitmap != null){
                bitmap.recycle();
                bitmap = null;
            }
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.workstack, null);
            rect_work_site.set(testL*boxSize, testT *boxSize, testL*boxSize+boxSize, testT*boxSize + boxSize);
            mapSizeCanvas.drawBitmap(bitmap, null, rect_work_site,null);
        }

        if(testL1 != -1 && testT1 != -1){
            if(bitmap_charger != null){
                bitmap_charger.recycle();
                bitmap_charger = null;
            }
            bitmap_charger = BitmapFactory.decodeResource(getResources(), R.mipmap.charger_icon, null);
            rect_charge_pile.set(testL1*boxSize, testT1 *boxSize, testL1*boxSize+boxSize, testT1*boxSize + boxSize);
            mapSizeCanvas.drawBitmap(bitmap_charger, null, rect_charge_pile,null);
        }

        // 存储区域
        if(storageList != null && storageList.size() != 0){
            int len = storageList.size();
            String text_storage = "";

            for(int i = 0;i < len;i++){
                long pos = storageList.get(i);
                int unRow = getRowWithPos((int) pos);// 存储区域格子所在行
                int unColumn = getColumnithPos((int) pos);// 存储区域格子所在列
                // 绘制存储区域填充
                int left = (unColumn - 1) * boxSize + boxSize + 1;// 矩形左侧
                int right = unColumn * boxSize - 1 + boxSize;// 矩形右侧
                int top = (unRow - 1) * boxSize + 1 + boxSize;// 矩形上侧
                int bottom = unRow * boxSize + boxSize - 1;// 矩形下侧
                mapSizeCanvas.drawRect(left, top, right, bottom, paintStorageRect);// 绘制矩形

                float x = (unColumn - 1) * boxSize + boxSize;
                float y = unRow * boxSize + boxSize;
                text_storage = String.valueOf(pos);
                mapSizeCanvas.drawText(text_storage, x, y, paintStorageNumber);//存储区域地图坐标
            }
        }


        if(carCurrentPathEntityList != null){// 绘制小车当前路径（锁格和未锁格）
            if(carCurrentPathEntityList.size() != 0){
                int carCurrentPathEntityList_len = carCurrentPathEntityList.size();
                for (int i = 0;i < carCurrentPathEntityList_len;i++){
                    List<Long> lockList = carCurrentPathEntityList.get(i).getLockPath();
                    List<Long> allList = carCurrentPathEntityList.get(i).getAllPath();

                    // 绘制小车当前路径的锁格和未锁格全区域
                    if(allList != null){
                        if(allList.size() != 0){
                            int allList_len = allList.size();
                            for (int k = 0;k < allList_len;k++){
                                long unlockPos = allList.get(k);
                                int unlockRow = getRowWithPos((int) unlockPos);
                                int unlockColumn = getColumnithPos((int) unlockPos);
                                int l = unlockColumn * boxSize + 1;
                                int t = unlockRow * boxSize + 1;
                                int r = l + boxSize - 2;
                                int b = t + boxSize - 2;
                                rectCarCurrentPath.set(l, t, r, b);
                                mapSizeCanvas.drawRect(rectCarCurrentPath, paintCarPathAll);
                            }
                        }
                    }


                    // 绘制小车当前路径的锁格区域
                    /*if(lockList != null){
                        if(lockList.size() != 0){
                            int lockList_len = lockList.size();
                            for (int j = 0;j < lockList_len;j++){
                                long lockPos = lockList.get(j);
                                int lockRow = getRowWithPos((int) lockPos);
                                int lockColumn = getColumnithPos((int) lockPos);
                                int l = lockColumn * boxSize + 1;
                                int t = lockRow * boxSize + 1;
                                int r = l + boxSize - 2;
                                int b = t + boxSize - 2;
                                rectCarCurrentPath.set(l ,t, r, b);
                                canvas.drawRect(rectCarCurrentPath, paintCarPathLock);
                            }
                        }
                    }*/


                }

            }
        }

        try {
            // 所有的锁格区域
            if (allLockedAreaList != null && allLockedAreaList.size() != 0){
                int len = allLockedAreaList.size();
                for (int i = 0;i < len;i++){
                    Object lockPos = allLockedAreaList.get(i);
                    int lockRow = getRowWithPos((int) lockPos);
                    int lockColumn = getColumnithPos((int) lockPos);
                    int l = lockColumn * boxSize + 1;
                    int t = lockRow * boxSize + 1;
                    int r = l + boxSize - 2;
                    int b = t + boxSize - 2;
                    rectCarCurrentPath.set(l ,t, r, b);
                    mapSizeCanvas.drawRect(rectCarCurrentPath, paintCarPathLock);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "锁格区域数据标识异常");
        }

        try {
            if(manualLockList != null && manualLockList.size() != 0){
                int len = manualLockList.size();
                for (int i = 0;i < len;i++){
                    Object manualPos = manualLockList.get(i);
                    int lockRow = getRowWithPos((int) manualPos);
                    int lockColumn = getColumnithPos((int) manualPos);
                    int l = lockColumn * boxSize + 1;
                    int t = lockRow * boxSize + 1;
                    int r = l + boxSize - 2;
                    int b = t + boxSize - 2;
                    rectCarCurrentPath.set(l ,t, r, b);
                    mapSizeCanvas.drawRect(rectCarCurrentPath, paintCarPathLock);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "手动锁格区域数据标识异常");
        }

        // 画数字
        String text = "";
        for(int k = 0; k < row; k++){// 需要画的行数
            for(int l = 0; l < column; l++){// 每一行需要画的列数
                int gridNumber = k * column + (l + 1);// 计算是第几个格子
                text = String.valueOf(gridNumber);
                float x = l * boxSize + boxSize;
                float y = (k + 1) * boxSize + boxSize;
                mapSizeCanvas.drawText(text, x, y, paintNumber);// 正常地图坐标

            }
        }

        // 不可走区域
        if(unWalkedList != null && unWalkedList.size() != 0){
            int len = unWalkedList.size();

            String text_unWalked = "";
            for (int i = 0;i < len;i++){
                long pos = unWalkedList.get(i);
                int unRow = getRowWithPos((int) pos);// 不可走区域格子所在行
                int unColumn = getColumnithPos((int) pos);// 不可走区域格子所在列
                // 绘制不可走区域填充
                int left = (unColumn - 1) * boxSize + boxSize + 1;// 矩形左侧
                int right = unColumn * boxSize - 1 + boxSize;// 矩形右侧
                int top = (unRow - 1) * boxSize + 1 + boxSize;// 矩形上侧
                int bottom = unRow * boxSize + boxSize - 1;// 矩形下侧
                mapSizeCanvas.drawRect(left, top, right, bottom, paintUnWalkedRect);// 绘制矩形

                float x = (unColumn - 1) * boxSize + boxSize;
                float y = unRow * boxSize + boxSize;
                text_unWalked = String.valueOf(pos);
                mapSizeCanvas.drawText(text_unWalked, x, y, paintUnWalkedNumber);//不可走区域地图坐标
            }
        }

        if(chargingPileList != null && chargingPileList.size() != 0){// 绘制充电桩
            list_charge_pile.clear();// 清空集合中数据
            if(bitmap_charger != null){
                bitmap_charger.recycle();
                bitmap_charger = null;
            }
            bitmap_charger = BitmapFactory.decodeResource(getResources(), R.mipmap.charger_icon, null);
            int chargingPileList_length = chargingPileList.size();
            for (int i = 0;i < chargingPileList_length;i++){
                String addrCodeID = chargingPileList.get(i).getAddrCodeID();
                String UUID = chargingPileList.get(i).getUUID();
                int pos = Integer.parseInt(addrCodeID);// 停在充电桩附近小车的地标
                int toward = chargingPileList.get(i).getToward();// 充电桩的朝向（朝向小车）
                int chargeRow = getRowWithPos(pos);
                int chargeColumn = getColumnithPos(pos);
                int l = 0, t = 0, r = 0, b = 0;
                if(toward > -1 && toward < 1){// 充电小车的下面
                    l = chargeColumn * boxSize;
                    t = (chargeRow + 1) * boxSize;
                    r = l + boxSize;
                    b = t + boxSize;
                }else if(toward > 89 && toward < 91){// 充电小车的左边
                    l = (chargeColumn - 1) * boxSize;
                    t = chargeRow * boxSize;
                    r = l + boxSize;
                    b = t + boxSize;
                }else if(toward > 179 && toward < 181){// 充电小车的上面
                    l = chargeColumn * boxSize;
                    t = (chargeRow - 1) * boxSize;
                    r = l + boxSize;
                    b = t + boxSize;
                }else if(toward > 269 && toward < 271){// 充电小车的右边
                    l = (chargeColumn + 1) * boxSize;
                    t = chargeRow * boxSize;
                    r = l + boxSize;
                    b = t + boxSize;
                }
                rect_charge_pile.set(l + 1, t + 1, r - 2, b - 2);
                mapSizeCanvas.drawBitmap(bitmap_charger, null, rect_charge_pile, null);
//                canvas.drawCircle(l + boxSize / 2, t + boxSize / 2, boxSize / 4, paintCircle);
//                canvas.drawText("100", l + boxSize / 2, t + boxSize /2 + boxSize / 8, paintChargeCarId);

                Map<String, Object> map_charge_pile = new HashMap<>();
                map_charge_pile.put("uuid", UUID);
                map_charge_pile.put("left", l);
                map_charge_pile.put("top", t);
                list_charge_pile.add(map_charge_pile);
            }

            try {
//                FileUtil.createFileWithByte(list_charge_pile.toString().getBytes("utf-8"), "数据文件1.doc");
//                FileUtil.createFileWithByte(chargingTaskEntityList.toString().getBytes("utf-8"), "数据文件2.doc");
                LogUtil.e("TAG", "充电桩位置和标识：" + list_charge_pile.toString());
                if(chargingTaskEntityList != null && chargingTaskEntityList.size() != 0){// 存在充电任务,充电桩显示小车id
                    LogUtil.e("TAG", "充电任务实体集合：" + chargingTaskEntityList.toString());
                    paintCircle.setColor(Color.BLACK);
                    int len = chargingTaskEntityList.size();
                    int l = 0, t = 0;
                    for (int i = 0;i < len;i++){
                        String sectionUUID = chargingTaskEntityList.get(i).getSectionUUID();// 地图uuid
                        String chargeUUID = chargingTaskEntityList.get(i).getChargeUUID();// 充电桩的uuid
                        if(sectionUUID != null && sectionId.equals(sectionUUID)){// 确保同一张地图
                            int len_charge_pile = list_charge_pile.size();
                            for (int j = 0;j < len_charge_pile;j++){
                                String strUUID = (String) list_charge_pile.get(j).get("uuid");
                                if (chargeUUID != null && chargeUUID.equals(strUUID)){
                                    // 确定充电桩在地图上的位置
                                    l = (int) list_charge_pile.get(j).get("left");
                                    t = (int) list_charge_pile.get(j).get("top");
                                }
                            }
                        }

                        // 在充电桩上绘制要来充电小车的id
                        if(l != 0 || t != 0){
                            float x = l + boxSize / 2;
                            float y = t + boxSize / 2 + boxSize / 8;
                            mapSizeCanvas.drawCircle(l + boxSize / 2, t + boxSize / 2, boxSize / 4, paintCircle);
                            mapSizeCanvas.drawText(chargingTaskEntityList.get(i).getDriveId(), x, y, paintChargeCarId);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                ToastUtil.showToast(getContext(), "充电桩标识小车异常");
            }
        }

        if(workStackList != null && workStackList.size() != 0){// 标识出工作栈的位置以及旋转区的位置
            // 关键 这里记得回收bitmap对象，释放内存，不然会一直消耗内存直至应用退出
            if(bitmap != null){
                bitmap.recycle();
                bitmap = null;
            }
            bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.workstack, null);
            int l, t, r, b;
            int len = workStackList.size();
            for(int i = 0;i < len;i++){
                int stopPos = workStackList.get(i);// 得到停止点的坐标
                long rotatePos = rotateList.get(i).get(0);// 得到旋转点的坐标
                long enterPos = rotateList.get(i).get(1);// 获取进入点的坐标

                int rotateRow = getRowWithPos((int) rotatePos);// 旋转区域格子所在行
                int rotateColumn = getColumnithPos((int) rotatePos);// 旋转区域格子所在列
                // 绘制旋转区域填充
                int left = (rotateColumn - 1) * boxSize + boxSize + 1;// 矩形左侧
                int right = rotateColumn * boxSize - 1 + boxSize;// 矩形右侧
                int top = (rotateRow - 1) * boxSize + 1 + boxSize;// 矩形上侧
                int bottom = rotateRow * boxSize + boxSize - 1;// 矩形下侧
                mapSizeCanvas.drawRect(left, top, right, bottom, paintRotateRect);// 绘制矩形

                float x = (rotateColumn - 1) * boxSize + boxSize;
                float y = rotateRow * boxSize + boxSize;
                text = String.valueOf(rotatePos);
                mapSizeCanvas.drawText(text, x, y, paintRotateNumber);//旋转区域地图坐标

                int stopRow = getRowWithPos(stopPos);// 获取停止点所在的行
                int stopColumn = getColumnithPos(stopPos);// 获取停止点所在的列
                // 此时需要根据进入点的坐标和旋转点的坐标来判断工作站位于停止点的哪个位置（上、下、左和右）
                if(enterPos < rotatePos && (rotatePos - enterPos) != 1){// 此时工作站位于停止点的下方
                    l = stopColumn * boxSize + 1; t = (stopRow + 1) * boxSize + 1; r = l + boxSize - 2; b = t + boxSize - 2;
                    rect_work_site.set(l, t, r, b);
                    mapSizeCanvas.drawBitmap(bitmap, null, rect_work_site, null);

                    map_stop_workSite.put(stopPos, (stopPos + column));
                }else if(enterPos > rotatePos && (enterPos - rotatePos) != 1){// 此时工作站位于停止点的上方
                    l = stopColumn * boxSize + 1; t = (stopRow - 1) * boxSize + 1; r = l + boxSize - 2; b = t + boxSize - 2;
                    rect_work_site.set(l, t, r, b);
                    mapSizeCanvas.drawBitmap(bitmap, null, rect_work_site, null);

                    map_stop_workSite.put(stopPos, (stopPos - column));
                }else if(enterPos < rotatePos && (rotatePos - enterPos) == 1){// 此时工作站位于停止点的右方
                    l = (stopColumn + 1)*boxSize + 1; t = stopRow * boxSize + 1; r = l + boxSize - 2; b = t + boxSize - 2;
                    rect_work_site.set(l, t, r, b);
                    mapSizeCanvas.drawBitmap(bitmap, null, rect_work_site, null);

                    map_stop_workSite.put(stopPos, (stopPos + 1));
                }else if(enterPos > rotatePos && (enterPos - rotatePos) == 1){// 此时工作站位于停止点的左方
                    l = (stopColumn - 1)*boxSize + 1; t = stopRow * boxSize + 1; r = l + boxSize -2; b = t + boxSize - 2;
                    rect_work_site.set(l, t, r, b);
                    mapSizeCanvas.drawBitmap(bitmap, null, rect_work_site, null);

                    map_stop_workSite.put(stopPos, (stopPos - 1));
                }

            }
        }

        /*if(carRouteMap != null && carRouteMap.size() != 0){// 绘制地图上小车的路径
            Iterator<Long> iteratorCarRoute = carRouteMap.keySet().iterator();// map集合键的迭代器，每个键代表某个小车的id
            while (iteratorCarRoute.hasNext()){
                long key = iteratorCarRoute.next();// 键，也是小车的id
                List<Long> carRouteList = carRouteMap.get(key);// 获取小车的当前路径信息。就是地图上的坐标值集
                int len = carRouteList.size();
                for(int i = 0;i < len;i++){
                    long carRoutePos = carRouteList.get(i);
                    int row = getRowWithPos((int) carRoutePos);// 坐标所在行
                    int column = getColumnithPos((int) carRoutePos);// 坐标所在列
                    // 确定矩形区域
                    int l = (column - 1) * boxSize + boxSize + 1;
                    int t = (row - 1)* boxSize + boxSize + 1;
                    int r = column * boxSize + boxSize - 1;
                    int b = row * boxSize + boxSize - 1;
                    canvas.drawRect(l, t, r, b, paintRouteRect);// 绘制填充区域
                    float x = (column - 1) * boxSize + boxSize;
                    float y = row * boxSize + boxSize;
                    canvas.drawText(String.valueOf(carRoutePos), x, y, paintRouteNumber);// 绘制小车路径区域的坐标
                }
            }
        }*/

        try {
            if(carList != null && carList.size() != 0){
                // 画小车位置
                paintRobotCarId.setTextAlign(Paint.Align.CENTER);// 设置小车id的画笔绘出的文本根据坐标点居中显示
                int len = carList.size();
                for(int carSize = 0; carSize < len; carSize++){
                    paintCircle.setColor(Color.BLACK);
                    paintRobotCarId.setColor(Color.WHITE);// 小车id的字体颜色
                    int carPos = (int) carList.get(carSize).getAddressCodeID();// 获取小车在地图上的位置,即第几个地图格
                    int carId = (int) carList.get(carSize).getRobotID();// 获取小车的id
                    int carRow = 0;// 小车在第几行;
                    int carColumn = 0;// 小车在第几列
                    // 根据小车在地图上的位置计算出小车在哪一行哪一列
                    if(carPos % column == 0){
                        carColumn = column;
                        carRow = carPos / column;
                    }else {
                        carColumn = carPos % column;
                        carRow = carPos / column + 1;
                    }

                    float x = (carColumn - 1) * boxSize + boxSize / 2 + boxSize;// 小车所在格子的中心点x坐标
                    float y = (carRow - 1) * boxSize + boxSize / 2 + boxSize;// 小车所在格子的中心点y坐标

                    int left = (carColumn - 1) * boxSize + boxSize;
                    int top = (carRow - 1) * boxSize + boxSize;
                    int right = (carColumn + 1) * boxSize;
                    int bottom = (carRow + 1) * boxSize;
                    rectF_car.set(left + boxSize/6, top + boxSize/6, right - boxSize/6, bottom - boxSize/6);
                    mapSizeCanvas.drawRoundRect(rectF_car, boxSize/6, boxSize/6, paintCarRoundRect);// 画小车的边框区域（蓝色填充）
                    mapSizeCanvas.drawCircle(x, y, boxSize/4, paintCircle);// 画小车的中间部分，是一个圆(黑色填充)
                    mapSizeCanvas.drawText(String.valueOf(carId), x, y + boxSize/12, paintRobotCarId);// 绘制小车的id

                    try {
                        // 小车有充电任务时，将小车的id颜色标识为红色
                        if(chargingTaskEntityList != null && chargingTaskEntityList.size() != 0){
                            // 小车需要充电时，中间圆形部分是红色，id为白色
                            paintCircle.setColor(Color.RED);
                            paintRobotCarId.setColor(Color.WHITE);
                            int size = chargingTaskEntityList.size();
                            for (int i = 0;i < size;i++){
                                String driveId = chargingTaskEntityList.get(i).getDriveId();
                                if(driveId != null && carId == Integer.parseInt(driveId)){
                                    mapSizeCanvas.drawCircle(x, y, boxSize/4, paintCircle);
                                    mapSizeCanvas.drawText(String.valueOf(carId), x, y + boxSize/12, paintRobotCarId);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        if(podList != null && podList.size() != 0){
            // 画pod的位置
            paintPodId.setColor(Color.WHITE);// pod id的字体颜色
            int len = podList.size();
            for(int podSize = 0;podSize < len;podSize++){
                int podPos = podList.get(podSize).getPodPos();// 获取pod在地图上的位置,即第几个地图格
                int podId = podList.get(podSize).getPodId();// 获取pod的id
                int podRow = 0;// pod在第几行;
                int podColumn = 0;// pod在第几列
                // 根据pod在地图上的位置计算出pod在哪一行哪一列
                if(podPos % column == 0){
                    podColumn = column;
                    podRow = podPos / column;
                }else {
                    podColumn = podPos % column;
                    podRow = podPos / column + 1;
                }

                int left = podColumn * boxSize - boxSize/3 + boxSize;
                int top = (podRow - 1) * boxSize + boxSize;
                int right = podColumn * boxSize + boxSize;
                int bottom = (podRow - 1) * boxSize + boxSize/3 + boxSize;

                mapSizeCanvas.drawRect(left, top, right, bottom, paintRect);// 画矩形，边长现设定为格子大小的1/3

                float x = podColumn * boxSize - boxSize/6 + boxSize;// 矩形中心点的x坐标
                float y = (podRow - 1) * boxSize + boxSize/6 + boxSize/16 + boxSize;// 矩形中心点的y坐标
                paintPodId.setTextAlign(Paint.Align.CENTER);
                mapSizeCanvas.drawText(String.valueOf(podId), x, y, paintPodId);

                int podAngle = podList.get(podSize).getPodAngle();
                /*Log.d("TAG_BoxView,pod", "-----------");
                Log.d("TAG_BoxView,podID = ", "" + podId);
                Log.d("TAG_BoxView,podPos = ", "" + podPos);
                Log.d("TAG_BoxView,podAngle = ", "" + podAngle);*/

                // 画点表示pod的A面
                paintPodSide.setStrokeWidth(boxSize/15);// 设置点的大小
                paintPodSide.setStrokeCap(Paint.Cap.ROUND);// 设置点的样式为圆形

                float x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;
                if(podAngle < 10){// pod的A面朝上
                    x1 = podColumn * boxSize - boxSize/6 + boxSize;
                    y1 = (podRow - 1) * boxSize + boxSize + boxSize/24;// 中间点
                    x2 = podColumn * boxSize - boxSize/6 + boxSize - boxSize/12;
                    y2 = (podRow - 1) * boxSize + boxSize + boxSize/24;
                    x3 = podColumn * boxSize - boxSize/6 + boxSize + boxSize/12;
                    y3 = (podRow - 1) * boxSize + boxSize + boxSize/24;
                }
                if(85 < podAngle && podAngle < 95){// pod的A面朝右
                    x1 = podColumn * boxSize - boxSize/24 + boxSize;
                    y1 = (podRow - 1) * boxSize + boxSize + boxSize/6;// 中间点
                    x2 = podColumn * boxSize - boxSize/24 + boxSize;
                    y2 = (podRow - 1) * boxSize + boxSize + boxSize/6 + boxSize/12;
                    x3 = podColumn * boxSize - boxSize/24 + boxSize;
                    y3 = (podRow - 1) * boxSize + boxSize + boxSize/6 - boxSize/12;
                }
                if(175 < podAngle && podAngle < 185){// pod的A面朝下
                    x1 = podColumn * boxSize - boxSize/6 + boxSize;
                    y1 = (podRow - 1) * boxSize + boxSize + boxSize/3 - boxSize/24;// 中间点
                    x2 = podColumn * boxSize - boxSize/6 + boxSize + boxSize/12;
                    y2 = (podRow - 1) * boxSize + boxSize + boxSize/3 - boxSize/24;
                    x3 = podColumn * boxSize - boxSize/6 + boxSize - boxSize/12;
                    y3 = (podRow - 1) * boxSize + boxSize + boxSize/3 - boxSize/24;
                }
                if(265 < podAngle && podAngle < 275){// pod的A面朝左
                    x1 = podColumn * boxSize - boxSize/3 + boxSize/24 + boxSize;
                    y1 = (podRow - 1) * boxSize + boxSize + boxSize/6;// 中间点
                    x2 = podColumn * boxSize - boxSize/3 + boxSize/24 + boxSize;
                    y2 = (podRow - 1) * boxSize + boxSize + boxSize/6 + boxSize/12;
                    x3 = podColumn * boxSize - boxSize/3 + boxSize/24 + boxSize;
                    y3 = (podRow - 1) * boxSize + boxSize + boxSize/6 - boxSize/12;
                }

                float[] fPoints = {x1, y1, x2, y2, x3, y3};
                mapSizeCanvas.drawPoints(fPoints,paintPodSide);
            }
        }

        try {
            if (lock_unlock_pos != null && lock_unlock_pos .size() != 0){
                int len = lock_unlock_pos.size();
                for (int i = 0;i < len;i++){
                    int pos = lock_unlock_pos.get(i);

                    int posRow = getRowWithPos(pos);
                    int posColumn = getColumnithPos(pos);

                    int left = (posColumn - 1) * boxSize + boxSize;
                    int top = (posRow - 1) * boxSize + boxSize;
                    int right = left + boxSize;
                    int bottom = top + boxSize;

                    rect.set(left + boxSize/4,top + boxSize/4, right - boxSize/4, bottom - boxSize/4);
                    mapSizeCanvas.drawRect(rect, paintLockUnLock);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        LogUtil.e("debug","draw2");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LogUtil.e("Demo","onLayout");
        if (mapSizeThread == null){
            isDrawing = true;
            mapSizeThread = new Thread(this);
            mapSizeThread.start();
        }
    }
}
