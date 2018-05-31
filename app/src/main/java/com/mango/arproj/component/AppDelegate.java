package com.mango.arproj.component;

import android.app.Application;
import android.content.Context;

import android.util.Log;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

public class AppDelegate extends Application {
    private static final String TAG = "AppDelegate";
    private final CloudPushService pushService;

    public AppDelegate() {
        pushService = PushServiceFactory.getCloudPushService();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initCloudChannel(this);

        //获取deviceID，将其在绑定为阿里云推送中心和云端的推送ID
        final String deviceID = pushService.getDeviceId();

        //推送绑定阿里云设备id
        PushServiceFactory.getCloudPushService().bindAccount(deviceID, new CommonCallback() {
            @Override public void onSuccess(String s){
                Log.d(TAG,"阿里推送绑定成功,推送id为："+deviceID+ " ");
            }

            @Override public void onFailed(String s, String s1){
                Log.d(TAG,"阿里推送绑定失败 "+deviceID+ " ");

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