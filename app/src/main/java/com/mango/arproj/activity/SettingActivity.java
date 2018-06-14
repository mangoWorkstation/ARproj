package com.mango.arproj.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;

public class SettingActivity extends AppCompatActivity {

    private String[] titles = {"重置密码","更换绑定手机号","退出登录"};

    //用户相关

    private String token;
    private String uuid;
    private String tel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        tel = getIntent().getStringExtra("tel");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingActivity.this,android.R.layout.simple_list_item_1,titles);
        ListView listView = findViewById(R.id.listView_setting);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(i==2){
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
                    Toast.makeText(SettingActivity.this,"您已退出登录",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Intent intent = new Intent(SettingActivity.this,RequestAuthCodeActivity.class);
                    intent.putExtra("token",token);
                    intent.putExtra("uuid",uuid);
                    intent.putExtra("tel",tel);
                    if(i==0){
                        intent.putExtra("destination","resetPwd");
                    }
                    else{
                        intent.putExtra("destination","changeTel");
                    }
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

}
