package com.pixtalks.facekitsdk.utils;

import java.io.File;

public class PixtalksLicence {

    public static boolean isValidLicence(String licencePath) {
        if (licencePath == null || licencePath.length() < 1) {
            return false;
        }

        return new File(licencePath).isFile() && isValidLicenceJNI(licencePath);
    }

    public static String getHardwareInfo() {
        return getHardwareInfoJNI();
    }

    public static String getVersionInfo() {
        return getVersionInfoJNI();
    }

    public static String getAuthInfo() {
        return getAuthInfoJNI();
    }

    private static native String getVersionInfoJNI();

    private static native String getAuthInfoJNI();

    private static native String getHardwareInfoJNI();

    private static native boolean isValidLicenceJNI(String licencePath);


}
