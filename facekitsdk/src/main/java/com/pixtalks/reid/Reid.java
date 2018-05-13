package com.pixtalks.reid;

import android.graphics.Bitmap;
import android.support.annotation.Keep;
import android.util.Log;

import com.pixtalks.facekitsdk.PConfig;

import java.io.File;

public class Reid {

    private boolean inited = false;
    private int modelType;
    private int lastErrorCode = 0;

    public Reid(int modelTpye) {
        this.modelType = modelTpye;
    }

    /**
     * @param licencePath
     * @param prototxt
     * @param binary
     * @return
     */
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
        int ret = -1;
        switch (modelType) {
            case 1:
                ret = loadReidModelJNI(licencePath, prototxt, binary, 1, 0, 78, cpuNumber);
                break;
            case 2:
                ret = loadReidModelJNI(licencePath, prototxt, binary, 2, 0, 92, cpuNumber);
                break;
            case 3:
                ret = loadReidModelJNI(licencePath, prototxt, binary, 3, 0, -1, cpuNumber);
                break;
        }
        inited = (ret == PConfig.okCode);
        return ret;
    }

    public float[] getFeature(Bitmap image, int[] landmark) {
        if (!inited) {
            Log.e(PConfig.reidLogTag, "Please init reid model and make sure init success\n");
            return null;
        }
        if (image == null || landmark == null || landmark.length != 10) {
            Log.e(PConfig.reidLogTag, "image == null or invalid landmark.\n");
        }
        float[] feature = getFeatureJNI(image, landmark);
        if (feature == null || feature.length < PConfig.featureMinSize) {
            this.lastErrorCode = getLastErrorCodeJNI();
        }
        return feature;
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
    private native int loadReidModelJNI(String licencePath, byte[] prototxt, byte[] binary, int modelType, int inputIndex, int outputIndex, int threadNumber);

    /**
     * 获取人脸特征，每次仅支持一个人脸特征获取
     *
     * @param image    图片
     * @param landmark 通过检测库获取5个关键点坐标(左眼、右眼、鼻子、左嘴角、右嘴角)
     * @return 人脸特征
     */
    @Keep
    private native float[] getFeatureJNI(Bitmap image, int[] landmark);

    private native int getLastErrorCodeJNI();
}
