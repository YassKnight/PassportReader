package com.kernal.passportreader.sdk;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.kernal.passportreader.sdk.base.OcrBaseActivity;
import com.kernal.passportreader.sdk.utils.DefaultPicSavePath;
import com.kernal.passportreader.sdk.utils.Devcode;
import com.kernal.passportreader.sdk.utils.ImportRecog;
import com.kernal.passportreader.sdk.utils.ManageIDCardRecogResult;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kernal.idcard.android.ResultMessage;
import kernal.idcard.camera.CardOcrRecogConfigure;
import kernal.idcard.camera.IBaseReturnMessage;
import kernal.idcard.camera.SerialAuth;
import kernal.idcard.camera.SharedPreferencesHelper;
import kernal.idcard.camera.UritoPathUtil;

/**
 * Time:2018/12/13
 * Author:A@H
 * Description:该activity是包含所有证件类型的
 */
public class AllCardMainActivity extends OcrBaseActivity implements IBaseReturnMessage {
    private static final int REQUEST_SCANACTIVITY=1;//跳转相机界面扫描请求码
    private  static  final int REQUEST_SYSTEMPIC=2;//跳转系统图库请求码

    /**
     * 智能检测边线按钮点击事件的调用方法
     */
    @Override
    public void jumpIntelligenceScanActivity() {
        CardOcrRecogConfigure.getInstance()
                //设置识别返回的语言
                .initLanguage(getApplicationContext())
                //证件类型的ID
                .setnMainId(SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2))
                //证件类型的子ID
                .setnSubID( SharedPreferencesHelper.getInt(
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
                .setSavePath(new DefaultPicSavePath(this,true));
        Intent intent=new Intent(AllCardMainActivity.this,CardsCameraActivity.class);
        startActivityForResult(intent,REQUEST_SCANACTIVITY);
    }

    /**
     * 扫描识别按钮点击事件的跳转方法
     */
    @Override
    public void jumpOriginalScanActivity() {
        CardOcrRecogConfigure.getInstance()
                .initLanguage(getApplicationContext())
                .setSaveCut(true)
                .setOpenIDCopyFuction(true)
                .setnMainId(SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2))
                .setnSubID( SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nSubID", 0))
                .setFlag(0)
                .setnCropType(0)
                .setSavePath(new DefaultPicSavePath(this,true));
        Intent intent=new Intent(AllCardMainActivity.this,CardsCameraActivity.class);
        startActivityForResult(intent,REQUEST_SCANACTIVITY);
    }

    /**
     * 导入识别按钮点击事件的调用方法
     */
    @Override
    public void jumpSystemSelectPic() {
        // Phote Album
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)),
                    REQUEST_SYSTEMPIC);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.install_fillManager), Toast.LENGTH_SHORT).show();
        }

    }
    /** 保存到缓存中 */
    public void saveBitmap(Bitmap bitmap, String tempCachePicturePath) {
        File f = new File(tempCachePicturePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径，然后调用识别
        if (requestCode == REQUEST_SYSTEMPIC && resultCode == Activity.RESULT_OK) {
           /* TextView textView=new TextView(OcrBaseActivity.this);
            textView.setWidth((int)(srcWidth*0.6));
            textView.setHeight((int)(srcWidth*0.2));
            textView.setTextSize(18);
            textView.setGravity(Gravity.CENTER);
            textView.setText(getText(wintone.viewdemo.R.string.distinguish));
            importDialog=new AlertDialog.Builder(OcrBaseActivity.this).setView(textView).setCancelable(true).create();
            importDialog.show();*/
            Uri uri = data.getData();
//            String selectPath = UritoPathUtil.getImageAbsolutePath(this, uri);
//            importPicToRecog(selectPath);
              importPicToRecog(uri);

        }else if(requestCode==REQUEST_SCANACTIVITY&&resultCode==Activity.RESULT_OK){
            //跳转扫描界面识别完成之后，数据回传
            if(data!=null){
                //数据回传的获取
                Bundle bundle=data.getBundleExtra("resultbundle");
                //bundle不为null，代表这识别成功
                if(bundle!=null){
                    CameraScanSucess(bundle);
                }else{
                    String error=data.getStringExtra("error");
                    String StrPath=data.getStringExtra("strpicpath");
                    CameraScanError(error,StrPath);

                }

            }

        }
    }

    /**
     * 进入相机界面扫描成功之后的调用
     */
    private void CameraScanSucess(Bundle bundle) {
        ResultMessage resultMessage=(ResultMessage) bundle.getSerializable("resultMessage");
        String[] picPath=bundle.getStringArray("picpath");
        //数据的封装
        String result=ManageIDCardRecogResult.managerSucessRecogResult(resultMessage,getApplicationContext());
        try {
            /**
             * @param recogResult 识别结果
             * @param picPath 图片路径数组，picPath[0]: 全图路径；picPath[1]: 裁切图；picPath[2]: 证件头像
             */
            Intent intent = new Intent(this,ShowResultActivity.class);
            intent.putExtra("recogResult", result);
            intent.putExtra("fullPagePath",picPath[0]);
            intent.putExtra("cutPagePath",picPath[1]);
            startActivity(intent);
        }catch (Exception e){

        }
    }

    /**
     * 进入相机界面扫描识别识别之后的调用
     *
     * 扫描识别失败，界面的跳转
     *  @param error  -10601 开发码错误,把该文件中的{@link com.kernal.passportreader.sdk.utils.Devcode}中的devcode替换
     *                -10602 applicationId错误，把build.gradle文件中的 applicationId修改为授权文件中绑定的信息
     *                -10603 授权到期，请从新申请授权
     *                -10605  string.xml中的app_name字段属性和授权文件中绑定的不一致
     *                -10606  string.xml中company_name字段属性和授权文件中绑定的不一致
     *                -10608  string.xml中缺少company_name字段，请添加该字段
     *
     */
    private void CameraScanError(String error,String fullpath) {
        try {
            Intent intent = new Intent(this,ShowResultActivity.class);
            intent.putExtra("exception", error);
            intent.putExtra("fullPagePath",fullpath);
            startActivity(intent);
        }catch (Exception e){

        }
    }

    /**
     * 导入识别点击事件触发后，图片路径回传，调用导入识别的方法
     */
    @Override
    public void importPicToRecog(final String picPath) {
        CardOcrRecogConfigure.getInstance()
                .initLanguage(getApplicationContext())
                .setnMainId(SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2))
                .setnSubID( SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nSubID", 0))
                .setSaveCut(true)
                .setOpenIDCopyFuction(true)
                .setSavePath(new DefaultPicSavePath(this,true));
        importRecog = new ImportRecog(AllCardMainActivity.this, AllCardMainActivity.this);
        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                importRecog.startImportRecogService(picPath);
            }
        });
    }

    /**
     * 导入识别点击事件触发后，图片路径回传，调用导入识别的方法
     */
    @Override
    public void importPicToRecog(final Uri uri) {

            CardOcrRecogConfigure.getInstance()
                    .initLanguage(getApplicationContext())
                    .setnMainId(SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nMainId", 2))
                    .setnSubID( SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nSubID", 0))
                    .setSaveCut(true)
                    .setSavePath(new DefaultPicSavePath(this,true));
            importRecog = new ImportRecog(AllCardMainActivity.this, AllCardMainActivity.this);
            ThreadManager.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        importRecog.startImportRecogService(uri,AllCardMainActivity.this);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
    }
    /**
     * 导入识别成功
     * @param resultMessage 识别结果（回到接口）
     * @param picPath 图片路径数组，picPath[0]: 全图路径；picPath[1]: 裁切图；picPath[2]: 证件头像
     */
    @Override
    public void scanOCRIdCardSuccess(ResultMessage resultMessage, String[] picPath) {

        String result=ManageIDCardRecogResult.managerSucessRecogResult(resultMessage,getApplicationContext());
        Intent intent = new Intent(this,ShowResultActivity.class);
        intent.putExtra("recogResult", result);
        intent.putExtra("VehicleLicenseflag", 0);
        intent.putExtra("importRecog", true);
        intent.putExtra("fullPagePath", picPath[0]);
        intent.putExtra("cutPagePath", picPath[1]);
        startActivity(intent);

    }

    /**
     *
     * 导入识别失败（回调接口）
     * @param error -10601 开发码错误,把该文件中的{@link com.kernal.passportreader.sdk.utils.Devcode}中的devcode替换
     *              -10602 applicationId错误，把build.gradle文件中的 applicationId修改为授权文件中绑定的信息
     *              -10603 授权到期，请从新申请授权
     *              -10605  string.xml中的app_name字段属性和授权文件中绑定的不一致
     *              -10606  string.xml中company_name字段属性和授权文件中绑定的不一致
     *              -10608  string.xml中缺少company_name字段，请添加该字段
     */
    @Override
    public void scanOCRIdCardError(String error,String[] picPath) {
        Intent intent = new Intent(this,ShowResultActivity.class);
        intent.putExtra("exception", error);
        intent.putExtra("VehicleLicenseflag", 1);
        intent.putExtra("importRecog", true);
        startActivity(intent);

    }

    /**
     * 序列号授权
     * @param authNumber
     */
    @Override
    public void serialNumberAuth(String authNumber) {
        SerialAuth serialAndImport = new SerialAuth(getApplicationContext(), AllCardMainActivity.this);
        serialAndImport.startAuthService(authNumber, Devcode.devcode);
    }

    /**
     * 序列号授权失败
     * @param error
     */
    @Override
    public void authOCRIdCardError(String error) {
        Toast.makeText(AllCardMainActivity.this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void authOCRIdCardSuccess(String result) {
        Toast.makeText(AllCardMainActivity.this, result, Toast.LENGTH_LONG).show();
    }

    /**
     * 获取证件的类型
     * @return 0：代表可以选择任何的证件类型 2:代表的是身份证的证件类型 6：代表的只能够选择中国行驶证的证件类型
     */
    @Override
    public int getCardId() {
        return 0;
    }
}
