package com.mango.arproj.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestAuthCodeActivity extends AppCompatActivity {

    //指定去往的activity
    private String destination;
    private String token;
    private String uuid;
    private String tel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_auth_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        destination = getIntent().getStringExtra("destination");
        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        tel = getIntent().getStringExtra("tel");


        EditText telText = findViewById(R.id.edit_request_authcode_phone);
        telText.setText(tel);
        telText.setEnabled(false);

        Button submitBtn = findViewById(R.id.btn_request_authocode_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestForAuthCode(token);
            }
        });


    }

    private void requestForAuthCode(final String token){


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                //添加data字段，分别装入token和pushID两个值
//                HashMap<String, String> data = new HashMap<>();
//                data.put("token", token);
//
//                String postBody = JSONEncodeFormatter.parser(10017, data);
//
//                Log.d("postBody", postBody);
//
//                //发起okhttp请求
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        //设置请求URL
//                        .url(ARutil.getAuthCodeURL())
//                        //装入处理后的字符串，使用post方式
//                        .post(RequestBody.create(
//                                MediaType.parse("application/json; charset=utf-8"),
//                                postBody))
//                        .build();
//
//                //获取网络请求的response
//                Response response = null;
//                try {
//                    response = client.newCall(request).execute();
//                    //获取response中的字符
//                    final String re = response.body().string();
//                    HashMap<String, String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
//                    String code = simpleMsg.get("code");
//                    if ("0".compareTo(code) == 0) {
//                        RequestAuthCodeActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(RequestAuthCodeActivity.this, "验证码已经发送！", Toast.LENGTH_LONG).show();
//                                if("changeTel".compareTo(destination)==0){
//                                    Intent intent = new Intent(RequestAuthCodeActivity.this,ChangeTelAuthActivity.class);
//                                    intent.putExtra("token",token);
//                                    startActivity(intent);
//                                }
//                                if("resetPwd".compareTo(destination)==0){
//                                    //TODO intent implemented here.
//                                }
//                            }
//                        });
//                    }
//                    //在主线程上更新本活动UI，不可在主线程上直接更新，将会造成闪退
//                    Log.d("res", re);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        Toast.makeText(RequestAuthCodeActivity.this, "验证码已经发送！", Toast.LENGTH_LONG).show();
        if("changeTel".compareTo(destination)==0){
            Intent intent = new Intent(RequestAuthCodeActivity.this,ChangeTelAuthActivity.class);
            intent.putExtra("token",token);
            startActivity(intent);
        }
        if("resetPwd".compareTo(destination)==0){
            //TODO intent implemented here.
        }
    }


}
