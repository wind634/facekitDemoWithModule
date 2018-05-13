package com.pixtalks.facekitsdk.utils;

import android.graphics.Bitmap;

public class CoordUtils {
    public static float width_extend_factor = 2.3f;
    public static float height_extend_factor = 2f;

    // TODO 请根据需要调整box大小
    // landmark一共10个值, 分别是: (图片左边眼睛x,图片左边眼睛y) (图片右边眼睛x, 图片右边眼睛y) (0, 0) (0,0) (0,0)
    public static int[] landmarkToBox(Bitmap image, int[] landmark) {
        if (landmark == null || landmark.length != 10) {
            return null;
        }
        int width = landmark[2] - landmark[0];
        float height = 1.2f * width;
        float x1 = Math.max(0.f, landmark[0] - width * width_extend_factor);
        float y1 = Math.max(0.f, landmark[1] - height * height_extend_factor);
        float x2 = Math.min(landmark[2] + width * width_extend_factor, image.getWidth());
        float y2 = Math.min(landmark[3] + height + height * height_extend_factor, image.getHeight());
        int[] box = new int[4];
        box[0] = (int) x1;
        box[1] = (int) y1;
        box[2] = (int) x2;
        box[3] = (int) y2;
        return box;
    }


//    // 纷翔特供
//    public static float height_rate = 1.2f;
//    public static float width_extend_factor = 1.4f;
//    public static float height_extend_factor = 1.6f;
//
//    public static int[] landmarkToBox(Bitmap image, int[] landmark) {
//        if (landmark == null || landmark.length != 10) {
//            return null;
//        }
//        int width = landmark[2] - landmark[0];
//        float height = height_rate * width;
//
//        float x1 = Math.max(0.f, landmark[0] - width * width_extend_factor);
//        float y1 = Math.max(0.f, landmark[1] - height * height_extend_factor);
//        float x2 = Math.min(landmark[0] + width * width_extend_factor, image.getWidth());
//        float y2 = Math.min(landmark[1] + height * height_extend_factor, image.getHeight());
//
//        int[] box = new int[4];
//
//        //  纷翔设备
//        box[0] = (int) x1 + 22;
//        box[1] = (int) y1;
//        box[2] = (int) x2 + 22;
//        box[3] = (int) y2;
//
//        return box;
//    }

}
