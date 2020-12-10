package com.kernal.passportreader.sdk;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.widget.Toast;

import com.kernal.passportreader.sdk.utils.CardScreenUtil;
import com.kernal.passportreader.sdk.utils.Devcode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import kernal.idcard.android.Frame;
import kernal.idcard.android.RecogParameterMessage;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;
import kernal.idcard.camera.CardOcrRecogConfigure;
import kernal.idcard.camera.OcrIdCardRecogParams;

/**
 * @author A@H
 *预览识别线程
 */
public class OcrRecogTask implements Runnable {
    private int LoadBufferImage = -1;
    private int ConfirmSideSuccess = -1;
    private int CheckPicIsClear = -1;
    private OcrIdCardRecogParams ocrIdCardRecogParams;
    private Context context;
    private int nMainIDX;
    private int nCropType;
    private int nSubID;
    private String picPathString = "";
    private String HeadJpgPath = "";
    private String cutPicPath = "";
    private Frame frame = new Frame();
    public byte[] data;
    private int[] NV21Point = new int[4];
    private boolean isTakePic = false;


    private volatile boolean isOpendetectLightspot = false;
    private int detectLightspot = -2;
    public static String importPicPath = "";

    public OcrRecogTask(Context context, OcrIdCardRecogParams ocrIdCardRecogParams) {
        this.context = context;
        this.ocrIdCardRecogParams = ocrIdCardRecogParams;
        nMainIDX = CardOcrRecogConfigure.getInstance().nMainId;
        nSubID = CardOcrRecogConfigure.getInstance().nSubID;
        nCropType = CardOcrRecogConfigure.getInstance().nCropType;

    }

    public void setnMainIDX(int nMainIDX) {
        this.nMainIDX = nMainIDX;
    }

    public OcrRecogTask setData(byte[] data) {
        this.data = data;
        return this;
    }

    public OcrRecogTask setNv21Point(int[] Nv21Point) {
        NV21Point = Nv21Point;
        return this;
    }

    public OcrRecogTask setTakePic(boolean takePic) {
        isTakePic = takePic;
        return this;
    }

    public OcrRecogTask setOpendetectLightspot(boolean opendetectLightspot) {
        isOpendetectLightspot = opendetectLightspot;
        return this;
    }

