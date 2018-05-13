package com.pixtalks.reid;

import android.graphics.Bitmap;
import android.support.annotation.Keep;
import android.util.Log;

import com.pixtalks.facekitsdk.PConfig;

import java.io.File;

public class Liveness {

    private boolean inited = false;
    private int lastErrorCode = 0;

    public Liveness() {
    }

    public int loadModel(String licencePath, byte[] prototxt, byte[] binary) {

        if (licencePath == null || licencePath.length() < 1 ||
                prototxt == null || prototxt.length < 1 ||
                binary == null || binary.length < 1) {
            return PConfig.invalidArgCode;
        }

        if (!new File(licencePath).isFile()) {
            return PConfig.licenceNotFoundCode;
        }

        int cpuNumber = PConfig.reidUseCpuNumber;
        int ret = loadLivenessModelJNI(licencePath, prototxt, binary, 1, 0, 14, cpuNumber);
        inited = (ret == PConfig.okCode);
        return ret;
    }

    public boolean isLiveness(Bitmap image, int[] landmark) {
        if (!inited) {
            Log.e(PConfig.reidLogTag, "Please init reid model and make sure init success\n");
            return false;
        }
        if (image == null || landmark == null || landmark.length != 10) {
            Log.e(PConfig.reidLogTag, "image == null or invalid landmark.\n");
        }
        float score = livenessConfidenceJNI(image, landmark);

        return score > PConfig.livenessThreshold;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }

    /**
     * licencePath: It may be from licence server, so it should be save at sd card
     * <p>
     * The inputIndex and outputIndex relate to model type, so do not change it.
     *
     * @return 0 success, other value see com.pixtalks.facekitsdk.PConfig.java
     */
    @Keep
    private native int loadLivenessModelJNI(String licencePath, byte[] prototxt, byte[] binary, int modelType, int inputIndex, int outputIndex, int threadNumber);

    /**
     *
     * @param image    图片
     * @param landmark 通过检测库获取5个关键点坐标(左眼、右眼、鼻子、左嘴角、右嘴角)
     * @return 是活体的置信度
     */
    @Keep
    private native float livenessConfidenceJNI(Bitmap image, int[] landmark);

    private native int getLastErrorCodeJNI();
}
