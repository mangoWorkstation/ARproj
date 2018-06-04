package com.mango.arproj.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

public class ForcePresetPasswordActivity extends AppCompatActivity {

    private Button submitBtn;

    private EditText pwdEditText;

    private String tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_force_preset_password);

        tel = getIntent().getStringExtra("tel");
        pwdEditText = findViewById(R.id.edit_presetpwd);
        submitBtn = findViewById(R.id.btn_presetpwd_set);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPassword();
            }
        });

    }

    private void submitPassword(){

        final String password = pwdEditText.getText().toString();
        this.pwdEditText.setError(null);

        if(TextUtils.isEmpty(password)==false && password.length()>6){

            new Thread(new Runnable() {
                @Override
                public void run() {

                    HashMap<String, String> data = new HashMap<>();
                    data.put("tel",tel);
                    data.put("SHApwd",new Encryptor().SHA512(password));

                    String postBody = JSONEncodeFormatter.parser(10003, data);


                    Log.d("postBody", postBody);

                    //发起okhttp请求
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            //设置请求URL
                            .url(ARutil.getAuthCodeURL())
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
                        HashMap<String, Object> res = JSONDecodeFormatter.decodeDataObject(re);
                        String code = (String) res.get("code");
                        HashMap<String,String> data_obj = (HashMap<String, String>) res.get("data");

                        if("0".compareTo(code)==0){

                            final String token = data_obj.get("token");

                            ForcePresetPasswordActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ForcePresetPasswordActivity.this,"设置成功，请完成资料完善",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ForcePresetPasswordActivity.this,UserProfileImplementationActivity.class);
                                    intent.putExtra("token",token);
                                    startActivity(intent);
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
        else{
            this.pwdEditText.setError("请使用长度超过6位的混合密码");
        }
    }

    @Override
    public void onBackPressed() {
        //TODO 不允许用户返回

        AlertDialog.Builder dialog = new AlertDialog.Builder(ForcePresetPasswordActivity.this);
        dialog.setTitle("宝宝快回来");
        dialog.setMessage("就差一步就完成了！");
        dialog.setCancelable(false);
        dialog.setPositiveButton("好哒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }
}
