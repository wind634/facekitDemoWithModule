package com.pixtalks.facekitsdk.utils;

public class ScoreMapper {

    private static final float PRECISION = 0.0001f;

    // 127 had change to sq
    public static float mapModel127Score(float score) {
        float s0 = 11.0f;
        float s60 = 10 - 0.304069f;
        float s80 = 10 - 0.407142f;
        float s90 = 10 - 0.506159f;
        float s100 = 9.0f;

        //invalid score
        if (score - s0 > PRECISION || s100 - score > PRECISION) {
            return -1;
        }
        // 原始score是越小越像
        // 90 - 100
        if (s90 - score >= PRECISION) {
            return 100 - 10 * (score - s100) / (s90 - s100);
            //80-90
        } else if (s80 - score >= PRECISION) {
            return 90 - 10 * (score - s90) / (s80 - s90);
            //60-80
        } else if (s60 - score >= PRECISION) {
            return 80 - 20 * (score - s80) / (s60 - s80);
            //0-60
        } else {
            return 60 - 60 * (score - s60) / (s0 - s60);
        }
    }

    public static float mapModel85Score(float score) {
        float s0 = 11.0f;
        float s60 = 9.714853f;
        float s80 = 9.619896f;
        float s90 = 9.539014f;
        float s100 = 9.0f;

        //invalid score
        if (score - s0 > PRECISION || s100 - score > PRECISION) {
            return -1;
        }
        // 原始score是越小越像
        // 90 - 100
        if (s90 - score >= PRECISION) {
            return 100 - 10 * (score - s100) / (s90 - s100);
            //80-90
        } else if (s80 - score >= PRECISION) {
            return 90 - 10 * (score - s90) / (s80 - s90);
            //60-80
        } else if (s60 - score >= PRECISION) {
            return 80 - 20 * (score - s80) / (s60 - s80);
            //0-60
        } else {
            return 60 - 60 * (score - s60) / (s0 - s60);
        }
    }

    public static float mapModel130Score(float score) {
        float s0 = 11.0f;
        float s60 = 9.689124f;
        float s80 = 9.598135f;
        float s90 = 9.524437f;
        float s100 = 9.0f;

        //invalid score
        if (score - s0 > PRECISION || s100 - score > PRECISION) {
            return -1;
        }
        // 原始score是越小越像
        // 90 - 100
        if (s90 - score >= PRECISION) {
            return 100 - 10 * (score - s100) / (s90 - s100);
            //80-90
        } else if (s80 - score >= PRECISION) {
            return 90 - 10 * (score - s90) / (s80 - s90);
            //60-80
        } else if (s60 - score >= PRECISION) {
            return 80 - 20 * (score - s80) / (s60 - s80);
            //0-60
        } else {
            return 60 - 60 * (score - s60) / (s0 - s60);
        }
    }
}
