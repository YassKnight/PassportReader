package com.kernal.passportreader.sdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.kernal.passportreader.sdk.utils.CardScreenUtil;

import kernal.idcard.android.Frame;

/**
 * @author A@H
 * @describle 扫描识别界面的自定义的扫描识别框
 */
public final class ViewfinderView extends View {
    private int checkLeftFrame = 0;// Test if the left of a document is existing or aligned.
    private int checkTopFrame = 0;// Test if the upper part of a document is existing or aligned.
    private int checkRightFrame = 0;// Test if the right of a document is existing or aligned.
    private int checkBottomFrame = 0;// Test if the lower part of a document is existing or aligned.
    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    /**
     * the time to refresh interface
     */
    private static final long ANIMATION_DELAY = 50L;

    /**
     * To judge directional value correspondig to rotation degree of the screen ：0,1,2,3
     */
    private static int directtion = 0;

    /**
     * When idcardType=0, it will turn to MRZ for recognition. WhenidcardType=1, it will turn to the full page for recognition.
     */
    private int idcardType = 2;
    private int nCropType = 0;
    public int barWidth, barHeight;
    private Paint paint;
    private int scannerAlpha;
    /**
     * The top of middle sliding line
     */
    private int slideTop;
    private int slideTop1;

    /**
     * The bottom of middle sliding line
     */
    private int slideBottom;
    /**
     * The movement length of middle line after refreshing each time
     */
    private static final int SPEEN_DISTANCE = 10;
    /**
     * Width of line in scanning frame
     */
    private static final int MIDDLE_LINE_WIDTH = 6;
    private boolean isFirst = false;
    /**
     * Width of frames all around
     */
    private static final int FRAME_LINE_WIDTH = 4;
    public static int FRAMECOLOR = 0;//Color of frames all around
    private Rect frame;
    public  static int width;
    public  static int height;
    public static int portraitWidth;
    private Frame fourLines = new Frame();
    //该参数是设置竖屏状态下，框线识别模式中，框的方向，true：width>height; false:height>width
    public static boolean isSameScreen=true;

    public void setFourLines(Frame fourLines, String str) {
        this.fourLines = fourLines;
        /**
         *  When we get the result, we update the full screen content.
         */
        postInvalidateDelayed(ANIMATION_DELAY, 0, 0, width, height);
    }

    /**
     *
     * @param context
     * @param nCropType 框的方式
     * @param checkFrame
     */
    public ViewfinderView(Context context,  int nCropType,int checkFrame) {
        super(context);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        paint = new Paint();
        FRAMECOLOR=0;
        scannerAlpha = 0;
        this.nCropType = nCropType;

        directtion = CardScreenUtil.getScreenOrientation(context);
        checkLeftFrame=checkFrame;
        checkBottomFrame=checkFrame;
        checkRightFrame=checkFrame;
        checkTopFrame=checkFrame;
        width=0;
        height=0;

    }
    public void setIdcardType(int idcardType) {
        this.idcardType = idcardType;
    }
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Rect getFrame() {
        return frame;
    }
    @Override
    public void onDraw(Canvas canvas) {
        if(width==0||height==0){
            width = canvas.getWidth();
            height= canvas.getHeight();
        }
        // For a general device, when its width is less than height, the rotation degree is 0 or 2; But for Samsung, when its wideth is greater than height, the rotation degree is 0 or 2.
        if (directtion == 0 || directtion == 2) {
            if (width > height) {
                if (!Build.MODEL.equals("GT-P7500")
                        && !Build.MODEL.equals("SM-T520")) {
                    if ((float) width / height == (float) 4 / 3) {
                        width = 3 * width / 4;
                    }
                }
                //a special device
                drawLine(canvas, width, height, false);
            } else {
                drawLine(canvas, width, height, true);
            }
            // For a general device, when its width is more than height, the rotation degree is 1 or 3;  when its wideth is less than height, the rotation degree is 1 or 3.
        } else if (directtion == 1 || directtion == 3) {
            if (width > height) {
                drawLine(canvas, width, height, false);
            } else {
                //a special device
                drawLine(canvas, width, height, true);
            }
        }

    }

