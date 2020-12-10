package com.kernal.passportreader.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kernal.passportreader.sdk.utils.CardScreenUtil;
import com.kernal.passportreader.sdk.utils.DefaultPicSavePath;
import com.kernal.passportreader.sdk.view.ScanCardsView;

import kernal.idcard.android.ResultMessage;
import kernal.idcard.camera.CardOcrRecogConfigure;
import kernal.idcard.camera.IScanReturnMessage;
import kernal.idcard.camera.SharedPreferencesHelper;
import kernal.idcard.camera.UritoPathUtil;

/**
 * @author A@H
 * @describle 相机界面，主要识别结果的获取以及识别完成之后的跳转
 */
public class CardsCameraActivity extends AppCompatActivity implements IScanReturnMessage, View.OnClickListener {
    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams imageButton_flash_params, imageButton_camera_params, imageButton_back_params, imageButton_spot_dection_params, imageButton_ejct_params;
    ImageButton imageButton_flash, imageButton_camera, imageButton_back, imageButton_spot_dection, imageButton_ejct;
    ScanCardsView scanICamera;
    private boolean isOpenFlash = false;
    private int width, height;
    private boolean isOpendetectLightspot = false;

    public CardsCameraActivity() {
        CardOcrRecogConfigure.getInstance()
                //设置识别返回的语言
                .initLanguage(getApplicationContext())
                //证件类型的ID
                .setnMainId(SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2))
                //证件类型的子ID
                .setnSubID(SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nSubID", 0))
                //身份证的正反面区分 0-自动区分；1-只识别正面；2-只识别反面（注：不设置默认为0）
                .setFlag(0)
                //设置扫描的方式 0-指导框扫描；1-实时监测边线
                .setnCropType(1)
                //是否保存全图
                .setSaveFullPic(true)
                //是否保存裁切图
                .setSaveCut(true)
                //是否保存证件的头像
                .setSaveHeadPic(true)
                //是否开启获取泰文的坐标点（注：只在识别泰国身份证，设置该参数）
                .setOpenGetThaiFeatureFuction(false)
                //是否开启证件识别的复印件的区分（注：黑白复印件通用，彩色复印件和摩尔纹只适用于身份证）
                .setOpenIDCopyFuction(true)
                //是否获取泰文的条码图片（注:只适用于泰国身份证）
                .setThaiCodeJpgPath(false)
                //是否开启拒识功能，默认开启拒识功能
                .setSetIDCardRejectType(true)
                //设置图片的存储路径(注：默认路径为:Environment.getExternalStorageDirectory().toString()
                //            + "/wtimage/")
                .setSavePath(new DefaultPicSavePath(this, true));
    }

    /**
     * 扫描识别成功，界面的跳转(回调接口)
     *
     * @param resultMessage 识别结果
     * @param picPath       图片路径数组，picPath[0]: 全图路径；picPath[1]: 裁切图；picPath[2]: 证件头像
     */
    @Override
    public void scanOCRIdCardSuccess(ResultMessage resultMessage, String[] picPath) {
        Vibrator mVibrator = (Vibrator) getApplication().getSystemService(
                Service.VIBRATOR_SERVICE);
        mVibrator.vibrate(200);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("resultMessage", resultMessage);
        bundle.putStringArray("picpath", picPath);
        intent.putExtra("resultbundle", bundle);
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    /**
     * 扫描识别失败，界面的跳转(回调接口)
     *
     * @param error   -10601 开发码错误,把该文件中的{@link com.kernal.passportreader.sdk.utils.Devcode}中的devcode替换
     *                -10602 applicationId错误，把build.gradle文件中的 applicationId修改为授权文件中绑定的信息
     *                -10603 授权到期，请从新申请授权
     *                -10605  string.xml中的app_name字段属性和授权文件中绑定的不一致
     *                -10606  string.xml中company_name字段属性和授权文件中绑定的不一致
     *                -10608  string.xml中缺少company_name字段，请添加该字段
     * @param picPath 该数组存储的是拍照识别失败时的全图路径
     */
    @Override
    public void scanOCRIdCardError(String error, String[] picPath) {
        Intent intent = new Intent();
        intent.putExtra("error", error);
        intent.putExtra("strpicpath", picPath[0]);
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }


    @Override
    public void authOCRIdCardSuccess(String result) {

    }

    /**
     * 授权失败
     *
     * @param error
     * @see
     */
    @Override
    public void authOCRIdCardError(String error) {
        Intent intent = new Intent();
        intent.putExtra("error", error);
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            scanICamera.importPicRecog(UritoPathUtil.getImageAbsolutePath(getApplicationContext(), uri));
        }
    }

