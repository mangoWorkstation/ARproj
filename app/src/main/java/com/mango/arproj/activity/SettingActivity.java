package com.mango.arproj.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mango.arproj.R;
import com.mango.arproj.util.ARutil;

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==2){
                    SharedPreferences.Editor editor = getSharedPreferences(ARutil.getSharePreferencePath(),MODE_PRIVATE).edit();
                    editor.remove("token");
                    editor.apply();
                    Toast.makeText(SettingActivity.this,"您已退出登录",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

}