    public void drawLine(Canvas canvas, int width, int height, boolean isPortrait) {
        if (idcardType == 3000) {
            if (!isPortrait) {//  a special device
                frame = new Rect((int) (width * 0.2), height / 3,
                        (int) (width * 0.85), 2 * height / 3);
            } else {
                // MRZ recognition
                /**
                 * The rectangular is the frame shown in the middle.
                 */
                if(isSameScreen){
                    frame = new Rect((int) (width * 0.05), (int) (height * 0.4),
                            (int) (width * 0.95), (int) (0.6 * height));
                }else{
                    frame = new Rect((int) (width * 0.05), (int) (height * 0.4),
                            (int) (width * 0.95), (int) (0.6 * height));
                }

            }
        } else if (idcardType == 2 || idcardType == 4 || idcardType == 1013 || idcardType == 22
                || idcardType == 1030 || idcardType == 1031
                || idcardType == 1032 || idcardType == 1005
                || idcardType == 1001 || idcardType == 2001
                || idcardType == 2004 || idcardType == 2002
                || idcardType == 2003 || idcardType == 14
                || idcardType == 15 || idcardType == 25 || idcardType == 26) {
            if (!isPortrait) {//  a special device
                frame = new Rect((int) (width * 0.2),
                        (int) (height - 0.41004673 * width) / 2,
                        (int) (width * 0.85),
                        (int) (height + 0.41004673 * width) / 2);
            } else {
                if(isSameScreen){
                    frame = new Rect((int) (width * 0.025),
                            (int) (height - 0.59375 * width) / 2,
                            (int) (width * 0.975),
                            (int) (height + 0.59375 * width) / 2);
                }else{
                    frame = new Rect((int) (width - 0.41004673 * height) / 2,
                            (int) (height * 0.2),
                            (int) (width + 0.41004673 * height) / 2,
                            (int) (height * 0.85));

                }
            }
        } else if (idcardType == 5 || idcardType == 6) {
            if (!isPortrait) {
                //  a special device
                frame = new Rect((int) (width * 0.24),
                        (int) (height - 0.41004673 * width) / 2,
                        (int) (width * 0.81),
                        (int) (height + 0.41004673 * width) / 2);
            } else {
                if(isSameScreen){
                    frame = new Rect((int) (width * 0.025),
                            (int) (height - 0.64 * width) / 2,
                            (int) (width * 0.975),
                            (int) (height + 0.64 * width) / 2);
                }else{
                    frame = new Rect((int) (width - 0.41004673 * height) / 2,
                            (int) (height * 0.24),
                            (int) (width + 0.41004673 * height) / 2,
                            (int) (height * 0.81));

                }
            }
        } else {
            if (!isPortrait) {//  a special device
                frame = new Rect((int) (width * 0.2),
                        (int) (height - 0.45 * width) / 2,
                        (int) (width * 0.85),
                        (int) (height + 0.45 * width) / 2);
            } else {
                // for page
                if(isSameScreen){
                    frame = new Rect((int) (width * 0.025),
                            (int) (height - 0.659 * width) / 2,
                            (int) (width * 0.975),
                            (int) (height + 0.659 * width) / 2);
                }else{
                    frame = new Rect(
                            (int) (width - 0.45 * height) / 2,
                            (int) (height * 0.2),
                            (int) (width + 0.45 * height) / 2,
                            (int) (height * 0.85));
                }
            }

        }
        // }
        if (frame == null) {
            return;
        }
        // the highest and lowest point of initialization middle sliding line
        if (!isFirst) {
            isFirst = true;
            slideTop = height / 3;
            slideBottom = 2 * height / 3;
            slideTop1 = width / 2;
        }
        // Sketch the shady part out of scanning frame. There are four sections. The upper part of both scanning frame and scree and the lower part of both scanning frame and screen
        // The left part of both scanning frame and scree and right part of both scanning frame and screen
        if (nCropType == 0) {
            paint.setColor(Color.argb(48, 0, 0, 0));
            canvas.drawRect(0, 0, width, frame.top, paint);
            canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
            canvas.drawRect(frame.right + 1, frame.top, width,
                    frame.bottom + 1, paint);
            canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        }
        paint.setColor(Color.rgb(238, 65, 86));
        // Draw two-pixel wide geen wireframes or red frames.
        if (FRAMECOLOR == 0) {
            paint.setColor(Color.rgb(77, 223, 68));

        } else {
            paint.setColor(FRAMECOLOR);

        }
        if (idcardType == 3000) {
            canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                    frame.right - FRAME_LINE_WIDTH + 2, frame.top
                            + FRAME_LINE_WIDTH, paint);// top line
            canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                    frame.left + FRAME_LINE_WIDTH + 2, frame.bottom
                            + FRAME_LINE_WIDTH, paint);// left line
            canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                    frame.right - FRAME_LINE_WIDTH + 2, frame.bottom
                            + FRAME_LINE_WIDTH, paint);// right line
            canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                    frame.bottom, frame.right - FRAME_LINE_WIDTH + 2,
                    frame.bottom + FRAME_LINE_WIDTH, paint);// bottom line
            //  skeetch one red line which can scan consistenly
            // paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        } else {
            if (nCropType == 0) {
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH - 2 + 50, frame.top
                                + FRAME_LINE_WIDTH, paint);
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH + 2, frame.top + 50,
                        paint);//  Top left corner
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.top + 50,
                        paint);
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                        frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.top + FRAME_LINE_WIDTH, paint);// Top right corner
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                        frame.bottom - 50, frame.left + FRAME_LINE_WIDTH + 2,
                        frame.bottom, paint);
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.bottom
                        - FRAME_LINE_WIDTH, frame.left + FRAME_LINE_WIDTH - 2
                        + 50, frame.bottom, paint); // Left bottom
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                        frame.bottom - 50, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.bottom, paint);
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                        frame.bottom - FRAME_LINE_WIDTH, frame.right
                                - FRAME_LINE_WIDTH - 2, frame.bottom, paint); //  Right bottom
            } else if (nCropType == 1) {
                //  a special device lenovo
                if (!isPortrait) {
                    if (!(fourLines.ltStartX == barWidth
                            && fourLines.ltStartY == barHeight
                            && fourLines.rtStartX == barWidth
                            && fourLines.rtStartY == barHeight
                            && fourLines.lbStartX == barWidth
                            && fourLines.lbStartY == barHeight
                            && fourLines.rbStartX == barWidth && fourLines.rbStartY == barHeight)) {
                        paint.setStrokeWidth(6);
                        paint.setAntiAlias(true);
                        float length = 3;

                        canvas.drawLine(fourLines.ltStartX, fourLines.ltStartY,
                                fourLines.lbStartX, fourLines.lbStartY, paint);// left line
                        canvas.drawLine(fourLines.ltStartX, fourLines.ltStartY,
                                fourLines.rtStartX, fourLines.rtStartY, paint);// top line
                        canvas.drawLine(fourLines.rtStartX, fourLines.rtStartY,
                                fourLines.rbStartX, fourLines.rbStartY, paint);// right line
                        canvas.drawLine(fourLines.lbStartX, fourLines.lbStartY,
                                fourLines.rbStartX, fourLines.rbStartY, paint);// bottom line
                        paint.setColor(Color.argb(80, 255, 255, 255));
                        Path topPath = new Path();
                        topPath.moveTo(fourLines.lbStartX, fourLines.lbStartY);
                        topPath.lineTo(fourLines.ltStartX, fourLines.ltStartY);
                        topPath.lineTo(width, 0);
                        topPath.lineTo(0, 0);
                        topPath.close();// Closure
                        canvas.drawPath(topPath, paint);
                        Path leftPath = new Path();
                        leftPath.moveTo(0, 0);
                        leftPath.lineTo(fourLines.lbStartX, fourLines.lbStartY);
                        leftPath.lineTo(fourLines.rbStartX, fourLines.rbStartY);
                        leftPath.lineTo(0, height);
                        leftPath.close();// Closure
                        canvas.drawPath(leftPath, paint);

                        Path rightPath = new Path();
                        rightPath.moveTo(fourLines.ltStartX, fourLines.ltStartY);
                        rightPath.lineTo(width, 0);
                        rightPath.lineTo(width, height);
                        rightPath.lineTo(fourLines.rtStartX, fourLines.rtStartY);
                        rightPath.close();// Closure
                        canvas.drawPath(rightPath, paint);

                        Path bottomPath = new Path();
                        bottomPath.moveTo(fourLines.rbStartX, fourLines.rbStartY);
                        bottomPath.lineTo(fourLines.rtStartX, fourLines.rtStartY);
                        bottomPath.lineTo(width, height);
                        bottomPath.lineTo(0, height);
                        bottomPath.close();// Closure
                        canvas.drawPath(bottomPath, paint);
                    } else if ((fourLines.ltStartX == barWidth
                            && fourLines.ltStartY == barHeight
                            && fourLines.rtStartX == barWidth
                            && fourLines.rtStartY == barHeight
                            && fourLines.lbStartX == barWidth
                            && fourLines.lbStartY == barHeight
                            && fourLines.rbStartX == barWidth && fourLines.rbStartY == barHeight)) {
                        //  a special device lenovo
                        paint.setColor(Color.rgb(0, 255, 0));
                        paint.setStrokeWidth(5);
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        PathEffect effects = new DashPathEffect(new float[]{10, 10, 10, 10}, 1);
                        paint.setPathEffect(effects);
                        Path path1 = new Path();
                        path1.moveTo((int) (width * 0.2), (int) (height - 0.41004673 * width) / 2);
                        path1.lineTo((int) (width * 0.85), (int) (height - 0.41004673 * width) / 2);
                        path1.lineTo((int) (width * 0.85), (int) (height + 0.41004673 * width) / 2);
                        path1.lineTo((int) (width * 0.2), (int) (height + 0.41004673 * width) / 2);
                        path1.close();
                        canvas.drawPath(path1, paint);
                        paint.reset();
                    }
                } else {
                    if ((!(fourLines.ltStartX == barHeight
                            && fourLines.ltStartY == barWidth
                            && fourLines.rtStartX == barHeight
                            && fourLines.rtStartY == barWidth
                            && fourLines.lbStartX == barHeight
                            && fourLines.lbStartY == barWidth
                            && fourLines.rbStartX == barHeight && fourLines.rbStartY == barWidth))) {
                        paint.setStrokeWidth(6);
                        paint.setAntiAlias(true);
                        float length = 3;
                       // WriteUtil.writeLog("绘制线的四个坐标点"+(width-fourLines.ltStartY)+"::::"+fourLines.ltStartX+":::::"+(width-fourLines.lbStartY)+"::::"+fourLines.lbStartX);
                        canvas.drawLine(portraitWidth - fourLines.ltStartY, fourLines.ltStartX,
                                portraitWidth - fourLines.lbStartY, fourLines.lbStartX, paint);// left line
                        canvas.drawLine(portraitWidth - fourLines.ltStartY, fourLines.ltStartX,
                                portraitWidth - fourLines.rtStartY, fourLines.rtStartX, paint);// top line
                        canvas.drawLine(portraitWidth - fourLines.rtStartY, fourLines.rtStartX,
                                portraitWidth - fourLines.rbStartY, fourLines.rbStartX, paint);// right line
                        canvas.drawLine(portraitWidth - fourLines.lbStartY, fourLines.lbStartX,
                                portraitWidth - fourLines.rbStartY, fourLines.rbStartX, paint);// bottom line
                        paint.setColor(Color.argb(80, 255, 255, 255));
                        Path topPath = new Path();
                        topPath.moveTo(portraitWidth - fourLines.lbStartY, fourLines.lbStartX);
                        topPath.lineTo(portraitWidth - fourLines.ltStartY, fourLines.ltStartX);
                        topPath.lineTo(width, 0);
                        topPath.lineTo(0, 0);
                        topPath.close();// Closure
                        canvas.drawPath(topPath, paint);
                        Path leftPath = new Path();
                        leftPath.moveTo(0, 0);
                        leftPath.lineTo(portraitWidth - fourLines.lbStartY, fourLines.lbStartX);
                        leftPath.lineTo(portraitWidth - fourLines.rbStartY, fourLines.rbStartX);
                        leftPath.lineTo(0, height);
                        leftPath.close();// Closure
                        canvas.drawPath(leftPath, paint);

                        Path rightPath = new Path();
                        rightPath.moveTo(portraitWidth - fourLines.ltStartY, fourLines.ltStartX);
                        rightPath.lineTo(width, 0);
                        rightPath.lineTo(width, height);
                        rightPath.lineTo(portraitWidth - fourLines.rtStartY, fourLines.rtStartX);
                        rightPath.close();// Closure
                        canvas.drawPath(rightPath, paint);

                        Path bottomPath = new Path();
                        bottomPath.moveTo(portraitWidth - fourLines.rbStartY, fourLines.rbStartX);
                        bottomPath.lineTo(portraitWidth - fourLines.rtStartY, fourLines.rtStartX);
                        bottomPath.lineTo(width, height);
                        bottomPath.lineTo(0, height);
                        bottomPath.close();// Closure
                        canvas.drawPath(bottomPath, paint);

                    } else if ((fourLines.ltStartX == barHeight
                            && fourLines.ltStartY == barWidth
                            && fourLines.rtStartX == barHeight
                            && fourLines.rtStartY == barWidth
                            && fourLines.lbStartX == barHeight
                            && fourLines.lbStartY == barWidth
                            && fourLines.rbStartX == barHeight && fourLines.rbStartY == barWidth)) {
                        paint.setColor(Color.rgb(0, 255, 0));
                        paint.setStrokeWidth(5);
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        PathEffect effects = new DashPathEffect(new float[]{10, 10, 10, 10}, 1);
                        paint.setPathEffect(effects);
                        Path path1 = new Path();
                        path1.moveTo((int) (width * 0.1), (int) (height - 1.28 * width) / 2);
                        path1.lineTo((int) (width * 0.9), (int) (height - 1.28 * width) / 2);
                        path1.lineTo((int) (width * 0.9), (int) (height + 1.28 * width) / 2);
                        path1.lineTo((int) (width * 0.1), (int) (height + 1.28 * width) / 2);
                        path1.close();// Closure
                        canvas.drawPath(path1, paint);
                        paint.reset();
                    }
                }
            }
            if (nCropType == 0) {
                // If it tests the left part of a document, a reminder line of left will be drawn.
                if (checkLeftFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.left + FRAME_LINE_WIDTH + 2,
                            frame.bottom, paint);// left
                // If it tests the abover part of a document, a reminder line of uppper part will be drawn.
                if (checkTopFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.top + FRAME_LINE_WIDTH, paint);// top
                // If it tests the right part of a document, a reminder line of right will be drawn.
                if (checkRightFrame == 1)
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.bottom, paint);// right
                //  If it tests the bottom part of a document, a reminder line of bottom part will be drawn.
                if (checkRightFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.bottom - FRAME_LINE_WIDTH, frame.right
                                    - FRAME_LINE_WIDTH - 2, frame.bottom, paint); // bottom
            }
        }
    }
}
