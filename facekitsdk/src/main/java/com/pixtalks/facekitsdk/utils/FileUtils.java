package com.pixtalks.facekitsdk.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.Log;

import com.pixtalks.facekitsdk.PConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    @NonNull
    public static boolean copyFileFromRawToOthers(@NonNull final Context context, @RawRes int id, @NonNull final String targetPath) {
        boolean flag = false;
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public static boolean saveStringToSD(@NonNull final String targetPath, @NonNull final String content) {
        boolean flag = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            out.write(content.getBytes());
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
    public static boolean createDir() {
        File dir = new File(PConfig.rootDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(PConfig.projectLogTag, String.format("Fail to mkdirs %s", PConfig.rootDir));
                return false;
            }
        }
        return true;
    }

    public static boolean saveFile(byte[] bytes, String saveFilePath) {
        File saveFile = new File(saveFilePath);
        if (saveFile.exists()) {
            System.out.printf("%s exist, do nothing, check!", saveFile.getAbsolutePath());
            return false;
        }

        try {
            OutputStream out = new FileOutputStream(saveFile);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (IOException ie) {
            ie.printStackTrace();
            return false;
        }
    }



}
