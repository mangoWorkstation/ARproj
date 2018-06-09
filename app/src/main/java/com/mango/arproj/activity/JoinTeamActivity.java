package com.mango.arproj.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.mango.arproj.R;
import com.mango.arproj.entity.Room;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JoinTeamActivity extends AppCompatActivity {
    //高德相关
    private AMapLocationClient mlocationClient;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;
    private MarkerOptions markerOption = null;
    AMap aMap = null;
    // 中心点坐标
    private LatLng centerLatLng = null;
    // 中心点marker
    private Marker centerMarker;
    private MapView mMapView;

    //用户相关
    private String token;
    private String uuid;
    private String joinCode;

    //房间相关

    private Room currentRoom;

    private ArrayList<Integer> icons = new ArrayList<>();

    private ArrayList<String> names  = new ArrayList<>();

    private ArrayList<HashMap<String, Object>> userProfiles = new ArrayList<>();

    private SimpleAdapter gridAdapter;

    //监听成员动态变化
    private JoinTeamBroadcastReceiver updateMemberReceiver;

    //监听房主注销房间
    private JoinTeamBroadcastReceiver roomDismissReceiver;


    private GridView gridView;

    class JoinTeamBroadcastReceiver extends android.content.BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //成员发生变化时
            if(ARutil.getActionNewMemberJoinIn().compareTo(intent.getAction())==0){
                Log.d("JoinTeamActivity",intent.getStringExtra("msg"));
                HashMap<String,Object> res = JSONDecodeFormatter.decodeDataArray(intent.getStringExtra("msg"));
                ArrayList<HashMap<String,String>> data = (ArrayList<HashMap<String, String>>) res.get("data");

                JoinTeamActivity.this.names.clear();
                JoinTeamActivity.this.icons.clear();
                JoinTeamActivity.this.userProfiles.clear();

                for(int i=0;i<data.size();i++){
                    JoinTeamActivity.this.icons.add(R.drawable.icon_appicon);
                    JoinTeamActivity.this.names.add(data.get(i).get("name"));
                }

                for(int i = 0;i<icons.size();i++){
                    HashMap<String, Object> hash = new HashMap<String, Object>();
                    hash.put("image",icons.get(i));
                    hash.put("text", names.get(i));
                    userProfiles.add(hash);
                }


                JoinTeamActivity.this.refreshGridView(userProfiles);
            }

            //房主解散房间时
            if(ARutil.getActionDismissRoom().compareTo(intent.getAction())==0){
                Toast.makeText(JoinTeamActivity.this,"房主解散了房间",Toast.LENGTH_LONG).show();
                finish();
            }



        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_team);

        currentRoom = new Room();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        joinCode = getIntent().getStringExtra("joinCode");


        initAmap(savedInstanceState);

        initEmptyGridView();

        requestForJoinIn(token,uuid,joinCode);


        //设置监听器：成员发生变化
        IntentFilter intentFilter_new_member_join = new IntentFilter();
        intentFilter_new_member_join.addAction(ARutil.getActionNewMemberJoinIn());

        updateMemberReceiver = new JoinTeamBroadcastReceiver();
        registerReceiver(updateMemberReceiver,intentFilter_new_member_join);

        //设置监听器：房间被房主解散
        IntentFilter intentFilter_room_dismiss = new IntentFilter();
        intentFilter_room_dismiss.addAction(ARutil.getActionDismissRoom());

        roomDismissReceiver = new JoinTeamBroadcastReceiver();
        registerReceiver(roomDismissReceiver,intentFilter_room_dismiss);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateMemberReceiver);
    }

    //初始化gridview
    private void initEmptyGridView(){
        gridView = findViewById(R.id.gridview_join_team);

        ArrayList<HashMap<String, Object>> userProfiles = new ArrayList<HashMap<String, Object>>();

        icons.add(R.drawable.icon_appicon);
        icons.add(R.drawable.icon_add_member);

        names.add("我");
        names.add(" ");


        for(int i = 0;i<icons.size();i++){
            HashMap<String, Object> hash = new HashMap<String, Object>();
            hash.put("image",icons.get(i));
            hash.put("text", names.get(i));
            userProfiles.add(hash);
        }
        String[] form = {"image","text"};
        int[] to =new int [2];
        to[0]=R.id.image_member_layout_icon;
        to[1]=R.id.text_member_layout_name;
        gridAdapter = new SimpleAdapter(JoinTeamActivity.this, userProfiles, R.layout.menber_icon_name, form, to);
        gridView.setAdapter(gridAdapter);
    }

    private void refreshGridView(ArrayList<HashMap<String,Object>> _userProfiles){
        this.gridView = findViewById(R.id.gridview_join_team);

        String[] form = {"image","text"};
        int[] to =new int [2];
        to[0]=R.id.image_member_layout_icon;
        to[1]=R.id.text_member_layout_name;
        gridAdapter = new SimpleAdapter(JoinTeamActivity.this, _userProfiles, R.layout.menber_icon_name, form, to);
        gridView.setAdapter(gridAdapter);
    }


    //请求加入队伍
    private void requestForJoinIn(final String token, final String uuid,final String joinCode){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid", uuid);
                data.put("joinCode",joinCode);

                String postBody = JSONEncodeFormatter.parser(10009, data);


                Log.d("postBody", postBody);

                //发起okhttp请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //设置请求URL
                        .url(ARutil.getTeamPoolURL())
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
                    HashMap<String,Object> msg = JSONDecodeFormatter.decodeDataArray(re);
                    String code = (String) msg.get("code");
                    if("0".compareTo(code)==0){
                        JoinTeamActivity.this.currentRoom.setUid((String) msg.get("uid"));
                        ArrayList<HashMap<String,String>> data_arr = (ArrayList<HashMap<String, String>>) msg.get("data");

                        for(int i=0;i<data_arr.size();i++){
                            JoinTeamActivity.this.icons.add(R.drawable.icon_appicon);
                            JoinTeamActivity.this.names.add(data_arr.get(i).get("name"));
                        }

                        for(int i = 0;i<icons.size();i++){
                            HashMap<String, Object> hash = new HashMap<String, Object>();
                            hash.put("image",icons.get(i));
                            hash.put("text", names.get(i));
                            userProfiles.add(hash);
                        }



                        JoinTeamActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JoinTeamActivity.this.refreshGridView(userProfiles);
//                                JoinTeamActivity.this.gridAdapter.notifyDataSetChanged();
                                TextView joinCodeView = JoinTeamActivity.this.findViewById(R.id.textview_join_team_joincode);
                                joinCodeView.setText("邀请码："+joinCode+"\n正在等待房主开始游戏...");
                                joinCodeView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                Toast.makeText(JoinTeamActivity.this,"加入队伍成功，告诉朋友们来参加吧",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{

                        JoinTeamActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(JoinTeamActivity.this,"邀请码不存在噢",Toast.LENGTH_LONG).show();
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

    private void initAmap(Bundle savedInstanceState){
        //获取地图控件引用
        mMapView = findViewById(R.id.map_join_team);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        markerOption = new MarkerOptions().draggable(true);

        aMap.getUiSettings().setZoomControlsEnabled(false);
    }

    /**
     * 请求退出房间
     */
    @Override
    public void onBackPressed() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid", uuid);
                data.put("roomUid",currentRoom.getUid());

                String postBody = JSONEncodeFormatter.parser(10016, data);


                Log.d("postBody", postBody);

                //发起okhttp请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //设置请求URL
                        .url(ARutil.getTeamPoolURL())
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
                    HashMap<String,String> msg = JSONDecodeFormatter.decodeSimpleMsg(re);
                    String code = (String) msg.get("code");
                    if("0".compareTo(code)==0){

                        JoinTeamActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JoinTeamActivity.this,"您已退出房间",Toast.LENGTH_LONG).show();
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

        super.onBackPressed();

    }
}
