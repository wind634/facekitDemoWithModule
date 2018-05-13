package com.pixtalks.facekitsdk;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.pixtalks.facekitsdk.utils.FileUtils;
import com.saicmotor.facekitsdk.R;

import java.io.File;

public class Init {
    public static boolean copyFilesFromRawToSD(@NonNull final Context context) {
        FileUtils.createDir();
        boolean flag = true;
        if (!new File(PConfig.getPixtalksLicencePath()).exists()) {
            flag &= FileUtils.copyFileFromRawToOthers(context, R.raw.android_facekit, PConfig.getPixtalksLicencePath());
        }

        Log.e(PConfig.projectLogTag, PConfig.getImg1Path());

        if (!new File(PConfig.getImg1Path()).exists()) {
            flag &= FileUtils.copyFileFromRawToOthers(context, R.raw.o3, PConfig.getImg1Path());
        }

        if (!new File(PConfig.getImg2Path()).exists()) {
            flag &= FileUtils.copyFileFromRawToOthers(context, R.raw.p4, PConfig.getImg2Path());
        }

        if (!new File(PConfig.getImg3Path()).exists()) {
            flag &= FileUtils.copyFileFromRawToOthers(context, R.raw.m2, PConfig.getImg3Path());
        }
        return flag;
    }
}
