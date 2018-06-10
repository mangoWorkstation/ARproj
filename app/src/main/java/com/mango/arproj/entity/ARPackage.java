package com.mango.arproj.entity;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by mangguojun on 2018/5/31.
 */

public class ARPackage {

    private String uid;
    private String roomid;
    private double latitude;
    private double longitude;
    private String content;
    public String getUid() {
        return uid;
    }
    public String getRoomid() {
        return roomid;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
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
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public HashMap<String,String> toSecureHashMap(){
        HashMap<String,String> e = new HashMap<>();

        e.put("latitude",String.valueOf(latitude));
        e.put("longitude",String.valueOf(longitude));
        e.put("content",content);

        return e;
    }


}
