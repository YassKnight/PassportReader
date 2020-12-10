package com.kernal.passportreader.sdk.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kernal.passportreader.sdk.utils.CameraConfigurationManager;
import com.kernal.passportreader.sdk.utils.CardScreenUtil;

/**
 * @author A@H
 * @describle 自定义预览View,主要是相机预览的开启与关闭，对焦方式以及闪光灯
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private Camera mCamera;
    private boolean mPreviewing = true;
    private boolean mSurfaceCreated = false;
    private Camera.PreviewCallback mpreviewCallback;
    private CameraConfigurationManager mCameraConfigurationManager;

    public CameraPreview(Context context) {
        super(context);
    }

    public void setCamera(Camera camera, Camera.PreviewCallback previewCallback) {
        mCamera = camera;
        mpreviewCallback=previewCallback;
        if (mCamera != null) {
            mCameraConfigurationManager = new CameraConfigurationManager(getContext());
            mCameraConfigurationManager.initFromCameraParameters(mCamera);
            getHolder().addCallback(this);
            if (mPreviewing) {
                   requestLayout();
            } else {
                  showCameraPreview();
          }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        post(new Runnable() {
            @Override
            public void run() {
                showCameraPreview();
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = true;
                mCamera.setPreviewDisplay(getHolder());
                mCameraConfigurationManager.setDesiredCameraParameters(mCamera);
                mCamera.setOneShotPreviewCallback(mpreviewCallback);
                mCamera.startPreview();
                mCamera.autoFocus(autoFocusCB);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void stopCameraPreview() {
        if (mCamera != null) {
            try {
                removeCallbacks(doAutoFocus);
                mPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();

            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void openFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.openFlashlight(mCamera);
        }
    }

    public void closeFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.closeFlashlight(mCamera);
        }
    }



    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        if (mCameraConfigurationManager != null && mCameraConfigurationManager.getCameraResolution() != null) {
            Point cameraResolution = mCameraConfigurationManager.getCameraResolution();
            // 取出来的cameraResolution高宽值与屏幕的高宽顺序是相反的
            int cameraPreviewWidth = cameraResolution.x;
            int cameraPreviewHeight = cameraResolution.y;
            if (width * 1f / height < cameraPreviewWidth * 1f / cameraPreviewHeight) {
                float ratio = cameraPreviewHeight * 1f / cameraPreviewWidth;
                width = (int) (height / ratio + 0.5f);
            } else {
                float ratio = cameraPreviewWidth * 1f / cameraPreviewHeight;
                height = (int) (width / ratio + 0.5f);
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }


    private boolean flashLightAvailable() {
        return mCamera != null && mPreviewing && mSurfaceCreated && getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private Runnable doAutoFocus = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && mPreviewing && mSurfaceCreated) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (Exception e) {
                }
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                postDelayed(doAutoFocus, 1500);
            } else {
                postDelayed(doAutoFocus, 500);
            }
        }
    };

    public Point getPreViewSize() {
        if (mCameraConfigurationManager != null) {
            return mCameraConfigurationManager.getmPreviewResolution();
        }
        return null;
    }

    public float getScale() {
        float scale=1.0f;
        if (mCameraConfigurationManager != null) {
            float width=0;
            float height=0;
            if(ViewfinderView.width==0||ViewfinderView.height==0){
                width=(float)CardScreenUtil.getScreenResolution(getContext()).x;
                height=(float)CardScreenUtil.getScreenResolution(getContext()).y;
            }else{
                width=ViewfinderView.width;
                height=ViewfinderView.height;
            }
             float Wscale=((float)mCameraConfigurationManager.getCameraResolution().x /width);
             float Hscale=((float)mCameraConfigurationManager.getCameraResolution().y /height);
              scale=(Wscale<Hscale)?Wscale:Hscale;
              if(CardScreenUtil.getScreenOrientation(getContext())==CardScreenUtil.ORIENTATION_PORTRAIT){
                 ViewfinderView.portraitWidth=(int)(mCameraConfigurationManager.getCameraResolution().x/scale);
              }
            return scale;
        }
        return scale;
    }

    public float getHscale(){
        float Hscale=((float)mCameraConfigurationManager.getCameraResolution().y /(float) CardScreenUtil.getScreenResolution(getContext()).y);
        return Hscale;
    }


}