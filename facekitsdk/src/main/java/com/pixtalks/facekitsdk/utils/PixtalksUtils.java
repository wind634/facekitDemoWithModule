package com.pixtalks.facekitsdk.utils;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.pixtalks.facekitsdk.PConfig;
import com.pixtalks.facekitsdk.type.LicenceServerResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class PixtalksUtils {

    private static final int REQUEST_CODE_PERMISSION = 2;

    // Storage Permissions
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    private static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean verifyPermissions(Activity activity) {
        // Check if we have write permission
        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_persmission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (write_permission != PackageManager.PERMISSION_GRANTED ||
                read_persmission != PackageManager.PERMISSION_GRANTED ||
                camera_permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_REQ,
                    REQUEST_CODE_PERMISSION
            );
            return false;
        } else {
            return true;
        }
    }

    public static void getLicenceFromSever(String username, String authCode, String machineCode) {

        new AsyncTask<String, Void, LicenceServerResult>() {
            @Override
            protected LicenceServerResult doInBackground(String... arg0) {
                try {
                    String username = arg0[0];
                    String authCode = arg0[1];
                    String machineCode = arg0[2];
                    System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
                    System.setProperty("sun.net.client.defaultReadTimeout", "15000");
                    URL url = new URL(PConfig.getLicenceServer());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("accept", "*/*");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Content-Type", "text/json");
                    connection.connect();

                    JSONObject requestData = new JSONObject();
                    //send
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    requestData.put("username", username);
                    requestData.put("code", "Android");
                    requestData.put("authCode", authCode);
                    requestData.put("machineCode", machineCode);
                    requestData.put("V2", true);
                    out.write(requestData.toString().getBytes("utf-8"));
                    out.flush();
                    out.close();

                    //response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    StringBuilder responseBuffer = new StringBuilder("");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuffer.append(line);
                    }
                    reader.close();

                    connection.disconnect();
                    Log.e(PConfig.projectLogTag, "From licence server " +responseBuffer.toString());
                    JSONObject resultJSON = new JSONObject(responseBuffer.toString());
                    int code = resultJSON.getInt("respCode");
                    String content = resultJSON.getString("content");

                    return new LicenceServerResult(code, content);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(LicenceServerResult licenceServerResult) {
                if (licenceServerResult != null) {
                    // 从licence 服务器返回的不一定就是合法licence, 服务器端要对访问校验
                    if (licenceServerResult.getCode() == PConfig.gotLicenceCode) {
                        if(!FileUtils.saveStringToSD(PConfig.getPixtalksLicencePath(), licenceServerResult.getContent())){
                            Log.e(PConfig.projectLogTag, "Fail to write licence to SD card\n");
                        } else{
                            Log.e(PConfig.projectLogTag, "Success got licence\n");
                        }
                    } else {
                        Log.e(PConfig.projectLogTag, "No licence licence got:" + licenceServerResult.getContent());
                    }
                } else {
                    Log.e(PConfig.projectLogTag, "Fail to get licence from server");
                }
            }
        }.execute(username, authCode, machineCode);
    }

    public static float[] normalizeFeature(float[] feature) {
        if (feature == null || feature.length < 1) {
            Log.e(PConfig.projectLogTag, "empty feature pass to normalizeFeature, take care!");
            return null;
        }
        float sum = 0;
        for (float aFeature : feature) {
            sum += aFeature * aFeature;
        }
        sum = (float) Math.sqrt(sum);

        for (int i = 0; i < feature.length; i++) {
            feature[i] = feature[i] / sum;
        }
        return feature;
    }

    public static float initScore(float[] fea1, float[] fea2) {
        if (fea1 == null || fea1.length < 1) {
            return Float.MAX_VALUE;
        }
        if (fea2 == null || fea2.length < 1) {
            return Float.MAX_VALUE;
        }
        if (fea1.length != fea2.length) {
            System.out.printf("ele1.length != ele2.length");
            return Float.MAX_VALUE;
        }
        float sum = 0;
        for (int i = 0; i < fea1.length; i++) {
            sum += fea1[i] * fea2[i];
        }
        // TODO 注意完善文档 sum取反和加10的原因
        return 10 - sum;
    }
}
