package com.pixtalks.facekitsdk;

//define ANDROID_SDK_OK
//define ANDROID_SDK_INVALID_ARG -4
//ANDROID_SDK_MODEL_NOT_INIT -44
//ANDROID_SDK_INNER_ERROR -444
//ANDROID_SDK_MODEL_NOT_FOUND -404
//ANDROID_SDK_LICENCE_NOT_FOUND -405
//ANDROID_SDK_INVALID_LICENCE -406
//ANDROID_SDK_DECODE_ERROR -1001
//ANDROID_SDK_DETECT_ERROR -1002

import android.os.Environment;
import android.util.Log;

public class PConfig {
    private static final String TAG = "PConfig";

    static {
        try {
            System.loadLibrary("pixtalks_facekit_v1_2");
        } catch (Exception e) {
            Log.e(TAG, "Fail to load pixtalks facekit lib");
        }
    }

    private static String username ="";
    private static String authCode ="";


    public static void setAuth(String mUsername, String mAuthCode){
        username = mUsername;
        authCode= mAuthCode;
    }


    // Or Use half of CPUs to process extract feature
    public static final int reidUseCpuNumber = 2;
    public static final int detectUseCpuNumber = 1;

    //根据设备性能和应用场景来选择, 取值范围 1,2,3. 取值越大, 适用越多场景. 其中
    // １表示用小图输入: 在计算能力教弱的设备上且是近距离的应用场景下建议取１
    // 2 表示用中图输入:
    // 3 表示用大图输入: 中等距离应用场景下用, 只要设备计算能力允许, 都可以用3
    // 不管取何值, 都只需把原始BitMap图片传递给JNI, so内部会根据参数进行图片resize
    public static final int detectInSizeLevel = 1;
    // ok
    public static final int okCode = 0;
    public static final int invalidArgCode = -4;
    public static final int modelNotInitCode = 44;
    public static final int innerErrorCode = -444;
    public static final int modelFileNotFoundCode = -404;
    public static final int licenceNotFoundCode = -405;
    public static final int invalidLicenceCode = -406;
    public static final int invalidLicenceStatusCode = -501;
    public static final int imageDecodeFailCode = -1001;
    public static final int imageDetectFailCode = -1002;

    public static final int keepAddImage = -2;  // 继续传图片取进行活体动作检测
    public static final int actionDetectNotInit = -1402;
    private static final int actionDetectFailed = -1403;
    private static final int headMoveFailed = -1404;
    private static final int tooMuchNotFaceDetect = -1405;

    private static final String licenceServer = "http://lic.pixtalks.top:5809/licence/get_licence";

    // 正常获取Licence
    public static final int gotLicenceCode = 0;
    // 授权信息错误
    public static final int usernameOrAuthCodeErrorCode = -1;
    // 该用户名及授权码所授权的licence已获取完毕，没有剩余的可用
    public static final int noLicenceCode = -2;
    // 服务器端错误
    public static final int serverErrorCode = -3;
    // 请求参数错误，比如请求JSON格式错误，机器码不符合要求
    public static final int requestParamErrorCode = -4;

    public static final int featureMinSize = 10;
    private static final int hardwareMinLength = 10;

    public static final float livenessThreshold = 50;

    public static final int actionType = 1;
    public static final int detectInterval = 10;
    public static final float t1 = 1.8f;
    public static final float t2 = 0.8f;

    public static final String detectLogTag = "DetectLog";
    public static final String reidLogTag = "ReidLog";
    public static final String projectLogTag = "FaceKitLog";

    public static final String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/facekitsdk";

    private static final String pixtalksLicenceName = "android_facekit.licence";

    private static final String dpFileName = "face_detector_v2_2.param.bin";
    private static final String dbFileName = "face_detector_v2_2.bin";
    private static final String lmpFileName = "landmark_2point_v2_1.param.bin";
    private static final String lmBFileName = "landmark_2point_v2_1.bin";

    private static final String mp85FileName = "m85_reid.proto";
    private static final String mb85FileName = "m85_reid.binary";
    private static final String mp127FileName = "reid_v1_4.prototxt";
    private static final String mb127FileName = "reid_v1_4.binary";

    private static final String lp120FileName = "liveness_v1_0.prototxt";
    private static final String lb120FileName = "liveness_v1_0.binary";


    private static String pixtalksLicencePath = String.format("%s/%s", rootDir, pixtalksLicenceName);

    public static void setPixtalksLicencePath(String mPixtalksLicencePath){
        pixtalksLicencePath=mPixtalksLicencePath;
    }

    // demo test only
    private static final String img1Path = String.format("%s/compare_test_1.jpg", rootDir);
//    private static final String img1Path = String.format("%s/abc.jpeg", rootDir);
    private static final String img2Path = String.format("%s/compare_test_2.jpg", rootDir);
    private static final String img3Path = String.format("%s/live_test.jpg", rootDir);

    public static String getLicenceServer() {
        return licenceServer;
    }

    public static String getUsername() {
        return username;
    }

    public static String getAuthCode() {
        return authCode;
    }

    public static int getHardwareMinLength() {
        return hardwareMinLength;
    }


    public static String getPixtalksLicencePath() {
        return pixtalksLicencePath;
    }

    public static String getDpFileName() {
        return dpFileName;
    }

    public static String getDbFileName() {
        return dbFileName;
    }

    public static String getLmpFileName() {
        return lmpFileName;
    }

    public static String getLmBFileName() {
        return lmBFileName;
    }

    public static String getMp85FileName() {
        return mp85FileName;
    }

    public static String getMb85FileName() {
        return mb85FileName;
    }

    public static String getMb127FileName() {
        return mb127FileName;
    }

    public static String getMp127FileName() {
        return mp127FileName;
    }

    public static String getLp120FileName() {
        return lp120FileName;
    }

    public static String getLb120FileName() {
        return lb120FileName;
    }

    public static String getImg1Path() {
        return img1Path;
    }

    public static String getImg2Path() {
        return img2Path;
    }

    public static String getImg3Path() {
        return img3Path;
    }

    public static String getPixtalksLicenceName() {
        return pixtalksLicenceName;
    }


}
