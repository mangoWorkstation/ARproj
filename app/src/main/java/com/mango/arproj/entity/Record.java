package com.mango.arproj.entity;

/**
 * Created by mangguojun on 2018/5/31.
 */

public class Record {
    private String uid;
    private String userid;
    private String start_t;
    private String end_t;
    private int stepCount;
    private int arCount;

    public String getUid() {
        return uid;
    }
    public String getUserid() {
        return userid;
    }
    public String getStart_t() {
        return start_t;
    }
    public String getEnd_t() {
        return end_t;
    }
    public int getStepCount() {
        return stepCount;
    }
    public int getArCount() {
        return arCount;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public void setStart_t(String start_t) {
        this.start_t = start_t;
    }
    public void setEnd_t(String end_t) {
        this.end_t = end_t;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    public void setArCount(int arCount) {
        this.arCount = arCount;
    }
}