    @Override
    public void run() {
        if (ocrIdCardRecogParams.recogBinder != null) {
            //拍照识别流程
            if (isTakePic) {
                if (RecogService.isRecogByPath) {
                    picPathString = importPicPath;
                } else {
                    picPathString = CardOcrRecogConfigure.getInstance().savePath.getFullPicPath();
                    RecogService.isRecogByPath = true;
                    saveFullPic(picPathString);
                }
                cutPicPath = CardOcrRecogConfigure.getInstance().savePath.getCropPicPath();
                HeadJpgPath = CardOcrRecogConfigure.getInstance().savePath.getHeadPicPath();
                ocrIdCardRecogParams.scanICamera.stopPreview();
                getRecogResult();
            } else {
                if (nMainIDX != 3000) {
                    /**
                     * 识别普通证件，视频流识别流程
                     */
                    //ocrIdCardRecogParams.recogBinder.SetParameter(1, nMainIDX);
                    //设置敏感区域的坐标点
                    ocrIdCardRecogParams.recogBinder.SetROI(NV21Point[0], NV21Point[1], NV21Point[2], NV21Point[3]);
                    //设置图片的旋转的方向
                    if (nCropType == 0) {
                        ocrIdCardRecogParams.recogBinder.SetRotateType(ocrIdCardRecogParams.Orientation);
                    } else {
                        ocrIdCardRecogParams.recogBinder.SetRotateType(0);
                    }
                    //设置视频流识别的方式
                    ocrIdCardRecogParams.recogBinder.SetVideoStreamCropTypeEx(nCropType);
                    //加载图片至核心
                    if (data != null && data.length > 0) {
                        LoadBufferImage = ocrIdCardRecogParams.recogBinder.LoadBufferImageEx(data,
                                ocrIdCardRecogParams.preWidth, ocrIdCardRecogParams.preHeight, 24, 0);
                    }
                    //设置清晰度阈值
                    if (nCropType == 1) {
                        ocrIdCardRecogParams.recogBinder.SetPixClearEx(40);
                    }
                    if (CardOcrRecogConfigure.getInstance().flag != 0) {
                        if (nMainIDX == 5 || nMainIDX == 6) {
                            ocrIdCardRecogParams.recogBinder.SetConfirmSideMethod(1);
                        } else if (nMainIDX == 13 || nMainIDX == 9
                                || nMainIDX == 10 || nMainIDX == 11 || nMainIDX == 12) {
                            ocrIdCardRecogParams.recogBinder.SetConfirmSideMethod(2);
                            ocrIdCardRecogParams.recogBinder.IsDetectRegionValid(1);
                        } else {
                            if (nMainIDX == 3 || nMainIDX == 2) {
                                ocrIdCardRecogParams.recogBinder.SetConfirmSideMethod(0);
                                ocrIdCardRecogParams.recogBinder.IsDetectRegionValid(1);
                                ocrIdCardRecogParams.recogBinder.IsDetect180Rotate(1);
                                ocrIdCardRecogParams.recogBinder.SetDetectIDCardType(CardOcrRecogConfigure.getInstance().flag);
                            } else {
                                ocrIdCardRecogParams.recogBinder.SetConfirmSideMethod(4);
                            }
                        }
                    }
                    if (LoadBufferImage == 0) {
                        //检测图片的边线
                        ConfirmSideSuccess = ocrIdCardRecogParams.recogBinder
                                .ConfirmSideLineEx(0);
                        if (ConfirmSideSuccess >= 0) {
                            detectLightspot=-2;
                            if (isOpendetectLightspot) {
                                //检测光斑
                                detectLightspot = ocrIdCardRecogParams.recogBinder.DetectLightspot();
                            }
                               //检测图片的清晰度
                            CheckPicIsClear = ocrIdCardRecogParams.recogBinder
                                    .CheckPicIsClearEx();
                        }
                    }
                    if (nCropType == 1) {
                        //智能检测边线，获取图片上的四个坐标点
                        ocrIdCardRecogParams.recogBinder.GetFourSideLines(frame);
                        //根据返回的图片的四个坐标点，换算成屏幕上的坐标点（注:屏幕上坐标点=图片上的坐标点*（屏幕的分辨率/图片的分辨率））
                    //    WriteUtil.writeLog("竖屏的坐标点,左("+frame.ltStartX+","+frame.ltStartY+")"+
                      //          "上("+frame.lbStartX+","+frame.lbStartY+")"+
                      //          "右("+frame.rtStartX+","+frame.rtStartY+")"+
                        //        "下("+frame.rbStartX+","+frame.rbStartY+")");

                            frame.ltStartX = (int) (frame.ltStartX * ocrIdCardRecogParams.scale);
                            frame.ltStartY = (int) (frame.ltStartY * ocrIdCardRecogParams.scale);
                            frame.lbStartX = (int) (frame.lbStartX * ocrIdCardRecogParams.scale);
                            frame.lbStartY = (int) (frame.lbStartY * ocrIdCardRecogParams.scale);
                            frame.rtStartX = (int) (frame.rtStartX * ocrIdCardRecogParams.scale);
                            frame.rtStartY = (int) (frame.rtStartY * ocrIdCardRecogParams.scale);
                            frame.rbStartX = (int) (frame.rbStartX * ocrIdCardRecogParams.scale);
                            frame.rbStartY = (int) (frame.rbStartY * ocrIdCardRecogParams.scale);

                    }
                    ocrIdCardRecogParams.scanICamera.UpdateFrame(frame, ConfirmSideSuccess,detectLightspot);
                    //图片加载成功&&检测边线成功&&检测清晰度成功&&没有光斑，进行识别
                    if (LoadBufferImage == 0 && ConfirmSideSuccess >=0
                            && CheckPicIsClear == 0 && detectLightspot != 0) {
                        picPathString = CardOcrRecogConfigure.getInstance().savePath.getFullPicPath();
                        cutPicPath = CardOcrRecogConfigure.getInstance().savePath.getCropPicPath();
                        if (CardOcrRecogConfigure.getInstance().isSaveHeadPic) {
                            HeadJpgPath = CardOcrRecogConfigure.getInstance().savePath.getHeadPicPath();
                        }
                        RecogService.isRecogByPath = false;
                        getRecogResult();
                    } else {
                        ocrIdCardRecogParams.scanICamera.addPreviewCallBack();
                    }
                } else {
                    /**
                     * 识别机读码
                     */
                    ocrIdCardRecogParams.recogBinder.SetROI(NV21Point[0], NV21Point[1], NV21Point[2], NV21Point[3]);
                    if (CardScreenUtil.getScreenOrientation(context) == CardScreenUtil.ORIENTATION_PORTRAIT) {
                        ocrIdCardRecogParams.recogBinder.SetRotateType(1);
                    } else {
                        ocrIdCardRecogParams.recogBinder.SetRotateType(0);
                    }
                    LoadBufferImage = ocrIdCardRecogParams.recogBinder.LoadBufferImageEx(data,
                            ocrIdCardRecogParams.preWidth, ocrIdCardRecogParams.preHeight, 24, 0);
                    if (LoadBufferImage == 0) {
                        ConfirmSideSuccess = ocrIdCardRecogParams.recogBinder.ConfirmSideLineEx(0);
                        if (ConfirmSideSuccess == 1034
                                || ConfirmSideSuccess == 1033
                                || ConfirmSideSuccess == 1036) {
                            CheckPicIsClear = ocrIdCardRecogParams.recogBinder.CheckPicIsClearEx();
                        }
                    }
                    boolean isConfirmSuccess = (ConfirmSideSuccess == 1034 || ConfirmSideSuccess == 1033 || ConfirmSideSuccess == 1036)
                            && CheckPicIsClear == 0;
                    if (isConfirmSuccess) {
                        nMainIDX = ConfirmSideSuccess;
                        picPathString = CardOcrRecogConfigure.getInstance().savePath.getFullPicPath();
                        cutPicPath = CardOcrRecogConfigure.getInstance().savePath.getCropPicPath();
                        getRecogResult();
                    } else {
                        ocrIdCardRecogParams.scanICamera.addPreviewCallBack();
                    }
                }
            }
        } else {
            ocrIdCardRecogParams.scanICamera.addPreviewCallBack();
        }
    }

