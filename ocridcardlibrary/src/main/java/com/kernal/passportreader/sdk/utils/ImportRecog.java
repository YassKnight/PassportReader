package com.kernal.passportreader.sdk.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;

import java.io.File;
import java.io.FileNotFoundException;

import kernal.idcard.android.RecogParameterMessage;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;
import kernal.idcard.camera.CardOcrRecogConfigure;
import kernal.idcard.camera.IBaseReturnMessage;

/**
 * Time:2018/12/20
 * Author:A@H
 * Description: 和相机不在同一界面的导入图片识别
 */
public class ImportRecog {

    private Context context;
    private int ReturnAuthority;
    public RecogService.recogBinder recogBinder;
    private int nMainId=2;
    private int nSubId=0;
    private volatile IBaseReturnMessage iBaseReturnMessage;
    private String selectPath="";
    private String cutSavePath="";
    private String HeadPicPath="";
    private String fullPicPath="";
    private Bitmap bitmap;
    public ImportRecog(Context context,IBaseReturnMessage iBaseReturnMessage){
        this.context=context;
        this.iBaseReturnMessage=iBaseReturnMessage;
    }
    public void setiBaseReturnMessage(IBaseReturnMessage iBaseReturnMessage) {
        this.iBaseReturnMessage = iBaseReturnMessage;
    }


    public ServiceConnection recogConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            recogBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            recogBinder = (RecogService.recogBinder) service;
            if(recogBinder!=null) {
                RecogParameterMessage rpm = new RecogParameterMessage();
                rpm.nTypeLoadImageToMemory = 0;
                rpm.nMainID =nMainId;
                rpm.nSubID[0] = nSubId;
                rpm.GetSubID = true;
                rpm.GetVersionInfo = true;
                rpm.logo = "";
                rpm.userdata = "";
                rpm.sn = "";
                rpm.authfile = "";
                rpm.isCut = true;
                rpm.triggertype = 0;
                rpm.devcode = Devcode.devcode;
                rpm.isSetIDCardRejectType=CardOcrRecogConfigure.getInstance().isSetIDCardRejectType;
                //nProcessType：0- deleting all instructions 1- cropping 2- rotation  3- cropping and rotation
                // 4- tilt correction 5- cropping+ tilt correction 6- rotation+tile correct
                // 7- cropping+rotation+tilt correction.
                rpm.nProcessType = 7;
                rpm.isOpenIDCopyFuction = CardOcrRecogConfigure.getInstance().isOpenIDCopyFuction;
                rpm.nSetType = 1;// nSetType: 0－Deleting operations，1－setting operations
                if(selectPath!=null&&!selectPath.equals("")){
                    rpm.lpFileName = selectPath; // If rpm.lpFileName is null, auomatic recognition function will be executed.
                }else if(bitmap!=null){
                    rpm.bitmap=bitmap;//Reserve
                    selectPath=fullPicPath;
                }
                rpm.lpHeadFileName =HeadPicPath ;//Save portrait of identity document
                rpm.isSaveCut = CardOcrRecogConfigure.getInstance().isSaveCut;// Save cropping picture false=not saving  true=saving
                rpm.cutSavePath=cutSavePath;
                if (nMainId== 2) {
                    rpm.isAutoClassify = true;
                    rpm.isOnlyClassIDCard = true;
                } else if (nMainId== 3000) {
                    rpm.nMainID = 1034;
                }
                // end
                try {
                    ResultMessage resultMessage;
                    resultMessage = recogBinder.getRecogResult(rpm);
                    if (resultMessage.ReturnAuthority == 0
                            && resultMessage.ReturnInitIDCard == 0
                            && resultMessage.ReturnLoadImageToMemory == 0
                            && resultMessage.ReturnRecogIDCard > 0) {
                        if(iBaseReturnMessage!=null){
                            String[] picPath={selectPath,cutSavePath,HeadPicPath};
                            iBaseReturnMessage.scanOCRIdCardSuccess(resultMessage,picPath);
                        }
                    } else {
                        if(iBaseReturnMessage!=null){
                            String error=ManageIDCardRecogResult.managerErrorRecogResult(context,resultMessage);
                            String[] picPath={selectPath};
                            iBaseReturnMessage.scanOCRIdCardError(error,picPath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(bitmap!=null&&!bitmap.isRecycled()){
                        bitmap.isRecycled();
                        bitmap=null;
                    }
                    if (recogBinder != null) {
                        context.unbindService(recogConn);
                    }
                }

            }
        }
    };

    public void startImportRecogService(String selectPath){
        this.selectPath=selectPath;
        if(CardOcrRecogConfigure.getInstance().isSaveCut){
            cutSavePath=CardOcrRecogConfigure.getInstance().savePath.getCropPicPath();
        }
        if(CardOcrRecogConfigure.getInstance().isSaveHeadPic){
            HeadPicPath=CardOcrRecogConfigure.getInstance().savePath.getHeadPicPath();
        }
        nMainId=CardOcrRecogConfigure.getInstance().nMainId;
        nSubId=CardOcrRecogConfigure.getInstance().nSubID;
        RecogService.nMainID =CardOcrRecogConfigure.getInstance().nMainId;
        RecogService.isRecogByPath = true;
        Intent recogIntent = new Intent(context,
                RecogService.class);
        context.bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
    }
    public void startImportRecogService(Uri uri,Context context) throws FileNotFoundException {
        this.bitmap= BitmapFactory.decodeFileDescriptor(context.getContentResolver().openFileDescriptor(uri,"r").getFileDescriptor());
        if(CardOcrRecogConfigure.getInstance().isSaveCut){
            cutSavePath=CardOcrRecogConfigure.getInstance().savePath.getCropPicPath();
        }
        if(CardOcrRecogConfigure.getInstance().isSaveHeadPic){
            HeadPicPath=CardOcrRecogConfigure.getInstance().savePath.getHeadPicPath();
        }
        if(CardOcrRecogConfigure.getInstance().isSaveFullPic){
            fullPicPath=CardOcrRecogConfigure.getInstance().savePath.getFullPicPath();
            ActivityRecogUtils.copyFile(context,uri,new File(fullPicPath));
        }
        nMainId=CardOcrRecogConfigure.getInstance().nMainId;
        nSubId=CardOcrRecogConfigure.getInstance().nSubID;
        RecogService.nMainID =CardOcrRecogConfigure.getInstance().nMainId;
        RecogService.isRecogByPath = true;
        Intent recogIntent = new Intent(context,
                RecogService.class);
        context.bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
    }
}
