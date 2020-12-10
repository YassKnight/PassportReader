package com.kernal.passportreader.sdk.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.kernal.passportreader.sdk.view.ViewfinderView;


public class CardScreenUtil {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;


    public static final int getScreenOrientation(Context context) {
        Point screenResolution = getScreenResolution(context);
        return screenResolution.x > screenResolution.y ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT;
    }


    public static int getPicOrientation(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int ScreenOrientation=display.getRotation();
        switch (ScreenOrientation){
            case 0:
                if(ViewfinderView.isSameScreen){
                    return 1;
                }else{
                    return 0;
                }
            case 1:
                return 0;
            case 2:
                if(ViewfinderView.isSameScreen){
                    return 3;
                }else{
                    return 2;
                }
            case 3:
                return 2;

        }
        return 0;
    }

    public static Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            if(Build.VERSION.SDK_INT>=19){
                display.getRealSize(screenResolution);
            }else {
                display.getSize(screenResolution);
            }

        } else {
            screenResolution.set(display.getWidth(), display.getHeight());
        }
        return screenResolution;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

 }