    public void getRecogResult() {
        ResultMessage resultMessage;
        RecogParameterMessage rpm = new RecogParameterMessage();
        rpm.nTypeLoadImageToMemory = 0;
        rpm.nMainID = nMainIDX;
        rpm.nSubID[0] = nSubID;
        rpm.GetSubID = true;
        rpm.GetVersionInfo = true;
        rpm.logo = "";
        rpm.userdata = "";
        rpm.sn = "";
        rpm.authfile = "";
        rpm.isSaveCut = CardOcrRecogConfigure.getInstance().isSaveCut;
        rpm.triggertype = 0;
        rpm.devcode = Devcode.devcode;
        rpm.isOnlyClassIDCard = true;
        rpm.isOpenGetThaiFeatureFuction = CardOcrRecogConfigure.getInstance().isOpenGetThaiFeatureFuction;
        rpm.isOpenIDCopyFuction = CardOcrRecogConfigure.getInstance().isOpenIDCopyFuction;
        rpm.isSetIDCardRejectType = CardOcrRecogConfigure.getInstance().isSetIDCardRejectType;
        // rpm.idcardRotateDegree=3;
        if (nMainIDX == 2) {
            rpm.isAutoClassify = true;
            rpm.lpHeadFileName = HeadJpgPath;//  save document protrait
            rpm.lpFileName = picPathString; //  If rpm.lpFileName is null, automatic recognition fuction will be executed
            rpm.cutSavePath = cutPicPath;
        } else {
            rpm.lpHeadFileName = HeadJpgPath;// save document protrait
            rpm.lpFileName = picPathString; //  If rpm.lpFileName is null, automatic recognition fuction will be executed.
            rpm.cutSavePath = cutPicPath;
        }
        rpm.isCut = false;
        try {
            resultMessage = ocrIdCardRecogParams.recogBinder.getRecogResult(rpm);
            if (resultMessage.ReturnAuthority == 0
                    && resultMessage.ReturnInitIDCard == 0
                    && resultMessage.ReturnLoadImageToMemory == 0
                    && resultMessage.ReturnRecogIDCard > 0) {

                if (!isTakePic&&CardOcrRecogConfigure.getInstance().isSaveFullPic) {
                    saveFullPic(picPathString);
                }else if(!CardOcrRecogConfigure.getInstance().isSaveFullPic){
                    deleteFullPic(picPathString);
                }
                String[] picPath = {picPathString, cutPicPath, HeadJpgPath};
                ocrIdCardRecogParams.scanICamera.recogSucessUpdateUi(resultMessage, picPath);
            } else {
                //识别返回错误值 非拍照-6 继续预览，拍照-6，finish()掉相机
                     String[] picPath={""};
                    if(!CardOcrRecogConfigure.getInstance().isSaveFullPic){
                        deleteFullPic(picPathString);
                    }else{
                        picPath[0]=picPathString;
                    }
                    ocrIdCardRecogParams.scanICamera.recogErrorUpdateUi(resultMessage,picPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RecogService.isRecogByPath = false;
        }
    }


    private void deleteFullPic(String picPathString){
        if(picPathString!=null&&!picPathString.equals("")){
            File file=new File(picPathString);
            if(file.exists()){
                file.delete();
            }
        }


    }
    /**
     * store the full picture
     *
     * @param picPathString1
     */
    private void saveFullPic(String picPathString1) {
        // TODO Auto-generated method stub
        //store the full picture start
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, ocrIdCardRecogParams.preWidth,
                ocrIdCardRecogParams.preHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, ocrIdCardRecogParams.preWidth,
                        ocrIdCardRecogParams.preHeight),
                100, baos);

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(picPathString1);
            outStream.write(baos.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch
            // block
            e.printStackTrace();
        } finally {
            try {
                outStream.close();
                baos.close();
            } catch (Exception e) {
            }
        }
    }
}
