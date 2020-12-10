package com.kernal.passportreader.sdk.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kernal.imageprocessor.NetworkProber;
import com.kernal.lisence.ProcedureAuthOperate;
import com.kernal.passportreader.sdk.utils.CardScreenUtil;
import com.kernal.passportreader.sdk.utils.ImportRecog;

import kernal.idcard.camera.SharedPreferencesHelper;
import wintone.viewdemo.R;

/**
 * Time:2018/12/20
 * Author:A@H
 * Description: 证件识别的主界面的基类
 */
public abstract class OcrBaseActivity extends AppCompatActivity implements View.OnClickListener {

    private long mPressedTime = 0;
    private int srcWidth, srcHeight;
    private Button btn_takePicture, btn_exit,
            btn_importRecog, btn_ActivationProgram, btn_Intelligent_detecting_edges, btn_cancel_imei;
    public Button btn_chooserIdCardType;
    EditText editText;
    private String[] idcardType;
    private String[][] idcardType2;
    private int nMainID = 2, nSubID = 0;
    public AlertDialog importDialog;
    private static final int PERMISSION_REQUESTCODE = 1;
    public ImportRecog importRecog;
    private boolean isOnKeyHome = false;
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(wintone.viewdemo.R.layout.activity_idcard);
        srcWidth = CardScreenUtil.getScreenResolution(this).x;
        srcHeight = CardScreenUtil.getScreenResolution(this).y;
        findView();
        setCardId(getCardId());
        idcardType2 = intiList(getCardId());
        idcardType = new String[idcardType2.length];
        for (int i = 0; i < idcardType2.length; i++) {
            idcardType[i] = idcardType2[i][0];
        }
        permission();
    }

    private void setCardId(int id) {
        if (id ==2)btn_chooserIdCardType.setVisibility(View.GONE);
        if (id != 0) {
            SharedPreferencesHelper.putInt(getApplicationContext(), "nMainId", id);
            SharedPreferencesHelper.putInt(getApplicationContext(), "nSubID", 0);
        }
    }

    private void findView() {
        // TODO Auto-generated method stub

        btn_chooserIdCardType = (Button) findViewById(getResources().getIdentifier("btn_chooserIdCardType", "id",
                getPackageName()));
        btn_takePicture = (Button) findViewById(getResources().getIdentifier("btn_takePicture", "id",
                getPackageName()));
        btn_cancel_imei = (Button) findViewById(getResources().getIdentifier("btn_cancel_imei", "id",
                getPackageName()));
        btn_exit = (Button) findViewById(getResources().getIdentifier("btn_exit", "id",
                getPackageName()));
        btn_importRecog = (Button) findViewById(getResources().getIdentifier("btn_importRecog", "id",
                getPackageName()));
        btn_ActivationProgram = (Button) findViewById(getResources().getIdentifier("btn_ActivationProgram", "id",
                getPackageName()));
        btn_Intelligent_detecting_edges = (Button) findViewById(getResources().getIdentifier("btn_Intelligent_detecting_edges", "id",
                getPackageName()));
        btn_ActivationProgram.setOnClickListener(this);
        btn_chooserIdCardType.setOnClickListener(this);
        btn_takePicture.setOnClickListener(this);
        btn_Intelligent_detecting_edges.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
        btn_cancel_imei.setOnClickListener(this);
        btn_importRecog.setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                srcWidth / 2, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (srcHeight * 0.25);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_ActivationProgram.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_ActivationProgram);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_chooserIdCardType.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_chooserIdCardType);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_takePicture.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_takePicture);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_Intelligent_detecting_edges.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_Intelligent_detecting_edges);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_importRecog.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_importRecog);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_cancel_imei.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_cancel_imei);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_exit.setLayoutParams(params);
    }

    @Override
    public void onClick(View v) {

        if (getResources().getIdentifier("btn_ActivationProgram", "id",
                this.getPackageName()) == v.getId()) {
            activationProgramOpera();
        } else if (getResources()
                .getIdentifier("btn_chooserIdCardType", "id", this.getPackageName()) == v.getId()) {
            createDialog();
        } else if (getResources().getIdentifier("btn_takePicture", "id",
                this.getPackageName()) == v.getId()) {
            /**
             * It will wast a lot of time if such resources like camera is released in the camera interface.
             * To optimize user experience, it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
             * is stored in oncreate() method in the camera interface.
             * If the displaying interface is the one to invoke and recognize,
             * this interface can only be invoked once.
             * If there are two displaying interfaces,
             * it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
             * is stored in oncreate() method in the displaying interface.
             * Otherwise, it will lead to the overflow of internal memeory.
             */
            jumpOriginalScanActivity();
        } else if (getResources().getIdentifier("btn_Intelligent_detecting_edges", "id",
                this.getPackageName()) == v.getId()) {
            /**
             * It will wast a lot of time if such resources like camera is released in the camera interface.
             * To optimize user experience, it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
             * is stored in oncreate() method in the camera interface.
             * If the displaying interface is the one to invoke and recognize,
             * this interface can only be invoked once.
             * If there are two displaying interfaces,
             * it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
             * is stored in oncreate() method in the displaying interface.
             * Otherwise, it will lead to the overflow of internal memeory.
             *
             *
             */
            jumpIntelligenceScanActivity();
        } else if (getResources().getIdentifier("btn_exit", "id",
                this.getPackageName()) == v.getId()) {
            this.finish();
        } else if (getResources().getIdentifier("btn_importRecog", "id",
                this.getPackageName()) == v.getId()) {

            jumpSystemSelectPic();
        } else if (getResources().getIdentifier("btn_cancel_imei", "id", this.getPackageName()) == v.getId()) {
            deleteAuthInfosOpera();
        }

    }

    public abstract void serialNumberAuth(String authNumber);

    public abstract void jumpIntelligenceScanActivity();

    public abstract void jumpOriginalScanActivity();

    public abstract void jumpSystemSelectPic();

    public abstract void importPicToRecog(String picPath);

    public abstract void importPicToRecog(Uri uri);

    public abstract int getCardId();

    /**
     * @return void 返回类型
     * @throws
     * @Title: activationProgramOpera
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @param: 设定文件
     */
    private void activationProgramOpera() {
        // TODO Auto-generated method stub
        editText = new EditText(OcrBaseActivity.this);
        AlertDialog alertDialog = new AlertDialog.Builder(OcrBaseActivity.this)
                .setTitle(getString(R.string.dialog_title))
                .setView(editText)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton(getString(R.string.online_activation), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editsString = editText.getText()
                                .toString().toUpperCase();

                        if (NetworkProber.isNetworkAvailable(OcrBaseActivity.this)) {
                            if (NetworkProber.isWifi(OcrBaseActivity.this)
                                    || NetworkProber.is3G(OcrBaseActivity.this)) {
                                if (editsString != null) {
                                    serialNumberAuth(editsString);
                                }
                            } else if (!NetworkProber.isWifi(OcrBaseActivity.this)
                                    && !NetworkProber.is3G(OcrBaseActivity.this)) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.network_unused),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getString(R.string.please_connect_network),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).create();
        alertDialog.show();
    }

    /**
     * @Title: deleteAuthInfosOpera @Description: 清除序列号绑定的IMEI信息
     * 设定文件 @return void 返回类型 @throws
     */
    private void deleteAuthInfosOpera() {
        editText = new EditText(OcrBaseActivity.this);
        AlertDialog alertDialog = new AlertDialog.Builder(OcrBaseActivity.this)
                .setTitle(getString(R.string.cancel_title))
                .setView(editText)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                }).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editsString = editText.getText()
                                .toString().toUpperCase();

                        if (NetworkProber.isNetworkAvailable(OcrBaseActivity.this)) {
                            if (NetworkProber.isWifi(OcrBaseActivity.this)
                                    || NetworkProber.is3G(OcrBaseActivity.this)) {
                                try {

                                    ProcedureAuthOperate pao = new ProcedureAuthOperate(OcrBaseActivity.this);
                                    pao.DeleteAuthInfosTask.execute(editsString, "", "11");
                                    int isSuccessDeleteAuthInfos = (Integer) pao.DeleteAuthInfosTask.get();
                                    if (isSuccessDeleteAuthInfos == 0) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                getString(R.string.cancel_success),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                getString(R.string.exception10) + ":" + isSuccessDeleteAuthInfos,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (!NetworkProber.isWifi(OcrBaseActivity.this)
                                    && !NetworkProber.is3G(OcrBaseActivity.this)) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.network_unused),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getString(R.string.please_connect_network),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 选择证件类型
     */
    private void createDialog() {
        int checkedItem = -1;

        for (int i = 0; i < idcardType2.length; i++) {

            if (Integer.valueOf(idcardType2[i][1]) == SharedPreferencesHelper.getInt(
                    getApplicationContext(), "nMainId", 2)) {
                if (idcardType2[i].length > 2) {
                    if (Integer.valueOf(idcardType2[i][2]) == SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nSubID", 0)) {
                        checkedItem = i;
                        break;
                    }
                } else {
                    checkedItem = i;
                    break;
                }
            }
        }

        AlertDialog alertDialog = new AlertDialog.Builder(OcrBaseActivity.this)
                .setTitle(getString(wintone.viewdemo.R.string.chooserIdCardType))
                .setSingleChoiceItems(idcardType, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        nMainID = Integer.valueOf(idcardType2[i][1]);
                        if (idcardType2[i].length > 2) {
                            nSubID = Integer.valueOf(idcardType2[i][2]);
                        }
                    }
                }).setNegativeButton(getString(wintone.viewdemo.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton(getString(wintone.viewdemo.R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (nMainID == 0) {
                            if (SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2) != 2) {
                                nMainID = SharedPreferencesHelper.getInt(
                                        getApplicationContext(), "nMainId", 2);
                            } else {
                                nMainID = 2;
                            }
                        }

                        SharedPreferencesHelper.putInt(getApplicationContext(),
                                "nMainId", nMainID);
                        SharedPreferencesHelper.putInt(getApplicationContext(),
                                "nSubID", nSubID);
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 动态授权
     */
    private void permission() {
        boolean isPermission = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                isPermission = false;
            }
        }
        if (!isPermission) {
            //没有授权
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUESTCODE);
        } else {
            //已经授权

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE:
                boolean isPermission = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isPermission = false;
                    }
                }
                if (!isPermission) {
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();
        if ((mNowTime - mPressedTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else {
            this.finish();
            System.exit(0);
        }
    }

    /**
     * TD-1型机读旅行证件 不支持扫描识别
     *
     * @return
     */
    private String[][] intiList(int nMainID) {
        String[][] list = null;
        if (getResources().getConfiguration().locale.getLanguage().equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("CN")) {
            String[][] temp = {{"机读码", "3000"},
                    {"TD-1型机读旅行证件", "2009"},
                    {"TD-2型机读旅行证件", "2006"},
                    {"护照", "13"},
                    {"居民身份证", "2"},
                    {"临时身份证", "4"},
                    {"往来港澳通行证2005版", "9"},
                    {"港澳台居民居住证-照片页", "31"},
                    {"港澳台居民居住证-签发机关页", "32"},
                    {"外国人永久居留身份证", "33"},
                    {"大陆居民往来台湾通行证1992版", "11"},
                    {"往来台湾通行证2017版-照片页", "29"},
                    {"签证", "12"},
                    {"往来港澳通行证2014版", "22"},
                    {"机动车驾驶证（中国）", "5"},
                    {"机动车驾驶证副页（中国）", "28"},
                    {"机动车行驶证（中国）", "6"},
                    {"机动车行驶证副页（中国）", "30"},
                    {"厦门社会保障卡", "1039"},
                    {"福建社会保障卡", "1041"},
                    {"香港居民身份证", "1001", "0"},//香港身份证自动分类时 subId设为0，旧版设为1，新版设为2
                    {"香港入境小票", "1015", "0"},
                    {"户口本", "16"},
                    {"港澳居民来往内地通行证(照片页)", "14"},
                    {"港澳居民来往内地通行证(机读码页)", "15"},
                    {"澳门居民身份证2002版（照片页）", "1005", "1"},
                    {"澳门居民身份证2013版（照片页）", "1005", "2"},
                    {"澳门蓝卡", "1014", "0"},

                    {"深圳居住证", "1013"},
                    {"台湾居民往来大陆通行证1992版（照片页）", "10"},
                    {"台湾居民往来大陆通行证2015版(照片页)", "25"},
                    {"台湾居民往来大陆通行证2015版(机读码页)", "26"},
                    {"台湾国民身份证(照片页)", "1031"},
                    {"台湾国民身份证(条码页)", "1032"},
                    {"中国军官证1998版", "7"},
                    {"台湾全民健康保险卡", "1030"},
                    {"马来西亚身份证（照片页）", "2001"},
                    {"新加坡身份证", "2004"},
                    {"新西兰驾驶证", "2003"},
                    {"加利福尼亚驾驶证", "2002"},
                    {"印度尼西亚居民身份证-KEPT", "2010", "1"},
                    {"印度尼西亚居民身份证-KPT", "2010", "2"},
                    {"泰国国民身份证(正面)", "2011"},
                    {"泰国国民身份证(反面)", "2016"},
                    {"北京社保卡", "1021"},
                    {"泰国驾驶证", "2012", "1"},
                    {"泰国驾驶证-私家车", "2012", "2"},
                    {"墨西哥选民证AB", "2013", "1"},
                    {"墨西哥选民证C", "2013", "2"},
                    {"墨西哥选民证DE", "2013", "3"},
                    {"墨西哥选民证背面ABC", "2014"},
                    {"马来西亚驾照", "2021"},
                    {"新加坡驾驶证", "2031"},
                    {"印度尼西亚驾驶证", "2041"},
                    {"日本驾照", "2051"},
                    {"菲律宾身份证", "2022"},
                    {"律师证照片页", "1008"},
                    {"律师证签发机关页", "1007"}
            };
            if (nMainID == 6) {
                temp= new String[][]{{"机动车行驶证（中国）", "6"},
                        {"机动车行驶证副页（中国）", "30"},};
            }
            list = temp;
        } else if (getResources().getConfiguration().locale.getLanguage()
                .equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("TW")) {
            String[][] temp = {{"機讀碼", "3000"},
                    {"TD-1型机读旅行证件", "2009"},
                    {"TD-2型机读旅行证件", "2006"},
                    {"護照", "13"},
                    {"居民身份證", "2"},
                    {"臨時身份證", "4"},
                    {"往来港澳通行證2005版", "9"},
                    {"港澳台居民居住證-照片頁", "31"},
                    {"港澳台居民居住證-簽發機關頁", "32"},
                    {"外國人永久居留身份證", "33"},
                    {"大陸居民往來台灣通行證1992版", "11"},
                    {"往來台灣通行證2017版-照片頁", "29"},
                    {"簽證", "12"},
                    {"往來港澳通行證2014版", "22"},
                    {"機動車駕駛證（中國）", "5"},
                    {"機動車駕駛證副頁（中國）", "28"},
                    {"機動車行駛證（中國）", "6"},
                    {"機動車行駛證副頁（中國）", "30"},
                    {"廈門社會保障卡", "1039"},
                    {"福建社會保障卡", "1041"},
                    {"香港居民身份證（照片頁）", "1001", "0"},
                    {"香港入境小票", "1015", "0"},
                    {"戶口本", "16"},
                    {"港澳居民來往內地通行證(照片頁)", "14"},
                    {"港澳居民來往內地通行證(機讀碼頁)", "15"},
                    {"澳門居民身份證2002版（照片頁）", "1005", "1"},
                    {"澳門居民身份證2013版（照片頁）", "1005", "2"},
                    {"澳门蓝卡", "1014", "0"},
                    {"深圳居住證", "1013"},
                    {"台灣居民往來大陸通行證1992版（照片頁）", "10"},
                    {"台灣國民身份證(照片頁)", "1031"},
                    {"台灣國民身份證(條碼頁)", "1032"},
                    {"台灣居民往來大陸通行證1992版(照片頁)）", "10"},
                    {"台灣居民往來大陸通行證2015版(照片頁)", "25"},
                    {"台灣居民往來大陸通行證2015版(機讀碼頁)", "26"},
                    {"中國軍官証1998版", "7"},
                    {"台灣全民健康保險卡", "1030"},
                    {"馬來西亞身份證（照片頁）", "2001"},
                    {"新加坡身份證", "2004"},
                    {"新西蘭駕照", "2003"},
                    {"加利福尼亞駕照", "2002"}, {"印度尼西亞身份證-KEPT", "2010", "1"},
                    {"印度尼西亞身份證-KPT", "2010", "2"},
                    {"泰國國民身份證（正面）", "2011"},
                    {"泰國國民身份證(反面)", "2016"},
                    {"北京社保卡", "1021"}
                    , {"泰國駕駛證", "2012", "1"},
                    {"泰國駕駛證-私家車", "2012", "2"},
                    {"墨西哥選民證AB", "2013", "1"},
                    {"墨西哥選民證C", "2013", "2"},
                    {"墨西哥選民證DE", "2013", "3"},
                    {"墨西哥選民證背面ABC", "2014"},
                    {"馬來西亞駕照", "2021"},
                    {"新加坡駕駛證", "2031"},
                    {"印度尼西亞駕駛證", "2041"},
                    {"日本駕照", "2051"},
                    {"菲律賓身份證", "2022"},
                    {"律师证照片页", "1008"},
                    {"律师证签发机关页", "1007"}
            };
            if (nMainID == 6) {
                temp= new String[][]{{"机动车行驶证（中国）", "6"},
                        {"机动车行驶证副页（中国）", "30"},};
            }
            list = temp;
        } else {
            String[][] temp = {{"Machine readable zone", "3000"},
                    {"TD-1 Machine Readable Travel Documents", "2009"},
                    {"TD-2 Machine Readable Travel Documents", "2006"},
                    {"Passport", "13"}, {"Chinese ID card", "2"},
                    {"Interim ID card", "4"},
                    {"Exit-Entry Permit to HK/Macau", "9"},
                    {"Residence permit for Hong Kong, Macao and Taiwan residents Portrait Page", "31"},
                    {"Residence permit for Hong Kong, Macao and Taiwan residents Authority Page", "32"},
                    {"Permanent Residence Permit for Aliens Portrait Page", "33"},
                    {"Taiwan pass", "11"}, {"Taiwan pass(2017 Obverse)", "29"}, {"Visa", "12"},
                    {"e-EEP to HK/Macau", "22"},
                    {"Chinese Driving license", "5"},
                    {"Chinese Driving license(second)", "28"},
                    {"Chinese vehicle license", "6"},
                    {"China Driving License Vice Page", "30"},
                    {"Xiamen Social Security Card", "1039"}, {"Fujian Social Security Card", "1041"}, {"HK ID card", "1001", "0"}, {"HK entry ticket", "1015", "0"}, {"Household Register", "16"},
                    {"Home return permit (Obverse)", "14"},
                    {"Home return permit (Reverse)", "15"},
                    {"Macau ID card 2002", "1005", "1"},
                    {"Macau ID card 2013", "1005", "2"},
                    {"Macao blue card", "1014", "0"},
                    {"Shenzhen Resident Permit", "1013"},
                    {"To the Mainland Travel Permit", "10"},
                    {"Taiwan ID card (Obverse)", "1031"},
                    {"Taiwan ID card (Reverse)", "1032"},
                    {"To the New Mainland Travel Permit(Obverse)", "25"},
                    {"To the New Mainland Travel Permit(Reverse)", "26"}, {"Chinese certificate of officers", "7"},
                    {"National health care card", "1030"},
                    {"MyKad", "2001"}, {"Singapore ID card", "2004"},
                    {"New Zealand Driving license", "2003"},
                    {"California driving license", "2002"}, {"Indonesia Resident Identity Card (EKPT)", "2010", "1"}, {"Indonesia Resident Identity Card (KPT)", "2010", "2"},
                    {"Thailand's Identity Card(Obverse)", "2011"},
                    {"Thailand's Identity Card(Reverse)", "2016"},
                    {"Beijing social security card", "1021"}
                    , {"Thailand Driving License", "2012", "1"},
                    {"Thailand Private Car Driving License", "2012", "2"},
                    {"Mexico INE&IFE ID Card Type AB", "2013", "1"},
                    {"Mexico INE&IFE ID Card Type C", "2013", "2"},
                    {"Mexico INE&IFE ID Card Type DE", "2013", "3"},
                    {"Mexico INE&IFE ID Card Reverse Page", "2014"},
                    {"Malaysia Driving license", "2021"}, {"Singapore Driving License", "2031"},
                    {"Lawyer's license pic", "1008"},
                    {"Lawyer's license date", "1007"},
                    {"Indonesia Driving License", "2041"}, {"Japan Driving License", "2051"}, {"Philippine ID card ", "2022"}
            };
            if (nMainID == 6) {
                temp= new String[][]{{"机动车行驶证（中国）", "6"},
                        {"机动车行驶证副页（中国）", "30"},};
            }
            list = temp;
        }
        return list;
    }
}
