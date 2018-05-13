package com.pixtalks.detect;

public class DetectResult {
    private int box[];
    private int landmark[];

    public DetectResult(int[] box, int[] landmark) {
        this.box = box;
        this.landmark = landmark;
    }

    public int[] getBox() {
        return box;
    }

    public int[] getLandmark() {
        return landmark;
    }
}

