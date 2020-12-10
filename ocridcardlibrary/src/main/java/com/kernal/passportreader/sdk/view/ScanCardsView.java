package com.kernal.passportreader.sdk.view;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kernal.passportreader.sdk.OcrRecogTask;
import com.kernal.passportreader.sdk.R;
import com.kernal.passportreader.sdk.ThreadManager;
import com.kernal.passportreader.sdk.utils.CardScreenUtil;
import com.kernal.passportreader.sdk.utils.ManageIDCardRecogResult;

import kernal.idcard.android.Frame;
import kernal.idcard.android.IDCardAPI;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;
import kernal.idcard.camera.CardOcrRecogConfigure;
import kernal.idcard.camera.IScanReturnMessage;
import kernal.idcard.camera.IdCardType;
import kernal.idcard.camera.OcrIdCardRecogParams;
import kernal.idcard.camera.ScanICamera;

/**
 * @author A@H
 * @describle 自定义扫描识别View实现了实时预览方法
 */
public class ScanCardsView extends RelativeLayout implements Camera.PreviewCallback, ScanICamera {
    CameraPreview mPreview;
    TextView text_idCardType, text_tips;
    Handler mHandler;
    protected  Camera mCamera;
    private boolean mSpotAble;//保持预览识别中线程单个执行
    private volatile boolean isFirstCreate = true;
    private Intent recogIntent;
    private IScanReturnMessage iScanReturnMessage;
    private int nMainIDX;
    private int nSubID;
    private RecogService.recogBinder recogBinder;
    private OcrIdCardRecogParams ocrIdCardRecogParams;
    private OcrRecogTask ocrRecogTask;
    private ViewfinderView viewfinderView;
    private Frame frame=new Frame();
    private int ConfirmSideSuccess;
    private int nCropType = 1;
    private volatile boolean isTakePic = false;
    private boolean isOpendetectLightspot = false;
    private int orientation;
    public static int[] IdcardTypes;
    private String[] picPath;
    private ResultMessage resultMessage;
    String errorMessage = "";
    Toast toast;

