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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.ClazzTransformer;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserProfileImplementationActivity extends AppCompatActivity {

    //views
    private EditText nicknameEdit;
    private RadioGroup radioGroup;
    private RadioButton radioBtn_female;
    private RadioButton radioBtn_male;
    private EditText ageEdit;
    private EditText weightEdit;
    private EditText heightEdit;
    private EditText provinceEdit;
    private EditText cityEdit;
    private Button submitBtn;


    private String token=null;

    private int gender=0;

    private String TAG = ClazzTransformer.getClazzTAG(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_implementation);

        //init buttons.
        nicknameEdit = findViewById(R.id.edit_userprofilepreset_nickname);
        radioGroup = findViewById(R.id.ratiogroup_userprofilepreset);
        radioBtn_male = findViewById(R.id.ratioBtn_userprofilepreset_male);
        radioBtn_female = findViewById(R.id.ratioBtn_userprofilepreset_female);
        ageEdit = findViewById(R.id.edit_userprofilepreset_age);
        weightEdit = findViewById(R.id.edit_userprofilepreset_weight);
        heightEdit = findViewById(R.id.edit_userprofilepreset_height);
        provinceEdit = findViewById(R.id.edit_userprofilepreset_province);
        cityEdit = findViewById(R.id.edit_userprofilepreset_city);
        submitBtn = findViewById(R.id.btn_userprofilepreset_submit);


        token = getIntent().getStringExtra("token");

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //获取变更后的选中项的ID
                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                gender = (radioButtonId==UserProfileImplementationActivity.this.radioBtn_male.getId())?1:0;
            }
        });
        radioGroup.check(R.id.ratioBtn_userprofilepreset_female);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nicknameEdit.setError(null);
                ageEdit.setError(null);
                weightEdit.setError(null);
                heightEdit.setError(null);
                provinceEdit.setError(null);
                cityEdit.setError(null);

                final String nickname = nicknameEdit.getText().toString();
                final String age = ageEdit.getText().toString();
                final String weight = weightEdit.getText().toString();
                final String height = heightEdit.getText().toString();
                final String province = provinceEdit.getText().toString();
                final String city = cityEdit.getText().toString();


                //检查昵称，不少于3个字
                if(TextUtils.isEmpty(nickname)||nickname.length()<3){
                    nicknameEdit.setError("需要大于3个字的昵称噢");
                    return;
                }


                //检查年龄，0<age<70
                if(TextUtils.isEmpty(age)||Integer.valueOf(age)<0||Integer.valueOf(age)>70){
                    ageEdit.setError("年龄不符合标准噢");
                    return;
                }

                //检查体重，0<weight<100 kg
                if(TextUtils.isEmpty(weight)||Float.valueOf(weight)<0||Float.valueOf(weight)>100){
                    weightEdit.setError("体重要小于100kg噢");
                    return;
                }

                //检查省份、城市
                if(TextUtils.isEmpty(province)){
                    provinceEdit.setError("所在省份不能为空噢");
                    if(TextUtils.isEmpty(city)){
                        cityEdit.setError("所在城市不能为空噢");
                        return;
                    }
                    return;

                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("token",token);
                        data.put("name",nickname);
                        data.put("gender",String.valueOf(gender));
                        data.put("age",age);
                        data.put("weight",weight);
                        data.put("height",height);
                        data.put("province",province);
                        data.put("city",city);

                        String postBody = JSONEncodeFormatter.parser(10004, data);

                        Log.d(TAG+":postBody", postBody);

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
                            HashMap<String, String> res = JSONDecodeFormatter.decodeSimpleMsg(re);
                            String code = res.get("code");

                            if("0".compareTo(code)==0){

                                UserProfileImplementationActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UserProfileImplementationActivity.this,"设置成功，快去登录吧",Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            }
                            else if("90006".compareTo(code)==0){
                                UserProfileImplementationActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UserProfileImplementationActivity.this,"有不合格的字段哟",Toast.LENGTH_LONG).show();
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
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(UserProfileImplementationActivity.this);
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
