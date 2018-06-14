package com.mango.arproj.activity;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class ChangeTelAuthActivity extends AppCompatActivity {

    private String token;

    private EditText oldAuthCodeEditText;
    private EditText newPhoneEditText;
    private EditText newAuthCodeEditText;

    private Button autheticOldAuthcodeBtn;
    private Button submitChangeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_tel_auth);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getStringExtra("token");

        oldAuthCodeEditText = findViewById(R.id.edit_change_tel_oldAuthcode);
        newPhoneEditText = findViewById(R.id.editText_change_tel_newTel);
        newAuthCodeEditText = findViewById(R.id.editText_change_tel_newAuthcode);
        newAuthCodeEditText.setEnabled(false);

        autheticOldAuthcodeBtn = findViewById(R.id.btn_change_tel_requestNewAuthcode);
        autheticOldAuthcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestForAuthOldTel(token,view);
            }
        });
        submitChangeBtn = findViewById(R.id.btn_change_tel_submit);
        submitChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestForChangeSubmit(token);
            }
        });
        submitChangeBtn.setEnabled(false);

    }

    private void requestForAuthOldTel(final String token, View view){

        final Button b = (Button)view;

        oldAuthCodeEditText.setError(null);
        newPhoneEditText.setError(null);

        if(TextUtils.isEmpty(oldAuthCodeEditText.getText().toString())){
            oldAuthCodeEditText.setError("验证码不可为空");
            return;
        }

        if(TextUtils.isEmpty(newPhoneEditText.getText().toString()) || !newPhoneEditText.getText().toString().matches(ARutil.getTelRex())){
            newPhoneEditText.setError("新手机不合法噢！");
            return;
        }

        final String oldAuthCode = oldAuthCodeEditText.getText().toString();
        final String newPhone = newPhoneEditText.getText().toString();


        //设置倒计时按钮
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                b.setEnabled(false);
                b.setText(millisUntilFinished / 1000 + "秒后重新获取");

            }

            @Override
            public void onFinish() {
                b.setEnabled(true);
                b.setText("重新获取验证码");

            }
        }.start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                //添加data字段，分别装入token和pushID两个值
                HashMap<String, String> data = new HashMap<>();
                data.put("token", token);
                data.put("authCode",oldAuthCode);
                data.put("newTel",newPhone);
                String postBody = JSONEncodeFormatter.parser(10018, data);

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
                        ChangeTelAuthActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                submitChangeBtn.setEnabled(true);
                                newAuthCodeEditText.setEnabled(true);
                                Toast.makeText(ChangeTelAuthActivity.this, "验证成功，新验证码已发送！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        ChangeTelAuthActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChangeTelAuthActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
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

//        submitChangeBtn.setEnabled(true);
//        newAuthCodeEditText.setEnabled(true);
//        Toast.makeText(ChangeTelAuthActivity.this, "验证成功，新验证码已发送！", Toast.LENGTH_SHORT).show();

    }

    private void requestForChangeSubmit(final String token){

        newAuthCodeEditText.setError(null);
        newPhoneEditText.setError(null);

        if(TextUtils.isEmpty(newAuthCodeEditText.getText().toString())){
            newAuthCodeEditText.setError("验证码不可为空");
            return;
        }

        if(TextUtils.isEmpty(newPhoneEditText.getText().toString()) || !newPhoneEditText.getText().toString().matches(ARutil.getTelRex())){
            newPhoneEditText.setError("新手机不合法噢！");
            return;
        }

        final String newAuthCode = newAuthCodeEditText.getText().toString();
        final String newPhone = newPhoneEditText.getText().toString();


        new Thread(new Runnable() {
            @Override
            public void run() {

                //添加data字段，分别装入token和pushID两个值
                HashMap<String, String> data = new HashMap<>();
                data.put("token", token);
                data.put("authCode",newAuthCode);
                data.put("newTel",newPhone);
                String postBody = JSONEncodeFormatter.parser(10019, data);

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
                        ChangeTelAuthActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                submitChangeBtn.setEnabled(true);
                                Toast.makeText(ChangeTelAuthActivity.this, "修改成功！", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                    else{
                        ChangeTelAuthActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChangeTelAuthActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
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
