package com.pixtalks.facekitsdk.utils;

import android.util.Log;

import com.pixtalks.facekitsdk.PConfig;

public class ModelSelector {
    int modelId = 0;
    public ModelSelector(int modelId) {
        if (modelId != 1 && modelId != 2) {
            Log.e(PConfig.projectLogTag, "Bad model type, please read SDK document or contact pixtalks");
            return;
        }
        this.modelId = modelId;
    }

    public int getModelId() {
        return modelId;
    }

    public String getDetectProtoFileName(){
        return PConfig.getDpFileName();
    }

    public String getDetectBinaryFileName(){
        return PConfig.getDbFileName();
    }

    public String getLandmarkProtoFileName(){
        return PConfig.getLmpFileName();
    }

    public String getLandmarkBinaryFileName(){
        return PConfig.getLmBFileName();
    }

    public String getProtoFileName() {
        switch (modelId) {
            case 1:
                return PConfig.getMp127FileName();
            case 2:
                return PConfig.getMp85FileName();
            default:
                return null;
        }
    }

    public String getBinaryFileName() {
        switch (modelId) {
            case 1:
                return PConfig.getMb127FileName();
            case 2:
                return PConfig.getMb85FileName();
            default:
                return null;
        }
    }

    public String getLivenessProtoFileName(){
        return PConfig.getLp120FileName();
    }

    public String getLivenessBinaryFileName(){
        return PConfig.getLb120FileName();
    }

    public float mapScore(float initScore) {
        switch (modelId) {
            case 1:
                return ScoreMapper.mapModel127Score(initScore);
            case 2:
                return ScoreMapper.mapModel85Score(initScore);
            case 3:
                return ScoreMapper.mapModel130Score(initScore);
            default:
                return -1;
        }
    }
}
