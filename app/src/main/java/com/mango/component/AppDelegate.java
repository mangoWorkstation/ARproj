package com.mango.component;

import android.app.Application;
import android.content.Context;

import android.util.Log;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

public class AppDelegate extends Application {
    private static final String TAG = "Init";
    @Override
    public void onCreate() {
        super.onCreate();
        initCloudChannel(this);
        final String account = "228769e811394682ac8c8dda21490ef3";

        //推送绑定阿里云设备id
        PushServiceFactory.getCloudPushService().bindAccount(account, new CommonCallback()
        {
            @Override public void onSuccess(String s){
                Log.d("bindResult","阿里推送绑定成功 "+s+ " ");
            }

            @Override public void onFailed(String s, String s1){
                Log.d("bindResult","阿里推送绑定失败 "+s+ " ");

            }
        });

        PushServiceFactory.getCloudPushService().setPushIntentService(AliyunMsgServiceCenter.class);


    }
    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "init cloudchannel success");
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }
}