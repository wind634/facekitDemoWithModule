package com.pixtalks.facekitsdk;

/**
 * Created by wangjiang on 2018/2/19.
 */

public class CompareResult {

    private int index;
    private float score;

    public CompareResult(int index, float score) {
        this.index = index;
        this.score = score;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
