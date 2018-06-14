package com.mango.arproj.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.mango.arproj.R;
import com.mango.arproj.component.RankResultAdapter;
import com.mango.arproj.util.JSONDecodeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RankResultActivity extends AppCompatActivity {

    private String msg;
    private ArrayList<HashMap<String,String>> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_result);

        msg = getIntent().getStringExtra("msg");
        Log.d("RankActivity",msg);

        data = (ArrayList<HashMap<String, String>>) JSONDecodeFormatter.decodeDataArray(msg).get("data");

        RankResultAdapter rankResultAdapter = new RankResultAdapter(RankResultActivity.this,R.layout.list_item_rank_result,data);
        ListView listView = findViewById(R.id.listView_rank_result);
        listView.setAdapter(rankResultAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
