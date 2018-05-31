package com.mango.arproj.entity;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by mangguojun on 2018/5/31.
 */

public class ARPackage {

    private String uid;
    private String roomid;
    private float latitude;
    private float longitude;
    private String content;
    public String getUid() {
        return uid;
    }
    public String getRoomid() {
        return roomid;
    }
    public float getLatitude() {
        return latitude;
    }
    public float getLongitude() {
        return longitude;
    }
    public String getContent() {
        return content;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
    public void setContent(String content) {
        this.content = content;
    }


}
