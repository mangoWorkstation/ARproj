package com.mango.arproj.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.mango.arproj.R;
import com.mango.arproj.component.RankResultAdapter;
import com.mango.arproj.component.RecordResultAdapter;
import com.mango.arproj.entity.Record;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.Encryptor;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecordResultActivity extends AppCompatActivity {

    private String msg;
    private ArrayList<Record> records = new ArrayList<>();

    //用户相关
    private String token;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_result);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        requestForRecord(token,uuid);


    }

    private void requestForRecord(final String token,final String uuid){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid",uuid);

                String postBody = JSONEncodeFormatter.parser(10021, data);


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
                    HashMap<String, Object> res = JSONDecodeFormatter.decodeDataArray(re);
                    String code = (String) res.get("code");
                    ArrayList<HashMap<String,String>> data_arr = (ArrayList<HashMap<String, String>>) res.get("data");

                    if("0".compareTo(code)==0){

                        Iterator<HashMap<String,String>> data_arr_iterator = data_arr.iterator();
                        while(data_arr_iterator.hasNext()){
                            HashMap<String,String> temp = data_arr_iterator.next();
                            Record record = new Record();
                            record.setStart_t(temp.get("start_t"));
                            record.setEnd_t(temp.get("end_t"));
                            record.setArCount(Integer.valueOf(temp.get("arCount")));
                            record.setStepCount(Integer.valueOf(temp.get("stepCount")));
                            RecordResultActivity.this.records.add(record);
                        }

                        RecordResultActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecordResultAdapter recordResultAdapter = new RecordResultAdapter(RecordResultActivity.this,R.layout.list_item_record_result,records);
                                ListView listView = findViewById(R.id.listView_record_result);
                                listView.setAdapter(recordResultAdapter);
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