    /**
     * 实时预览，定时获取检测边线结果值
     */
    private Handler PreViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (frame != null) {
                if (ConfirmSideSuccess == -139) {
                    ViewfinderView.FRAMECOLOR = Color.rgb(238, 65, 86);
                    text_tips.setText(getContext().getString(getResources().getIdentifier("please_place",
                            "string", getContext().getPackageName())) + text_idCardType.getText().toString());
                    if (nCropType == 1) {
                        viewfinderView.setFourLines(frame, null);
                    }
                    PreViewHandler.sendEmptyMessageDelayed(100, 1500);
                } else if (ConfirmSideSuccess == -145) {
                    ViewfinderView.FRAMECOLOR = Color.rgb(238, 65, 86);
                    if (nCropType == 1) {
                        viewfinderView.setFourLines(frame, null);
                    }
                    text_tips.setText(getContext().getString(getResources().getIdentifier("too_far_away",
                            "string", getContext().getPackageName())) + text_idCardType.getText().toString());
                    PreViewHandler.sendEmptyMessageDelayed(100, 1500);
                } else {
                    text_tips.setText(null);
                    ViewfinderView.FRAMECOLOR = Color.rgb(77, 223, 68);
                    if (nCropType == 1) {
                        viewfinderView.setFourLines(frame, null);
                    }
                    PreViewHandler.sendEmptyMessageDelayed(100, 1500);
                }

            }

        }
    };
    /**
     * 光斑检测提示
     */
    private Handler spotDectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(toast==null){

                toast= Toast.makeText(getContext(),
                        getContext().getString(R.string.detectLightspot),
                        Toast.LENGTH_SHORT);
            }else{
                toast.setText(getContext().getString(R.string.detectLightspot));

            }
            toast.show();

        }
    };
    /**
     * 识别成功更新UI
     */
    private Handler recogSucessHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ViewfinderView.FRAMECOLOR = Color.rgb(77, 223, 68);
            viewfinderView.setFourLines(frame, null);
            text_tips.setText("");
            isTakePic = false;
            iScanReturnMessage.scanOCRIdCardSuccess(resultMessage, picPath);
        }
    };

    private Handler recogErrorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    iScanReturnMessage.scanOCRIdCardError(errorMessage,picPath);
                    break;
                case 1:
                    ViewfinderView.FRAMECOLOR = Color.rgb(238, 65, 86);
                    text_tips.setText(getContext().getString(getResources().getIdentifier("please_place",
                            "string", getContext().getPackageName())) + text_idCardType.getText().toString());
                    if (nCropType == 1) {
                        viewfinderView.setFourLines(frame, null);
                    }
                    if(mCamera!=null&&mSpotAble){
                        mCamera.setOneShotPreviewCallback(ScanCardsView.this);
                    }
                    break;

            }


        }
    };

    public ScanCardsView(Context context) {
        this(context, null);
    }

    public ScanCardsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScanCardsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler();
        initOcr();
        initView(context, attrs);
    }

    /**
     * 初始化布局
     *
     * @param context
     * @param attrs
     */
    @TargetApi(16)
    private void initView(Context context, AttributeSet attrs) {

        mPreview = new CameraPreview(getContext());
        mPreview.setId(R.id.cards_camera_preview);
        orientation = CardScreenUtil.getScreenOrientation(context);

        //证件类型
        LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLayoutParams.addRule(CENTER_VERTICAL);
        text_idCardType = new TextView(getContext());
        text_idCardType.setText(IdCardType.getIdName(getContext(), nMainIDX, nSubID));
        text_idCardType.setGravity(Gravity.CENTER);
        text_idCardType.setTextColor(Color.RED);
        text_idCardType.setTextSize(18);
        text_idCardType.setAlpha(1);
        //错误提示
        text_tips = new TextView(getContext());
        LayoutParams texttipLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        texttipLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
        texttipLayoutParams.bottomMargin = CardScreenUtil.dp2px(getContext(), 80);
        text_tips.setTextSize(14);
        text_tips.setTextColor(Color.RED);
        text_tips.setGravity(Gravity.CENTER);
        text_tips.setAlpha(1);
        //扫描框
        if (nMainIDX != 3000) {
            viewfinderView = new ViewfinderView(getContext(), nCropType, 0);
        } else {
            viewfinderView = new ViewfinderView(getContext(), nCropType, 1);
        }
        viewfinderView.setIdcardType(nMainIDX);
        LayoutParams viewfindParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //viewfindParams.addRule(RelativeLayout.ALIGN_TOP, mPreview.getId());
       // viewfindParams.addRule(RelativeLayout.ALIGN_BOTTOM, mPreview.getId());
        this.addView(mPreview);
        this.addView(text_idCardType, textLayoutParams);
        this.addView(viewfinderView, viewfindParams);
        this.addView(text_tips, texttipLayoutParams);

    }


    /**
     * 初始化识别
     */
    private void initOcr() {
        initRecogService();
      /*  if (IdcardTypes != null && IdcardTypes.length > 1) {
            RecogService.listLoadXMLIdCardType.clear();
            for (int i = 0; i < IdcardTypes.length; i++) {
                if (IdcardTypes[i] == 2 || IdcardTypes[i] == 3) {
                    RecogService.listLoadXMLIdCardType.add(2);
                    RecogService.listLoadXMLIdCardType.add(3);
                } else {
                    RecogService.listLoadXMLIdCardType.add(IdcardTypes[i]);
                }
            }
        }*/
        recogIntent = new Intent(getContext(), RecogService.class);
        getContext().bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
    }

    /**
     * 识别服务静态数值赋值
     */
    private void initRecogService() {
        nMainIDX = CardOcrRecogConfigure.getInstance().nMainId;
        nSubID = CardOcrRecogConfigure.getInstance().nSubID;
        nCropType = CardOcrRecogConfigure.getInstance().nCropType;
        RecogService.isOnlyReadSDAuthmodeLSC = false;
        if (nMainIDX == 3000) {
            RecogService.nMainID = 1034;
        } else {
            RecogService.nMainID = nMainIDX;
        }
        RecogService.isRecogByPath = false;
    }

    /**
     * 实时预览，对每一帧数据进行处理
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (mSpotAble) {
            if (isFirstCreate) {
         //WriteUtil.writeLog("预览图和屏幕分辨率的比率为:"+mPreview.getScale());
                ocrIdCardRecogParams = new OcrIdCardRecogParams.Build()
                        //设置预览的宽度
                        .setPreWidth(mPreview.getPreViewSize().x)
                        //设置预览的高度
                        .setPreHeight(mPreview.getPreViewSize().y)
                        //
                        .setScanICamera(ScanCardsView.this)
                        //设置识别服务
                        .setRecogBinder(recogBinder)
                        //设置屏幕大小与预览大小的比
                        .setscale((1 / mPreview.getScale()))
                        //设置图片的方向
                        .setPicOrientation(CardScreenUtil.getPicOrientation(getContext()))
                        .create();
                ocrRecogTask = new OcrRecogTask(getContext(), ocrIdCardRecogParams);
                //设置扫描识别的敏感区域
                ocrRecogTask.setNv21Point(getNV21Point());
            }
            // 传递识别数据，设置是否是拍照
            ocrRecogTask.setData(data).setTakePic(isTakePic);
            ThreadManager.getThreadPool().execute(ocrRecogTask);
        }
    }

    @Override
    public void setIScan(IScanReturnMessage iScanReturnMessage) {
        this.iScanReturnMessage = iScanReturnMessage;
    }

    /**
     * 关闭摄像头
     */
    @Override
    public void stopCamera() {
        try {
            PreViewHandler.removeCallbacksAndMessages(null);
            spotDectionHandler.removeCallbacksAndMessages(null);
            recogSucessHandler.removeCallbacksAndMessages(null);
            recogErrorHandler.removeCallbacksAndMessages(null);
            if(toast!=null){
                toast.cancel();
            }
            if (mCamera != null) {
                mSpotAble = false;
                mPreview.stopCameraPreview();
                mPreview.setCamera(null, null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {


        }
    }

    /**
     * 管理光斑检测
     * @param isOpendetectLightspot
     */
    public void managerSpotDection(boolean isOpendetectLightspot) {
        this.isOpendetectLightspot = isOpendetectLightspot;
        if(ocrRecogTask!=null){
            ocrRecogTask.setOpendetectLightspot(isOpendetectLightspot);
        }

    }

    /**
     * 管理闪光灯
     *
     * @param
     */
    @Override
    public void managerFlashLight(boolean isOpenFlash) {
        if (isOpenFlash) {
            mPreview.openFlashlight();
        } else {
            mPreview.closeFlashlight();
        }
    }

    /**
     * 开启后置相机
     */
    @Override
    public void startCamera() {
        startCameraById(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * 开启指定相机
     *
     * @param var
     */
    @Override
    public void startCameraById(int var) {
        if (mCamera != null) {
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == var) {
                try {
                    mCamera = Camera.open(var);
                    mPreview.setCamera(mCamera, this);
                    starOcr();
                } catch (Exception e) {
                    iScanReturnMessage.openCameraError(e.toString());
                }
                break;
            }
        }
    }

    /**
     * 切换证件类型
     */
    public void switchIDCardType(int nMainIDX, int nSubID) {
        int[] nSubIDs={nSubID};
        CardOcrRecogConfigure.getInstance().setnMainId(nMainIDX).setnSubID(nSubID);
        initRecogService();
        if(recogBinder!=null) {
            recogBinder.SetParameter(1, nMainIDX);
            recogBinder.SetIDCardID(nMainIDX, nSubIDs);
        }
        ocrRecogTask.setnMainIDX(nMainIDX);
        viewfinderView.setIdcardType(nMainIDX);
        viewfinderView.setFourLines(new Frame(), "");
        text_idCardType.setText(IdCardType.getIdName(getContext(), nMainIDX, nSubID));

    }

    /**
     * 开启预览识别
     */
    private void starOcr() {
        mSpotAble = true;
        mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        mHandler.post(mOneShotPreviewCallbackTask);
        PreViewHandler.removeCallbacks(mPreViewUpdateUiTask);
        PreViewHandler.post(mPreViewUpdateUiTask);
    }


    public ServiceConnection recogConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            recogBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            recogBinder = (RecogService.recogBinder) service;
            if (recogBinder != null) {
                if (recogBinder.getReturnAuthority() != 0) {
                    iScanReturnMessage.authOCRIdCardError("授权失败" + recogBinder.getReturnAuthority());
                }
            }
        }
    };



    /**
     * 根据屏幕上预览框的坐标点计算出图片上的坐标点
     *
     * @return
     */
    int[] getNV21Point() {
        int[] NV21Point = new int[4];
         float scale=mPreview.getScale();

        if (CardScreenUtil.ORIENTATION_PORTRAIT == CardScreenUtil.getScreenOrientation(getContext()) && !ViewfinderView.isSameScreen) {
            NV21Point[0] = (int) (viewfinderView.getFrame().top * scale);
            NV21Point[1] = (int) (viewfinderView.getFrame().left * scale);
            NV21Point[2] = (int) (viewfinderView.getFrame().bottom * scale);
            NV21Point[3] = (int) (viewfinderView.getFrame().right * scale);
        } else {
            NV21Point[0] = (int) (viewfinderView.getFrame().left *  scale);
            NV21Point[1] = (int) (viewfinderView.getFrame().top *   scale);
            NV21Point[2] = (int) (viewfinderView.getFrame().right * scale);
            NV21Point[3] = (int) (viewfinderView.getFrame().bottom *scale);
        }
        return NV21Point;
    }

    /**
     * 实现循环回调
     */
    @Override
    public void addPreviewCallBack() {
        isFirstCreate = false;
        if (mCamera != null && mSpotAble) {
            try {
                mCamera.setOneShotPreviewCallback(ScanCardsView.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消识别任务，解绑识别服务
     */
    @Override
    public void destroyService() {
        ThreadManager.getThreadPool().cancel(ocrRecogTask);
        IdcardTypes = null;
        if (recogBinder != null) {
            getContext().unbindService(recogConn);
            recogBinder = null;
        }
    }

    /**
     * 实时更新界面
     *
     * @param frame
     * @param ConfirmSideSuccess
     */
    @Override
    public void UpdateFrame(Frame frame, int ConfirmSideSuccess,int detectLightspot) {
        this.frame = frame;
        this.ConfirmSideSuccess = ConfirmSideSuccess;
        if (isOpendetectLightspot&&ConfirmSideSuccess>=0&&detectLightspot==0) {
            spotDectionHandler.sendEmptyMessage(0);
        }
        if (nCropType == 1) {
            viewfinderView.setFourLines(frame, null);
        }
    }

    /**
     * 证件识别成功
     *
     * @param resultMessage
     * @param picPath
     */
    @Override
    public void recogSucessUpdateUi(ResultMessage resultMessage, String[] picPath) {
        this.resultMessage=resultMessage;
        this.picPath = picPath;
        PreViewHandler.removeCallbacksAndMessages(null);
        spotDectionHandler.removeCallbacksAndMessages(null);
        isTakePic = false;
        recogSucessHandler.sendEmptyMessage(0);

    }

    /**
     * 证件识别失败
     *
     * @param resultMessage
     */
    @Override
    public void recogErrorUpdateUi(ResultMessage resultMessage,String[] picPath) {

        if (isTakePic) {
            isTakePic = false;
            this.picPath=picPath;
            PreViewHandler.removeCallbacksAndMessages(null);
            errorMessage = ManageIDCardRecogResult.managerErrorRecogResult(getContext(), resultMessage);
            recogErrorHandler.sendEmptyMessage(0);

        } else {

            if(resultMessage.ReturnRecogIDCard!=-6){
                this.picPath=picPath;
                PreViewHandler.removeCallbacksAndMessages(null);
                errorMessage = ManageIDCardRecogResult.managerErrorRecogResult(getContext(), resultMessage);
                recogErrorHandler.sendEmptyMessage(0);
                return;
            }else{
                recogErrorHandler.sendEmptyMessage(1);
            }

        }
    }

    /**
     * 拍照识别
     */
    @Override
    public void takePicRecog() {
        isTakePic = true;
    }

    @Override
    public void importPicRecog(String picPath) {
        OcrRecogTask.importPicPath= picPath;
        isTakePic = true;
        RecogService.isRecogByPath = true;
        ConfirmSideSuccess = -1;
     /*   viewfinderView.setFourLines(new Frame(), null);
        text_tips.setText("");*/
    }

    /**
     * 循环监听扫描检测边线返回值
     */
    private Runnable mPreViewUpdateUiTask = new Runnable() {
        @Override
        public void run() {
            PreViewHandler.sendEmptyMessageDelayed(100, 1000);
        }
    };
    /**
     * 设置预览回调
     */
    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && mSpotAble) {
                try {
                    mCamera.setOneShotPreviewCallback(ScanCardsView.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }
}