    @Override
    public void onClick(View v) {
        //返回事件
        if (v == imageButton_back) {
            this.finish();
            //拍照事件
        } else if (v == imageButton_camera) {
            scanICamera.takePicRecog();
            //闪光灯
        } else if (v == imageButton_flash) {
            if (isOpenFlash) {
                isOpenFlash = false;
                scanICamera.managerFlashLight(isOpenFlash);
                imageButton_flash.setBackgroundResource(R.mipmap.flash_off);
            } else {
                isOpenFlash = true;
                scanICamera.managerFlashLight(isOpenFlash);
                imageButton_flash.setBackgroundResource(R.mipmap.flash_on);
            }
            //隐藏、显示拍照事件
        } else if (v == imageButton_ejct) {
            imageButton_ejct.setVisibility(View.GONE);
            imageButton_camera.setVisibility(View.VISIBLE);
            // 光斑检测事件
        } else if (v == imageButton_spot_dection) {
            if (isOpendetectLightspot) {
                Toast.makeText(this, getString(R.string.closeddetectLightspot),
                        Toast.LENGTH_SHORT).show();
                isOpendetectLightspot = false;
                imageButton_spot_dection.setBackgroundResource(R.mipmap.spot_dection_off);
                scanICamera.managerSpotDection(isOpendetectLightspot);
            } else {
                Toast.makeText(this, getString(R.string.opendetectLightspot),
                        Toast.LENGTH_SHORT).show();
                isOpendetectLightspot = true;
                imageButton_spot_dection.setBackgroundResource(R.mipmap.spot_dection_on);
                scanICamera.managerSpotDection(isOpendetectLightspot);
            }
        }
    }

