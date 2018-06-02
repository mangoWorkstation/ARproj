package com.mango.arproj.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.mango.arproj.R;
import com.mango.arproj.util.ARurl;
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

public class LoginActivity extends AppCompatActivity{


    // UI references.
    private AutoCompleteTextView phoneView;
    private EditText passwordView;
    private View mProgressView;
    private View mLoginFormView;

    private String rex = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        phoneView = (AutoCompleteTextView) findViewById(R.id.edit_login_phone);
        passwordView = (EditText) findViewById(R.id.edit_login_pwd);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button loginButton = (Button) findViewById(R.id.btn_login_login);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        phoneView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        final String phone = phoneView.getText().toString();
        final String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError("无效的密码");
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            phoneView.setError("手机号不可为空");
            focusView = phoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            phoneView.setError("无效的手机号，11位噢");
            focusView = phoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    //添加data字段，分别装入token和pushID两个值
                    HashMap<String, String> data = new HashMap<>();
                    data.put("tel",phone);
                    data.put("pushID", PushServiceFactory.getCloudPushService().getDeviceId());
                    data.put("SHAPwd",new Encryptor().SHA512(password));

                    //将消息和状态码10006装入JSON格式化器中，返回处理后的JSON字符串
                    String postBody = JSONEncodeFormatter.parser(10005, data);


                    Log.d("postBody", postBody);

                    //发起okhttp请求
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            //设置请求URL
                            .url(ARurl.getLoginURL())
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
                        //在主线程上更新本活动UI，不可在主线程上直接更新，将会造成闪退
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                HashMap<String, String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
                                String str = "code:" + simpleMsg.get("code") + "\ntimestamps:" + simpleMsg.get("timestamp") + "\ndata:" + simpleMsg.get("data").toString();
                                Toast.makeText(LoginActivity.this, str, Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.d("res", re);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    private boolean isPhoneValid(String phone) {
        return phone.matches(rex);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
}

