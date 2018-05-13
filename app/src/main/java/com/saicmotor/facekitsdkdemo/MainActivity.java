package com.saicmotor.facekitsdkdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.pixtalks.detect.DetectResult;
import com.pixtalks.detect.FaceLandmarkDetector;
import com.pixtalks.facekitsdk.Init;
import com.pixtalks.facekitsdk.PConfig;
import com.pixtalks.facekitsdk.utils.FileUtils;
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
 * 该示例代码提供调用参考, 基于该SDK开发应用时, 请先解决调用摄像头问题
 * 从摄像头视频流里解码得到BitMap, 把BitMap数据传递给检测接口, 检测接口会返回所有检测到的人脸
 * 请根据应用场景选择人脸去抽取特征(比如选取最大面积人脸)
 */

public class MainActivity extends AppCompatActivity {

    public void flushFileDisplay(String path){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(path)));
        this.sendBroadcast(intent);
    }

    private static final String logTag = "FaceKit";
    // FaceLandmarkDetector和Reid 应该都只创建一个实例、初始化一次，然后在程序的整个生命周期里都复用同一个实例。
    // Liveness是单张静默活体的。若启用了动作活体，可以去掉。
    // FaceLandmarkDetector, Reid不能分别多线程并发调用。从测试的结果来看（速度），也不太建议FaceLandmarkDetector、Reid各开一个线程。
    // 可以直接一个线程先调用FaceLandmarkDetector的检测接口、活体接口，然后在通过了人脸检测、活体检测后才调用Reid。当然，UI部分应该另开线程处理。

    // 调用动作活体参考流程：
    // 先通过faceLandmarkDetector.detectFace 检测人脸（因为每次动作活体验证过程中，会保存当前次的全部验证图片，如果第一张不先进行检测，可能会保存大量没必要的非人脸图片）
    // 检测到人脸后才调用动作活体接口（参考说明1）并提示用户眨眼（眨眼过程中头部不要大幅度晃动）
    // 送去faceLandmarkDetector.addImage的图片不需要先调用faceLandmarkDetector.detectFace接口进行人脸检测。faceLandmarkDetector.addImage内部会根据需要自动进行人脸检测。

    // 说明1
    // 眨眼动作活体使用流程（先保证FaceLandmarkDetector初始化成功）：
    // 1 调用faceLandmarkDetector.beginActionDetect（每次开始一次活体动作验证，都需要调用一次）
    // 2 连续调用faceLandmarkDetector.addImage 直至返回 PConfig.okCode 或业务逻辑端控制一次验证传递的最大帧数
    // 3 当faceLandmarkDetector.addImage 返回 PConfig.okCode后，可以调用当faceLandmarkDetector.getDailyImage 获取活体验证他通过的图片


    FaceLandmarkDetector faceLandmarkDetector = null;
    Reid reid = null;
    Liveness liveness = null;

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

    private int loadModel() throws IOException {
        byte[] detectParam, detectBin;
        byte[] landmarkParam, landmarkBin;
        byte[] reidParam, reidBin;
        byte[] livenessParam, livenessBin;

        // Detect
        faceLandmarkDetector = new FaceLandmarkDetector();
        long begin = System.currentTimeMillis();
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getDetectProtoFileName());
            int available = assetsInputStream.available();
            detectParam = new byte[available];
            int byteCode = assetsInputStream.read(detectParam);
            Log.e(PConfig.projectLogTag, "Detect model prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getDetectBinaryFileName());
            int available = assetsInputStream.available();
            detectBin = new byte[available];
            int byteCode = assetsInputStream.read(detectBin);
            Log.e(PConfig.projectLogTag, "Detect model binary file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getLandmarkProtoFileName());
            int available = assetsInputStream.available();
            landmarkParam = new byte[available];
            int byteCode = assetsInputStream.read(landmarkParam);
            Log.e(PConfig.projectLogTag, "Landmark model prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getLandmarkBinaryFileName());
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
            InputStream assetsInputStream = getAssets().open(modelSelector.getProtoFileName());
            int available = assetsInputStream.available();
            reidParam = new byte[available];
            int byteCode = assetsInputStream.read(reidParam);

            //Log.e(PConfig.projectLogTag, str);
            Log.e(PConfig.projectLogTag, "Reid prototxt file size " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getBinaryFileName());
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
            InputStream assetsInputStream = getAssets().open(modelSelector.getLivenessProtoFileName());
            int available = assetsInputStream.available();
            livenessParam = new byte[available];
            int byteCode = assetsInputStream.read(livenessParam);
            Log.e(PConfig.projectLogTag, "liveness prototxt file size  " + byteCode);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open(modelSelector.getLivenessBinaryFileName());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
        int currentapiVersion = Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.M) {
            PixtalksUtils.verifyPermissions(this);
        }
        Init init = new Init();
        init.copyFilesFromRawToSD(this);

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
                PixtalksUtils.getLicenceFromSever(PConfig.getUsername(), PConfig.getAuthCode(), hardwareInfo);
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
                    return;
                }
                Log.e(PConfig.projectLogTag, "Load model use time " + (System.currentTimeMillis() - begin));

                begin = System.currentTimeMillis();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(PConfig.getImg1Path(), options);

                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap2 = BitmapFactory.decodeFile(PConfig.getImg2Path(), options);
                Log.e(PConfig.projectLogTag, "read image use time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                ArrayList<DetectResult> detectResults = faceLandmarkDetector.detectFace(bitmap);

                Log.e(PConfig.projectLogTag, "detect image 1 ues time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                ArrayList<DetectResult> detectResults2 = faceLandmarkDetector.detectFace(bitmap2);
                Log.e(PConfig.projectLogTag, "detect image 2 ues time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                Log.i(PConfig.projectLogTag, "Detect result size " + detectResults.size());
                Log.i(PConfig.projectLogTag, "Detect result 2 size " + detectResults2.size());
                float[] fea = reid.getFeature(bitmap, detectResults.get(0).getLandmark());
                Log.e(PConfig.projectLogTag, "get feature image 1 use time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                float[] fea2 = reid.getFeature(bitmap2, detectResults2.get(0).getLandmark());
                Log.e(PConfig.projectLogTag, "Feature dim " + fea.length + " " + fea2.length);
                Log.e(PConfig.projectLogTag, "get feature image 2 use time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                float score = modelSelector.mapScore(PixtalksUtils.initScore(fea, fea2));
                Log.e(PConfig.projectLogTag, "compare feature use time " + (System.currentTimeMillis() - begin));
                begin = System.currentTimeMillis();
                Log.e("Score ", score + "");
                Log.e(PConfig.projectLogTag, "two image use time " + (System.currentTimeMillis() - begin));

                begin = System.currentTimeMillis();
                boolean isLiveness = liveness.isLiveness(bitmap, detectResults.get(0).getLandmark());
                Log.e(PConfig.projectLogTag, "Image 1 is " + isLiveness + " Use time " + (System.currentTimeMillis() - begin));
                isLiveness = liveness.isLiveness(bitmap2, detectResults2.get(0).getLandmark());
                Log.e(PConfig.projectLogTag, "Image 2 is " + isLiveness + " Use time " + (System.currentTimeMillis() - begin));

                // 每次开始动作活体验证都要调用
                faceLandmarkDetector.beginActionDetect(PConfig.actionType, PConfig.detectInterval, PConfig.t1, PConfig.t2);

                for (int i = 0; i< 30; ++i) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("/storage/emulated/0/pixtalks_facekit_3/frame/ff_" + i + ".jpg");
                    Bitmap frame = BitmapFactory.decodeFile(stringBuffer.toString(), options);
                    ret = faceLandmarkDetector.addImage(frame);
                    if (PConfig.keepAddImage == ret) {
                        continue;
                    }

                    if (ret == PConfig.okCode) {
                        Log.e(PConfig.projectLogTag, "Action pass");
                        byte[] selectImg = faceLandmarkDetector.getDailyImage();
                        FileUtils.saveFile(selectImg, "/storage/emulated/0/pixtalks_facekit_3/select_frame.jpg");
                        flushFileDisplay("/storage/emulated/0/pixtalks_facekit_3/select_frame.jpg");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView tv = (TextView) findViewById(R.id.sample_text);
            tv.setText("See the logcat");

        } else {
            TextView tv = (TextView) findViewById(R.id.sample_text);
            tv.setText("Local licence not exist or invalid");
        }
    }


}