    @Override
    @TargetApi(16)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideBottomUIMenu();
        setContentView(R.layout.activity_idcardscan);
        width = CardScreenUtil.getScreenResolution(this).x;
        height = CardScreenUtil.getScreenResolution(this).y;
        // WriteUtil.writeLog("该设备的屏幕分辨率为:"+width+"X"+height);
        relativeLayout = findViewById(R.id.camera_layout);
        scanICamera = new ScanCardsView(this);
        if (CardScreenUtil.getScreenOrientation(this) == CardScreenUtil.ORIENTATION_LANDSCAPE) {
            //闪光灯
            imageButton_flash = new ImageButton(this);
            imageButton_flash.setBackground(getResources().getDrawable(R.mipmap.flash_off));
            imageButton_flash_params = new RelativeLayout.LayoutParams((int) (width * 0.05), (int) (width * 0.05));
            imageButton_flash_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.camera_layout);
            imageButton_flash_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.camera_layout);
            imageButton_flash_params.leftMargin = (int) (width * 0.02);
            imageButton_flash_params.topMargin = (int) (width * 0.02);

            //拍照按钮
            imageButton_camera = new ImageButton(this);
            imageButton_camera.setBackground(getResources().getDrawable(R.mipmap.tack_pic_btn));
            imageButton_camera_params = new RelativeLayout.LayoutParams((int) (width * 0.1),
                    (int) (width * 0.1));
            imageButton_camera_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.camera_layout);
            imageButton_camera_params.addRule(RelativeLayout.CENTER_VERTICAL);
            imageButton_camera_params.rightMargin = (int) (width * 0.02);
            imageButton_camera.setVisibility(View.GONE);

            //返回按钮
            imageButton_back = new ImageButton(this);
            imageButton_back.setBackground(getResources().getDrawable(R.mipmap.camera_back_nomal));
            imageButton_back_params = new RelativeLayout.LayoutParams((int) (width * 0.05), (int) (width * 0.05));
            imageButton_back_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.camera_layout);
            imageButton_back_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.camera_layout);
            imageButton_back_params.leftMargin = (int) (width * 0.02);
            imageButton_back_params.bottomMargin = (int) (width * 0.02);
            //光斑检测按钮
            imageButton_spot_dection = new ImageButton(this);
            imageButton_spot_dection.setBackgroundResource(R.mipmap.spot_dection_off);
            imageButton_spot_dection_params = new RelativeLayout.LayoutParams((int) (width * 0.08), (int) (width * 0.08));
            imageButton_spot_dection_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.camera_layout);
            imageButton_spot_dection_params.addRule(RelativeLayout.CENTER_VERTICAL);
            imageButton_spot_dection_params.leftMargin = (int) (width * 0.02);

            //隐藏拍照按钮
            imageButton_ejct = new ImageButton(this);
            imageButton_ejct.setBackground(getResources().getDrawable(R.mipmap.locker_btn_def));
            imageButton_ejct_params = new RelativeLayout.LayoutParams((int) (height * 0.05), (int) (height * 0.5));
            imageButton_ejct_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.camera_layout);
            imageButton_ejct_params.addRule(RelativeLayout.CENTER_VERTICAL);

        } else {
            //竖屏状态下框的方向 true:width>height; false:height>width
            //ViewfinderView.isSameScreen = false;
            //闪光灯
            imageButton_flash = new ImageButton(this);
            imageButton_flash.setBackground(getResources().getDrawable(R.mipmap.flash_off));
            imageButton_flash_params = new RelativeLayout.LayoutParams((int) (height * 0.05), (int) (height * 0.05));
            imageButton_flash_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.camera_layout);
            imageButton_flash_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.camera_layout);
            imageButton_flash_params.rightMargin = (int) (height * 0.02);
            imageButton_flash_params.topMargin = (int) (height * 0.02);

            //拍照按钮
            imageButton_camera = new ImageButton(this);
            imageButton_camera.setBackground(getResources().getDrawable(R.mipmap.tack_pic_btn));
            imageButton_camera_params = new RelativeLayout.LayoutParams((int) (height * 0.1),
                    (int) (height * 0.1));
            imageButton_camera_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.camera_layout);
            imageButton_camera_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            imageButton_camera_params.bottomMargin = (int) (height * 0.02);
            imageButton_camera.setVisibility(View.GONE);

            //返回按钮
            imageButton_back = new ImageButton(this);
            imageButton_back.setBackground(getResources().getDrawable(R.mipmap.camera_back_nomal));
            imageButton_back_params = new RelativeLayout.LayoutParams((int) (height * 0.05), (int) (height * 0.05));
            imageButton_back_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.camera_layout);
            imageButton_back_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.camera_layout);
            imageButton_back_params.leftMargin = (int) (height * 0.02);
            imageButton_back_params.topMargin = (int) (height * 0.02);
            //光斑检测按钮
            imageButton_spot_dection = new ImageButton(this);
            imageButton_spot_dection.setBackgroundResource(R.mipmap.spot_dection_off);
            imageButton_spot_dection_params = new RelativeLayout.LayoutParams((int) (height * 0.08), (int) (height * 0.08));
            imageButton_spot_dection_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.camera_layout);
            imageButton_spot_dection_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            imageButton_spot_dection_params.topMargin = (int) (height * 0.02);

            //隐藏拍照按钮
            imageButton_ejct = new ImageButton(this);
            imageButton_ejct.setBackground(getResources().getDrawable(R.mipmap.locker_btn_def_p));
            imageButton_ejct_params = new RelativeLayout.LayoutParams((int) (width * 0.5), (int) (width * 0.05));
            imageButton_ejct_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.camera_layout);
            imageButton_ejct_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        relativeLayout.addView(scanICamera, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(imageButton_flash, imageButton_flash_params);
        relativeLayout.addView(imageButton_camera, imageButton_camera_params);
        relativeLayout.addView(imageButton_back, imageButton_back_params);
        relativeLayout.addView(imageButton_spot_dection, imageButton_spot_dection_params);
        relativeLayout.addView(imageButton_ejct, imageButton_ejct_params);
        imageButton_camera.setOnClickListener(this);
        imageButton_back.setOnClickListener(this);
        imageButton_flash.setOnClickListener(this);
        imageButton_ejct.setOnClickListener(this);
        imageButton_spot_dection.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanICamera.setIScan(this);
        scanICamera.startCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();

        scanICamera.stopCamera();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanICamera.destroyService();
    }

    @Override
    public void openCameraError(String error) {
        Log.i("string", "失败的信息" + error);

    }


    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

}
