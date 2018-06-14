package com.mango.arproj.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mango.arproj.R;

public class SettingActivity extends AppCompatActivity {

    private String[] titles = {"重置密码","更换绑定手机号","退出登录"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingActivity.this,android.R.layout.simple_list_item_1,titles);
        ListView listView = findViewById(R.id.listView_setting);
        listView.setAdapter(adapter);
    }
}
