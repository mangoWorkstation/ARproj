package com.mango.arproj.entity;

import java.util.HashMap;

/**
 * Created by 芒果君 on 2018/5/31.
 */

public class User {
    private String uuid;
    private String tel;
    private String appKey;
    private String salt;
    private String token;
    private String name;
    private int gender;
    private int age;
    private float weight;
    private float height;
    private float latitude;
    private float longitude;
    private String token_expire_t;
    private String province;
    private String city;
    private int stepCount;
    private int thumbsUpCount;
    private int joinCount;
    private int arCount;
    private String authCode;
    private String authCode_expire_t;
    private String pushID;
    public String getUuid() {
        return uuid;
    }
    public String getTel() {
        return tel;
    }
    public String getAppKey() {
        return appKey;
    }
    public String getSalt() {
        return salt;
    }
    public String getToken() {
        return token;
    }
    public String getName() {
        return name;
    }
    public int getGender() {
        return gender;
    }
    public int getAge() {
        return age;
    }
    public float getWeight() {
        return weight;
    }
    public float getHeight() {
        return height;
    }
    public float getLatitude() {
        return latitude;
    }
    public float getLongitude() {
        return longitude;
    }
    public String getToken_expire_t() {
        return token_expire_t;
    }
    public String getProvince() {
        return province;
    }
    public String getCity() {
        return city;
    }
    public int getStepCount() {
        return stepCount;
    }
    public int getThumbsUpCount() {
        return thumbsUpCount;
    }
    public int getJoinCount() {
        return joinCount;
    }
    public int getArCount() {
        return arCount;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public void setTel(String tel) {
        this.tel = tel;
    }
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
    public void setToken_expire_t(String token_expire_t) {
        this.token_expire_t = token_expire_t;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    public void setThumbsUpCount(int thumbsUpCount) {
        this.thumbsUpCount = thumbsUpCount;
    }
    public void setJoinCount(int joinCount) {
        this.joinCount = joinCount;
    }
    public void setArCount(int arCount) {
        this.arCount = arCount;
    }

    public String getAuthCode() {
        return authCode;
    }
    public String getAuthCode_expire_t() {
        return authCode_expire_t;
    }
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
    public void setAuthCode_expire_t(String authCode_expire_t) {
        this.authCode_expire_t = authCode_expire_t;
    }
    public String getPushID() {
        return pushID;
    }
    public void setPushID(String pushID) {
        this.pushID = pushID;
    }


    /**
     * 此方法与toHashMap方法相同，但过滤了用户的关键安全信息，主要用于向其他用户提供别人的基本信息
     * @return
     */
    public HashMap<String, String> toSecureHashMap(){
        HashMap<String, String> eHashMap = new HashMap<>();

        eHashMap.put("uuid", uuid);
        eHashMap.put("name", name);
        eHashMap.put("gender", String.valueOf(gender));
        eHashMap.put("age", String.valueOf(age));
        eHashMap.put("province", province);
        eHashMap.put("city", city);
        eHashMap.put("weight", String.valueOf(weight));
        eHashMap.put("height", String.valueOf(height));
        eHashMap.put("stepCount", String.valueOf(stepCount));
        eHashMap.put("thumbsUpCount", String.valueOf(thumbsUpCount));
        eHashMap.put("joinCount", String.valueOf(joinCount));

        return eHashMap;
    }
}
