package com.mango.arproj.util;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by 芒果君 on 2018/5/31.
 * 用于将对象转换成为字符串 或 哈希表
 * 2018/05/31 测试通过
 */

public class ClazzTransformer {

    private static String TAG = "ClazzTransformer";

    /**
     * 对象转换成为字符串
     * @param object 对象
     * @return 属性字符串
     */
    public static String toString(Object object){
        String str = "["+object.getClass().getName()+"] ";
        Field[] fields = object.getClass().getDeclaredFields();
        for(Field f:fields) {
            try {
                str += " "+f.getName() + " = " + String.valueOf(f.get(object))+"; ";
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    /**
     * 将对象转换成为HashMap，可排除对象内部的某些属性字段
     * @param object 对象
     * @param excludeKeys 需要排除的属性字段
     * @return 哈希表
     */
    public static HashMap<String,String> toHashMap(Object object,String[] excludeKeys){

        HashMap<String,String> eHashMap = toHashMap(object);

        if(excludeKeys!=null){
            for (String k :excludeKeys){
                try {
                    eHashMap.remove(k);
                }
                catch (Exception e){
                    Log.e(TAG,"Excluded Keys NOT FOUND.");
                    return null;
                }
            }
        }

        return eHashMap;
    }

    /**
     * 将对象转换成为HashMap
     * @param object 对象
     * @return 哈希表
     */
    public static HashMap<String,String> toHashMap(Object object){
        HashMap<String, String> eHashMap = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for(Field f:fields) {
            try {
                eHashMap.put(f.getName(),String.valueOf(f.get(object)));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return eHashMap;
    }

    /**
     * 获取对象的标识符（类名）为TAG
     * @param object
     * @return TAG
     */
    public static String getClazzTAG(Object object){
        return object.getClass().getName();
    }
}
