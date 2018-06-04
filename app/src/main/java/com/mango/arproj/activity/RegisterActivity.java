package com.mango.arproj.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Slide;
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

import java.io.IOException;
import java.util.HashMap;

import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class RegisterActivity extends AppCompatActivity{

    // UI references.
    private AutoCompleteTextView phoneView;
    private EditText authcodeView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置转场动画
        Slide slide = new Slide();
        slide.setDuration(200);
        getWindow().setEnterTransition(slide);

        setContentView(R.layout.activity_register);

        mLoginFormView = findViewById(R.id.scrollView_register_form);
        mProgressView = findViewById(R.id.progress_register);
        phoneView = (AutoCompleteTextView) findViewById(R.id.edit_register_phone);
        authcodeView = (EditText) findViewById(R.id.edit_register_authcode_input);
        authcodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    submitRegister();
                    return true;
                }
                return false;
            }
        });

        //获取验证码按钮
        Button requestAuthCodeBtn = findViewById(R.id.btn_register_authcode);
        requestAuthCodeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAuthCode(view);
            }
        });

        //验证注册按钮
        Button registerBtn = findViewById(R.id.btn_register_submit);
        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRegister();
            }
        });


    }

    /**
     * 请求验证注册码
     */
    private void requestAuthCode(View view){
        final Button b = (Button) view;

        // Reset errors.
        phoneView.setError(null);
        authcodeView.setError(null);

        // Store values at the time of the login attempt.
        final String phone = phoneView.getText().toString();
        final String authcode = authcodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid authcode, if the user entered one.
        if (!TextUtils.isEmpty(authcode) && !isAuthCodeValid(authcode)) {
            authcodeView.setError("验证码不可为空");
            focusView = authcodeView;
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
                    data.put("tel", phone);

                    String postBody = JSONEncodeFormatter.parser(10001, data);


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
                        HashMap<String, String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
                        String code = simpleMsg.get("code");
                        if ("0".compareTo(code) == 0) {
                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(RegisterActivity.this, "验证码已经发送！", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if("90003".compareTo(code)==0){

                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(RegisterActivity.this, "手机号已被注册", Toast.LENGTH_LONG).show();

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

    /**
     * 提交验证注册
     */
    private void submitRegister() {

        // Reset errors.
        phoneView.setError(null);
        authcodeView.setError(null);

        // Store values at the time of the login attempt.
        final String phone = phoneView.getText().toString();
        final String authcode = authcodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid authcode, if the user entered one.
        if (!TextUtils.isEmpty(authcode) && !isAuthCodeValid(authcode)) {
            authcodeView.setError("验证码不可为空");
            focusView = authcodeView;
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
                    data.put("authCode",authcode);

                    String postBody = JSONEncodeFormatter.parser(10002, data);


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
                        HashMap<String, String> simpleMsg = JSONDecodeFormatter.decodeSimpleMsg(re);
                        String code = simpleMsg.get("code");
                        if("0".compareTo(code)==0){
                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this,"验证成功",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegisterActivity.this,ForcePresetPasswordActivity.class);
                                    intent.putExtra("tel",phone);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else if("90004".compareTo(code)==0){

                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(RegisterActivity.this,"错误的验证码",Toast.LENGTH_LONG).show();

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

    private boolean isPhoneValid(String phone) {
        return phone.matches(ARutil.getTelRex());
    }

    private boolean isAuthCodeValid(String authcode) {
        return (authcode!=null?true:false);
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

