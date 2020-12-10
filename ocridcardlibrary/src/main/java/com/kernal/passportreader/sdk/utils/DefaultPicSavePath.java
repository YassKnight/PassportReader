package com.kernal.passportreader.sdk.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;

import androidx.annotation.NonNull;

import java.io.File;

import kernal.idcard.camera.SavePath;

/**
 * @author A@H
 * Description: 该类实现了SavePath方法 主要是生成图片的路径
 */
public class DefaultPicSavePath implements SavePath {
    public String PATH ;
    private String parentPath;
    private boolean saveOnce=false;


    public DefaultPicSavePath(Context context){
        PATH= context.getFilesDir().getPath()+ "/wtimage/";
        File file=new File(PATH);
        if(!file.exists()){
            file.mkdirs();
        }
         parentPath=PATH;
    }

    /**
     * 图片只存储一次，之后覆盖
     * @param onlyOne
     */
    public DefaultPicSavePath(Context context,boolean onlyOne){
        PATH= context.getFilesDir().getPath()+ "/wtimage/";
        File file=new File(PATH);
        if(!file.exists()){
            file.mkdirs();
        }
        parentPath=PATH;
        saveOnce=onlyOne;
    }

    public DefaultPicSavePath(@NonNull String parentPath){
        if(parentPath!=null&&!parentPath.equals("")){
            this.parentPath=parentPath;
        }else{
            this.parentPath=PATH;
        }
        File file=new File(parentPath);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    @Override
    public String getCropPicPath() {
        String cropPicPath="";
        if(saveOnce){
            cropPicPath= parentPath+"Android_WintoneIDCard_"+"0000002"+"thaiCropCode.jpg";
        }else{
            cropPicPath= parentPath+"Android_WintoneIDCard_"+pictureName()+"thaiCropCode.jpg";
        }
        return cropPicPath;
    }

    @Override
    public String getFullPicPath() {
        String fullPicPath="";
        if(saveOnce){
            fullPicPath= parentPath+"Android_WintoneIDCard_"+"0000001"+"thaiFullCode.jpg";
        }else{
            fullPicPath= parentPath+"Android_WintoneIDCard_"+pictureName()+"thaiFullCode.jpg";
        }

        return fullPicPath;
    }

    @Override
    public String getHeadPicPath() {
        String headPicPath="";
        if(saveOnce){
            headPicPath= parentPath+"Android_WintoneIDCard_"+"0000003"+"thaiHeadCode.jpg";
        }else{
            headPicPath= parentPath+"Android_WintoneIDCard_"+pictureName()+"thaiHeadCode.jpg";
        }

        return headPicPath;
    }

    @Override
    public String getThaiCodePath() {
        String thaiCoddPath="";
        if(saveOnce){
            thaiCoddPath= parentPath+"Android_WintoneIDCard_"+"0000004"+"thaicode.jpg";
        }else{
            thaiCoddPath= parentPath+"Android_WintoneIDCard_"+pictureName()+"thaicode.jpg";
        }

        return thaiCoddPath;
    }

    public String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow(); //  Get system time。
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        if (month < 10)
            str = String.valueOf(year) + "0" + String.valueOf(month);
        else {
            str = String.valueOf(year) + String.valueOf(month);
        }
        if (date < 10) {
            str = str + "0" + String.valueOf(date);
        } else {
            str = str + String.valueOf(date);
        }
        if (hour < 10)
            str = str + "0" + String.valueOf(hour);
        else {
            str = str + String.valueOf(hour);
        }
        if (minute < 10)
            str = str + "0" + String.valueOf(minute);
        else {
            str = str + String.valueOf(minute);
        }
        if (second < 10)
            str = str + "0" + String.valueOf(second);
        else {
            str = str + String.valueOf(second);
        }
        return str;
    }

}
