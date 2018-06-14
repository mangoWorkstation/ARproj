package com.mango.arproj.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.Encryptor;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPwdActivity extends AppCompatActivity {

    private String token;

    private EditText authCodeEdit;
    private EditText newPwdEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getStringExtra("token");

        authCodeEdit = findViewById(R.id.edit_resetPwd_authcode);
        newPwdEdit = findViewById(R.id.edit_resetPwd_newPwd);

        Button submitBtn = findViewById(R.id.btn_resetpwd_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestForResetPwd(token);
            }
        });

    }

    private void requestForResetPwd(final String token){

        authCodeEdit.setError(null);
        newPwdEdit.setError(null);

        final String authcode = authCodeEdit.getText().toString();
        final String newPwd = newPwdEdit.getText().toString();

        if(TextUtils.isEmpty(authcode)){
            authCodeEdit.setError("验证码不可为空");
            return;
        }

        if(TextUtils.isEmpty(newPwd) || newPwd.length()<6){
            newPwdEdit.setError("密码应该是数字密码混合 长度大于6位");
            return;
        }



        new Thread(new Runnable() {
            @Override
            public void run() {

                //添加data字段，分别装入token和pushID两个值
                HashMap<String, String> data = new HashMap<>();
                data.put("token", token);
                data.put("authCode",authcode);
                data.put("newSHAPwd",new Encryptor().SHA512(newPwd));

                String postBody = JSONEncodeFormatter.parser(10020, data);

                Log.d("postBody", postBody);

                //发起okhttp请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //设置请求URL
                        .url(ARutil.getUserURL())
                        //装入处理后的字符串，使用post方式
                        .post(RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"),
                                postBody))
                        .build();

                //获取网络请求的response
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    //获取response中的字符
                    final String re = response.body().string();
                    HashMap<String, String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
                    String code = simpleMsg.get("code");
                    if ("0".compareTo(code) == 0) {
                        ResetPwdActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ResetPwdActivity.this, "密码重置成功！请重新登录！", Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = getSharedPreferences(ARutil.getSharePreferencePath(),MODE_PRIVATE).edit();
                                editor.remove("token");
                                editor.remove("uuid");
                                editor.remove("tel");
                                editor.apply();
                                PushServiceFactory.getCloudPushService().unbindAccount(new CommonCallback() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.d("SettingsActivity","阿里云推送解绑成功");
                                    }

                                    @Override
                                    public void onFailed(String s, String s1) {
                                        Log.d("SettingsActivity","阿里云推送解绑失败");

                                    }
                                });
                                finish();
                            }
                        });
                    }
                    else{
                        ResetPwdActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ResetPwdActivity.this, "服务器正忙，修改失败！", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                    //在主线程上更新本活动UI，不可在主线程上直接更新，将会造成闪退
                    Log.d("res", re);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
