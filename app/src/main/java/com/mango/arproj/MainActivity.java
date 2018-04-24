package com.mango.arproj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.mango.util.JSONDecodeFormatter;
import com.mango.util.JSONEncodeFormatter;


import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{

    private CloudPushService mPushService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button sendBtn = findViewById(R.id.btn_sendRequest);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String,String> data = new HashMap<>();
                        data.put("token","228769e8-1139-4682-ac8c-8dda21490ef3");
                        data.put("pushID","123456");

                        String postBody = JSONEncodeFormatter.parser(10006,data);


                        Log.d("postBody",postBody);

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://120.78.177.77/ar/api/login")
                                .post(RequestBody.create(
                                        MediaType.parse("application/json; charset=utf-8"),
                                        postBody))
                                .build();

                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            final String re = response.body().string();
                            //在主线程上更新UI
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    HashMap<String,String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
                                    Log.d("simpleMsg",simpleMsg.toString());
                                    String str = "code:"+simpleMsg.get("code")+"\ntimestamps:"+simpleMsg.get("timestamp")+"\nmsg:"+simpleMsg.get("msg");
                                    Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                                }
                            });
                            Log.d("res",re);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
