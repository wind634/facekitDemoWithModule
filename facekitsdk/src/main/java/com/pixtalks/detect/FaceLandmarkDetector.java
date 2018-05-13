package com.pixtalks.detect;

import android.graphics.Bitmap;

import com.pixtalks.facekitsdk.PConfig;
import com.pixtalks.facekitsdk.utils.CoordUtils;

import java.io.File;
import java.util.ArrayList;

public class FaceLandmarkDetector {

    private int lastErrorCode;

    private boolean inited = false;

    private boolean actionDetectInited = false;

    private boolean actionPass = false;

    public FaceLandmarkDetector() {
        this.lastErrorCode = 0;
    }

    public int loadModel(String licencePath,
                         byte[] detectParam,
                         byte[] detectBin,
                         byte[] landmarkParam,
                         byte[] landmarkBin,
                         int inputSizeLevel,
                         int cpuNumber) {
        if (!new File(licencePath).isFile()) {
            return PConfig.licenceNotFoundCode;
        }
        int ret = loadDetectModelJNI(licencePath, detectParam, detectBin, landmarkParam, landmarkBin, 0, 93, 0, 73, inputSizeLevel, cpuNumber);
        if (ret == PConfig.okCode) {
            inited = true;
        }
        return ret;
    }

    public ArrayList<DetectResult> detectFace(Bitmap image) {
        if (!inited) {
            System.out.print("Face detector model not inited, do it first\n");
            return null;
        }
        if (image == null) {
            return null;
        }
        ArrayList<int[]> landmarks = detectFaceJNI(image);
        if (landmarks == null || landmarks.size() < 1) {
            this.lastErrorCode = getLastErrorCodeJNI();
            return null;
        }
        ArrayList<DetectResult> results = new ArrayList<>();
        for (int i = 0; i < landmarks.size(); i++) {
            int[] landmark = landmarks.get(i);
            if (landmark.length != 10) {
                this.lastErrorCode = PConfig.innerErrorCode;
                return null;
            }
            int[] box = CoordUtils.landmarkToBox(image, landmark);
            DetectResult detectResult = new DetectResult(box, landmark);
            results.add(detectResult);
        }
        return results;
    }

    public int beginActionDetect(int actionType, int detectInterval, float t1, float t2) {
        if (!inited) {
            System.out.print("Face detector model not inited, do it first\n");
            return PConfig.modelNotInitCode;
        }
        actionPass = false;
        //1 eye blink
        if (actionType != 1) {
            return PConfig.invalidArgCode;
        }
        int ret = beginActionDetectJNI(actionType, detectInterval, t1, t2);
        if (PConfig.okCode == ret) {
            actionDetectInited = true;
        }
        return ret;
    }
    public int addImage(Bitmap bitmap) {
        if (!inited) {
            System.out.print("Face detector model not inited, do it first\n");
            return PConfig.modelNotInitCode;
        }
        if(bitmap == null){
            return PConfig.imageDecodeFailCode;
        }
        if (!actionDetectInited) {
            System.out.print("Action detect not init, please call beginActionDetect first\n");
            return PConfig.modelNotInitCode;
        }

        int ret = addImageJNI(bitmap);
        if (PConfig.okCode == ret) {
            actionPass = true;
        }

        return ret;
    }

    public byte[] getDailyImage() {
        if (actionPass) {
            byte[] img = getDailyImageJNI();
            actionPass = false;
            return img;
        }
        return null;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }

    /**
     * 一旦调用了该方法释放资源，再次使用detectFace之前必须调用loadModel，因此该方法几乎用不到
     */
    public static void releaseJNIRes(){
        releaseJNIResJNI();
    }

    private native synchronized int loadDetectModelJNI(String licencePath,
                                                       byte[] detectParam,
                                                       byte[] detectBin,
                                                       byte[] landmarkParam,
                                                       byte[] landmarkBin,
                                                       int detectInIndex,
                                                       int detectOutIndex,
                                                       int landmarkInIndex,
                                                       int landmarkOutIndex,
                                                       int inputSizeLevel,
                                                       int cpuNumber);

    /**
     * @param image the target image
     * @return 5 point landmark, landmark to box to get the box
     */
    private native synchronized ArrayList<int[]> detectFaceJNI(Bitmap image);

    private native int beginActionDetectJNI(int actionType, int detectInterval, float t1, float t2);

    private native int addImageJNI(Bitmap bitmap);

    private native byte[] getDailyImageJNI();

    private native synchronized int getLastErrorCodeJNI();

    private static native int releaseJNIResJNI();
}