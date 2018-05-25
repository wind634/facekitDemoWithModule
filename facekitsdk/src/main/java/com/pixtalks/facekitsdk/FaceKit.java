package com.pixtalks.facekitsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.pixtalks.detect.DetectResult;
import com.pixtalks.detect.FaceLandmarkDetector;
import com.pixtalks.facekitsdk.utils.CoordUtils;
import com.pixtalks.facekitsdk.utils.ModelSelector;
import com.pixtalks.facekitsdk.utils.PixtalksLicence;
import com.pixtalks.facekitsdk.utils.PixtalksUtils;
import com.pixtalks.reid.Liveness;
import com.pixtalks.reid.Reid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by wangjiang on 2018/2/19.
 */

public class FaceKit {
    private static final String logTag = "FaceKit";

    private Context mContext;

    public FaceKit(Context context){
        this.mContext = context;

        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
        int currentapiVersion = Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.M) {
            PixtalksUtils.verifyPermissions((Activity)mContext);
        }
        Init.copyFilesFromRawToSD(mContext);

    }

    // 刷新文件显示
    public void flushFileDisplay(String path){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(path)));
        mContext.sendBroadcast(intent);
    }


    private FaceLandmarkDetector faceLandmarkDetector = null;
    private Liveness liveness = null;
    private Reid reid = null;

    // TODO 注意!!! 模型类型必须和模型文件匹配, 请不要修改ModelSelector的代码
    // 1 速度比较快，但是不能和PC模型互用
    ModelSelector modelSelector = new ModelSelector(1);

    // Used to load the 'lib' library on application startup.
    static {
        try {
            System.loadLibrary("pixtalks_facekit_v1_2");
        } catch (Exception e) {
            Log.e(logTag, "Fail to load pixtalks facekit lib");
        }
    }

    /**
     * 设置相关验证信息
     */
    public void setAuth(String userName, String authCode){
        PConfig.setAuth(userName, authCode);
    }

    /**
     * 设置证书下载位置
     */
    public void setLicencePath(String licencePath){
        PConfig.setPixtalksLicencePath (String.format("%s/%s", licencePath, PConfig.getPixtalksLicenceName()));

    }

    /**
     * 初始化模型
     * @return
     */
    public int initModel(){
        boolean hasLicence = false;

        // licence 文件不存在或者校验失败都尝试从服务器申请
        // 弹出的申请窗口提供默认的用户名和授权代码，也可以支持修改(提供默认值的文本输入框)
        if (!new File(PConfig.getPixtalksLicencePath()).exists()) {
            Log.e(PConfig.projectLogTag, "Licence file not exist.\n");
        } else if (!PixtalksLicence.isValidLicence(PConfig.getPixtalksLicencePath())) {
            Log.e(PConfig.projectLogTag, "The old licence invalid.\n");
            String hardwareInfo = PixtalksLicence.getHardwareInfo();
            if (hardwareInfo == null || hardwareInfo.length() < PConfig.getHardwareMinLength()) {
                Log.e(PConfig.projectLogTag, "Fail to get hardware info");
            } else {
                // 从服务器上获取，请先修改 pixtalks.facekitsdk.PConfig.java username 和 authCode字段
                PixtalksUtils.getLicenceFromSever(PConfig.getUsername(), PConfig.getAuthCode(), hardwareInfo,null);
                return PConfig.licenceNotFoundCode;
            }
        } else {
            hasLicence = true;
            Log.i(PConfig.projectLogTag, "Have valid licence");
        }

        if (hasLicence) {
            try {
                long begin = System.currentTimeMillis();
                int ret = loadModel();
                if (ret != PConfig.okCode) {
                    Log.e(PConfig.projectLogTag, "Fail to load model with " + ret);
                    return ret;
                }
                Log.e(PConfig.projectLogTag, "Load model use time " + (System.currentTimeMillis() - begin));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return PConfig.okCode;
        } else {
            return PConfig.invalidLicenceStatusCode;
        }

    }


    /**
     * @return 应用端最好应该区分load detect model 和 load reid model的错误信息
     * @throws IOException
     */
    private int loadModel() throws IOException {
        byte[] detectParam, detectBin;
        byte[] landmarkParam, landmarkBin;
        byte[] reidParam, reidBin;
        byte[] livenessParam, livenessBin;

        // Detect
        faceLandmarkDetector = new FaceLandmarkDetector();
        long begin = System.currentTimeMillis();
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getDetectProtoFileName());
            int available = assetsInputStream.available();
            detectParam = new byte[available];
            int byteCode = assetsInputStream.read(detectParam);
            Log.e(PConfig.projectLogTag, "Detect model prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getDetectBinaryFileName());
            int available = assetsInputStream.available();
            detectBin = new byte[available];
            int byteCode = assetsInputStream.read(detectBin);
            Log.e(PConfig.projectLogTag, "Detect model binary file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getLandmarkProtoFileName());
            int available = assetsInputStream.available();
            landmarkParam = new byte[available];
            int byteCode = assetsInputStream.read(landmarkParam);
            Log.e(PConfig.projectLogTag, "Landmark model prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getLandmarkBinaryFileName());
            int available = assetsInputStream.available();
            landmarkBin = new byte[available];
            int byteCode = assetsInputStream.read(landmarkBin);
            Log.e(PConfig.projectLogTag, "Landmark model binary file size " + byteCode);
            assetsInputStream.close();
        }
        Log.e(PConfig.projectLogTag, "Read detect and landmark model use time " + (System.currentTimeMillis() - begin) + " ms");
        begin = System.currentTimeMillis();
        int ret = faceLandmarkDetector.loadModel(PConfig.getPixtalksLicencePath(),
                detectParam, detectBin, landmarkParam, landmarkBin, PConfig.detectInSizeLevel, PConfig.detectUseCpuNumber);
        if (ret != PConfig.okCode) {
            Log.e(PConfig.projectLogTag, "Init detector fail with " + ret);
            return ret;
        }
        Log.e(PConfig.projectLogTag, "Init detector use time " + (System.currentTimeMillis() - begin) + "ms");

        // Feature
        reid = new Reid(modelSelector.getModelId());
        begin = System.currentTimeMillis();
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getProtoFileName());
            int available = assetsInputStream.available();
            reidParam = new byte[available];
            int byteCode = assetsInputStream.read(reidParam);

            //Log.e(PConfig.projectLogTag, str);
            Log.e(PConfig.projectLogTag, "Reid prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getBinaryFileName());
            int available = assetsInputStream.available();
            reidBin = new byte[available];
            int byteCode = assetsInputStream.read(reidBin);
            Log.e(PConfig.projectLogTag, "Reid binary file size " + byteCode);
            assetsInputStream.close();
        }
        Log.e(PConfig.projectLogTag, "Read reid model time " + (System.currentTimeMillis() - begin));
        begin = System.currentTimeMillis();

        ret = reid.loadModel(PConfig.getPixtalksLicencePath(), reidParam, reidBin);
        if (ret != PConfig.okCode) {
            Log.e(PConfig.projectLogTag, "Init reid model fail with " + ret);
            return ret;
        }
        Log.e(PConfig.projectLogTag, "Init reid model use time " + (System.currentTimeMillis() - begin));

        begin = System.currentTimeMillis();
        // Liveness
        liveness = new Liveness();
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getLivenessProtoFileName());
            int available = assetsInputStream.available();
            livenessParam = new byte[available];
            int byteCode = assetsInputStream.read(livenessParam);
            Log.e(PConfig.projectLogTag, "liveness prototxt file size  " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = mContext.getAssets().open(modelSelector.getLivenessBinaryFileName());
            int available = assetsInputStream.available();
            livenessBin = new byte[available];
            int byteCode = assetsInputStream.read(livenessBin);
            Log.e(PConfig.projectLogTag, "liveness binary file size  " + byteCode);
            assetsInputStream.close();
        }
        Log.e(PConfig.projectLogTag, "Read liveness model use time " + (System.currentTimeMillis() - begin));

        begin = System.currentTimeMillis();
        ret = liveness.loadModel(PConfig.getPixtalksLicencePath(), livenessParam, livenessBin);

        if (ret != PConfig.okCode) {
                Log.e(PConfig.projectLogTag, "Init liveness model fail with " + ret);
            return ret;
        }
        Log.w(PConfig.projectLogTag, "Init liveness model use time " + (System.currentTimeMillis() - begin));

        return 0;
    }


    /**
     * 判断是否有人脸
     */
    public ArrayList<DetectResult>  detectFace(Bitmap bitmap){
        return faceLandmarkDetector.detectFace(bitmap);
    }

    /**
     * 获取特征值
     */
    public float[]  getFeatureByDetectResult(Bitmap bitmap, DetectResult detectResult){
        return reid.getFeature(bitmap, detectResult.getLandmark());
    }


    /**
     * 获取特征值(一张图片可能有多个人脸)
     * isOnlyFirst 是否只提取识别度最高的人脸 默认为true
     */
    public ArrayList<float[]> getFeature(Bitmap bitmap, boolean isOnlyFirst){
        ArrayList<DetectResult> detectResults = faceLandmarkDetector.detectFace(bitmap);
        if(detectResults!=null){
            ArrayList<float[]> featureList = new ArrayList<float[]>();
            if(!isOnlyFirst) {
                for (int i = 0; i < detectResults.size(); i++) {
                    float[] feature = reid.getFeature(bitmap, detectResults.get(i).getLandmark());
                    featureList.add(feature);
                }
            }else {
                float[] feature = reid.getFeature(bitmap, detectResults.get(0).getLandmark());
                featureList.add(feature);
            }
            return featureList;
        }else {
            return null;
        }
    }


    /**
     * 两个bitmap进行比对的接口
     *
     */

    public float compareScore(Bitmap bitmap1, Bitmap bitmap2){
        ArrayList<DetectResult> detectResults1 = faceLandmarkDetector.detectFace(bitmap1);
        if(detectResults1 == null){
            return 0.0f;
        }
        float[] feature1 = reid.getFeature(bitmap1, detectResults1.get(0).getLandmark());

        ArrayList<DetectResult> detectResults2 = faceLandmarkDetector.detectFace(bitmap2);
        if(detectResults2 == null){
            return 0.0f;
        }
        float[] feature2 = reid.getFeature(bitmap2, detectResults2.get(0).getLandmark());

        return modelSelector.mapScore(PixtalksUtils.initScore(feature1, feature2));
    }

    /**
     * 两个特征值进行比对的接口
     */

    public float compareScore(float[] fea1, float[] fea2){
        return modelSelector.mapScore(PixtalksUtils.initScore(fea1, fea2));
    }



    /**
     * 一比n
     * 每一个bitmap都要重新提取特征值, bitmap很多的话会比较慢, 不建议使用
     * 返回比对的索引
     */
    public CompareResult compareMultiScore(Bitmap bitmap, ArrayList<Bitmap> bitmapList){
        ArrayList<float[]> featureList = getFeature(bitmap,true);
        if(featureList == null){
            return null;
        }

        float[] feature = featureList.get(0);

        float maxScore = 0;
        int index = 0;

        for(int i=0; i<bitmapList.size(); i++){
            Bitmap b = bitmapList.get(i);
            ArrayList<float[]> __featureList = getFeature(b,true);
            if(__featureList != null) {
                float[] fea = __featureList.get(0);
                float score = modelSelector.mapScore(PixtalksUtils.initScore(feature, fea));
                if (score > maxScore) {
                    maxScore = score;
                    index = i;
                }
            }
        }

        return new CompareResult(index, maxScore);
    }


    /**
     * 一比n
     */
    public CompareResult compareMultiScore(float[] feature, ArrayList<float[]> featureList){

        float maxScore = 0;
        int index = 0;

        for(int i=0; i<featureList.size(); i++){
            float[] fea = featureList.get(i);
            float score = modelSelector.mapScore(PixtalksUtils.initScore(feature, fea));
            if (score > maxScore) {
                maxScore = score;
                index = i;
            }
        }

        return new CompareResult(index, maxScore);
    }

    /**
     * 判断是否是活体
     */
    public boolean isLive(Bitmap bitmap, DetectResult detectResult){
        return liveness.isLiveness(bitmap, detectResult.getLandmark());
    }

    /**
     * 设置人脸框大小
     */
    public void setFaceArgs(float width_extend_factor, float height_extend_factor){
        CoordUtils.width_extend_factor = width_extend_factor;
        CoordUtils.height_extend_factor = height_extend_factor;
    }


    /** 开始进行眨眼活体验证
     *
     */
    // 每次开始动作活体验证都要调用
     public  void beginActionDetect() {
         faceLandmarkDetector.beginActionDetect(PConfig.actionType, PConfig.detectInterval, PConfig.t1, PConfig.t2);
     }
    /**
     * 添加图片
     */
    public  int addImage(Bitmap bitmap) {
        return faceLandmarkDetector.addImage(bitmap);
    }

    /**
     * 添加图片
     */
    public  byte[] getDailyImage() {
        return faceLandmarkDetector.getDailyImage();
    }

}
