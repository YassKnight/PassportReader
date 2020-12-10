package com.kernal.passportreader.sdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;

/**
 * Created by huangzhen on 2017/2/6.
 */

public class ActivityRecogUtils {
    /**
     *Invoking Activity recognition with animation effect
     * @param context Context
     * @param selectPath The path to recognize images.
     * @param nMainID Document type to be recognized.
     * @param cutBoolean To see if cropping is needed.
     */
    public static void getRecogResult(Context context, String selectPath, int nMainID, boolean cutBoolean) {
        try {
            RecogService.isRecogByPath = true;
            String logopath = "";
            // String logopath = getSDPath() + "/photo_logo.png";
            Intent intent = new Intent("kernal.idcard");
            Bundle bundle = new Bundle();
            int nSubID[] = null;// {0x0001};
            bundle.putBoolean("isGetRecogFieldPos", false);// To see if we get the location of recognized fields. If the default one is untrue, it means not getting.
            // It must be cropped in the images which have been clipped by core recognition engine.
            bundle.putString("cls", "checkauto.com.IdcardRunner");
            bundle.putInt("nTypeInitIDCard", 0); // To save, press “0”.
            bundle.putString("lpFileName", selectPath);//  Designated image path.
            bundle.putString("cutSavePath", "");// Storage path of cropping images.
            bundle.putInt("nTypeLoadImageToMemory", 0);// ”0” stands for unceratian images, “1” for visual ight, “2” for IR, “4” for UV.
            // if (nMainID == 1000) {
            // nSubID[0] = 3;
            // }
            bundle.putInt("nMainID", nMainID); // Mian type of documents. “6” stands for driving license, “2” for 2nd generation Identity card of P.R. Here one main document type can be uploaded. Each type of documents has its only unique ID No.. The possible value can be seen on the notes of main document type.
            bundle.putIntArray("nSubID", nSubID); // Save the subtype ID of recognized documents. The subtype ones of each document can be shown in “ Subtype document instruction”. IfnSubID[0]=null, it means to set main type douments as “nMainID”.
            // bundle.putBoolean("GetSubID", true);
            // //Get the subtype ID of recognized images.
            // bundle.putString("lpHeadFileName",
            // "/mnt/sdcard/head.jpg");//In saving the path, the suffix can only be jpg, bmp, tif.
            bundle.putBoolean("GetVersionInfo", true); //Get teh version information of development kit.
            //bundle.putBoolean("isSetIDCardRejectType", true);
            bundle.putString("sn", "");
            // bundle.putString("datefile",
            // "assets");//Environment.getExternalStorageDirectory().toString()+"/wtdate.lsc"
            bundle.putString("devcode", Devcode.devcode);
            // bundle.putBoolean("isCheckDevType", true); // To test device type switch in a compulsive way.
            // bundle.putString("versionfile",
            // "assets");//Environment.getExternalStorageDirectory().toString()+"/wtversion.lsc"
            // bundle.putString("sn", "XS4XAYRWEFRY248YY4LHYY178");
            // //Serial No. activation method,  XS4XAYRWEFRY248YY4LHYY178 has been used.
            // bundle.putString("server",
            // "http://192.168.0.36:8080");//http://192.168.0.36:8888
            // bundle.putString("authfile", ""); // File activation method.  //
            // /mnt/sdcard/AndroidWT/357816040594713_zj.txt
            if(nMainID==2) {
                bundle.putBoolean("isAutoClassify", true);
                bundle.putBoolean("isOnlyClassIDCard", true);
            }
            bundle.putString("logo", logopath); // The path of logo. Logo is shown on the top right corner of awaiting pages.
            bundle.putInt("nProcessType", 7);//2- rotation  3- cropping and rotation 4- tilt correction 5- cropping+ tilt correction 6- rotation+tile correct 7- cropping+rotation+tilt correction.
            bundle.putInt("nSetType", 1);// nSetType:“0”-cancelling the operations, 1- setting operations.
            bundle.putBoolean("isCut", cutBoolean); // If not setting, this item will be defualt automatic cropping.
            bundle.putBoolean("isSaveCut", true);// To see if the cropping images should be saved.
            bundle.putString("returntype", "withvalue");// Withvalue is a returned value mode with paramaters( new value passed mode.
           //bundle.putIntegerArrayList("listLoadXMLIdCardType", null);//Whether to add the function of random handoff on the camera interface, when the size is greater than 1, all the document templates in this set are loaded.
            intent.putExtras(bundle);
            ((Activity) context).startActivityForResult(intent, 8);
        } catch (Exception e) {
            Toast.makeText(
                    context,
                    context.getString(context.getResources().getIdentifier("noFoundProgram","string",context.getPackageName()))
                            + "wintone.idcard", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * .Get corresponding recognition results and skip to the result displaying interface.
     * @param context Context
     * @param resultMessage Recognize the returned paramater.
     * @param VehicleLicenseflag The primary interface is the one of driving license or general primary interface.
     */
    public static void goShowResultActivity(Context context, ResultMessage resultMessage, int VehicleLicenseflag, String fullPicturePath, String cutPicturePath) {
        try {
            String recogResultString = "";
            if (resultMessage.ReturnAuthority == 0
                    && resultMessage.ReturnInitIDCard == 0
                    && resultMessage.ReturnLoadImageToMemory == 0
                    && resultMessage.ReturnRecogIDCard > 0) {
                String iDResultString = "";
                String[] GetFieldName = resultMessage.GetFieldName;
                String[] GetRecogResult = resultMessage.GetRecogResult;

                for (int i = 1; i < GetFieldName.length; i++) {
                    if (GetRecogResult[i] != null) {
                        if (!recogResultString.equals(""))
                            recogResultString = recogResultString
                                    + GetFieldName[i] + ":"
                                    + GetRecogResult[i] + ",";
                        else {
                            recogResultString = GetFieldName[i] + ":"
                                    + GetRecogResult[i] + ",";
                        }
                    }
                }
                Intent intent = new Intent("kernal.idcard.ShowResultActivity");
                intent.putExtra("recogResult", recogResultString);
                intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
                intent.putExtra("fullPagePath", fullPicturePath);
                intent.putExtra("cutPagePath", cutPicturePath);
                intent.putExtra("importRecog", true);
                ((Activity)context).finish();
                ((Activity)context).startActivity(intent);
            } else {
                String string = "";
                if (resultMessage.ReturnAuthority == -100000) {
                    string = context.getString(context.getResources().getIdentifier("exception", "string", context.getPackageName()))
                            + resultMessage.ReturnAuthority;
                } else if (resultMessage.ReturnAuthority != 0) {
                    string = context.getString(context.getResources().getIdentifier("exception1", "string", context.getPackageName()))
                            + resultMessage.ReturnAuthority;
                } else if (resultMessage.ReturnInitIDCard != 0) {
                    string = context.getString(context.getResources().getIdentifier("exception2", "string", context.getPackageName()))
                            + resultMessage.ReturnInitIDCard;
                } else if (resultMessage.ReturnLoadImageToMemory != 0) {
                    if (resultMessage.ReturnLoadImageToMemory == 3) {
                        string = context.getString(context.getResources().getIdentifier("exception3", "string", context.getPackageName()))
                                + resultMessage.ReturnLoadImageToMemory;
                    } else if (resultMessage.ReturnLoadImageToMemory == 1) {
                        string = context.getString(context.getResources().getIdentifier("exception4", "string", context.getPackageName()))
                                + resultMessage.ReturnLoadImageToMemory;
                    } else {
                        string = context.getString(context.getResources().getIdentifier("exception5", "string", context.getPackageName()))
                                + resultMessage.ReturnLoadImageToMemory;
                    }
                } else if (resultMessage.ReturnRecogIDCard <= 0) {
                    if (resultMessage.ReturnRecogIDCard == -6) {
                        string = context.getString(context.getResources().getIdentifier("exception9", "string", context.getPackageName()));
                    } else {
                        string = context.getString(context.getResources().getIdentifier("exception6", "string", context.getPackageName()))
                                + resultMessage.ReturnRecogIDCard;
                    }
                }
                Intent intent = new Intent("kernal.idcard.ShowResultActivity");
                intent.putExtra("exception", string);
                intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
                ((Activity)context).finish();
                ((Activity)context).startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,
                    context.getString(context.getResources().getIdentifier("recognized_failed","string",context.getPackageName())),
                    Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * Transform the mirror data into a normal image
     * or Transform a normal image into the mirror data
     * @param src
     * @param w
     * @param h
     * @return
     */
    public static void transformMirrorNV21Data(byte[] src, int w, int h) {
        int i;
        int index;
        byte temp;
        int a, b;
        //mirror y
        for (i = 0; i < h; i++) {
            a = i * w;
            b = (i + 1) * w - 1;
            while (a < b) {
                temp = src[a];
                src[a] = src[b];
                src[b] = temp;
                a++;
                b--;
            }
        }
        // mirror u and v
        index = w * h;
        for (i = 0; i < h / 2; i++) {
            a = i * w;
            b = (i + 1) * w - 2;
            while (a < b) {
                temp = src[a + index];
                src[a + index] = src[b + index];
                src[b + index] = temp;

                temp = src[a + index + 1];
                src[a + index + 1] = src[b + index + 1];
                src[b + index + 1] = temp;
                a += 2;
                b -= 2;
            }
        }
    }

    /**
     * Transform the mirror data into a normal image
     * or Transform a normal image into the mirror data(RGB)
     * @param picPath
     */
   public static int[] transformMirrorRGB(String picPath){
       Bitmap bitmap=BitmapFactory.decodeFile(picPath);
       int width=bitmap.getWidth();
       int height=bitmap.getHeight();
       int[] pixels=new int[width*height];
       int a, b;
       int temp;
       bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
       for (int i = 0; i < height; i++) {
           a = i * width;
           b = (i + 1) * width - 1;
           while (a < b) {
               temp = pixels[a];
               pixels[a] = pixels[b ];
               pixels[b] = temp;
               a += 1;
               b -= 1;
           }
       }
       if(bitmap!=null){
           if(!bitmap.isRecycled()){
               bitmap.recycle();
               bitmap=null;
           }
       }
    return pixels;
   }
    public static void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }
}
