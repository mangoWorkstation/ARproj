package com.mango.arproj.entity;

/**
 * Created by mangguojun on 2018/5/31.
 */

public class Room {
    private String uid;
    private String joinCode;
    private String joinCode_expire_t;
    private int state;
    private int userCount;
    private String organizerid;
    private String created_t;

    public String getUid() {
        return uid;
    }
    public String getJoinCode() {
        return joinCode;
    }
    public String getJoinCode_expire_t() {
        return joinCode_expire_t;
    }
    public int getState() {
        return state;
    }
    public int getUserCount() {
        return userCount;
    }
    public String getOrganizerid() {
        return organizerid;
    }
    public String getCreated_t() {
        return created_t;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
    public void setJoinCode_expire_t(String joinCode_expire_t) {
        this.joinCode_expire_t = joinCode_expire_t;
    }
    public void setState(int state) {
        this.state = state;
    }
    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }
    public void setOrganizerid(String organizerid) {
        this.organizerid = organizerid;
    }
    public void setCreated_t(String created_t) {
        this.created_t = created_t;
    }
}
