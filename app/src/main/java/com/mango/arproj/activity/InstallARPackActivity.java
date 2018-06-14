package com.mango.arproj.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.mango.arproj.R;
import com.mango.arproj.entity.ARPackage;
import com.mango.arproj.entity.Room;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstallARPackActivity extends AppCompatActivity {

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
    private LatLng currentPosition = null;

    private ArrayList<Marker> arMarkers = new ArrayList<>();
    //用户相关
    private String token;
    private String uuid;
    private String roomUid;
    private String duration;

    private InstallARPackActivityBroadcastReceiver gameStartedReceiver;

    //监听云端分发AR信物的位置指令
    private class InstallARPackActivityBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ARutil.getActionGameStarted().compareTo(intent.getAction())==0){
                // TODO intent implemented
                // 跳转到比赛界面
                String msg = intent.getStringExtra("msg");
                Intent newIntent = new Intent(InstallARPackActivity.this,GamingActivity.class);
                newIntent.putExtra("msg",msg);
                newIntent.putExtra("token",token);
                newIntent.putExtra("uuid",uuid);
                newIntent.putExtra("roomUid",roomUid);
                newIntent.putExtra("duration",duration);
                newIntent.putExtra("isRoomCreator","true");
                startActivity(newIntent);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_arpack);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        roomUid = getIntent().getStringExtra("roomUid");

        //初始化按钮
        initButtons();

        initAmap(savedInstanceState);

        gameStartedReceiver = new InstallARPackActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ARutil.getActionGameStarted());
        registerReceiver(gameStartedReceiver,intentFilter);
    }

    private void initButtons(){
        //提交位置按钮
        final FancyButton submitBtn = findViewById(R.id.btn_install_arpack_submit_ar);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!InstallARPackActivity.this.arMarkers.isEmpty()){
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(InstallARPackActivity.this);
                    dialog.setTitle("即将提交AR信物位置");
                    dialog.setMessage("一旦提交就不可修改，是否确认");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestForARInstall(InstallARPackActivity.this.token,InstallARPackActivity.this.uuid,InstallARPackActivity.this.roomUid);
                        }
                    });
                    dialog.setNegativeButton("返回", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
                else{
                    Toast.makeText(InstallARPackActivity.this,"AR信物位置不可与当前位置重叠噢",Toast.LENGTH_LONG).show();
                }


            }
        });

        //游戏开始按钮
        FancyButton startBtn = findViewById(R.id.btn_install_arpack_start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!submitBtn.isEnabled()){
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(InstallARPackActivity.this);
                    final EditText editText = new EditText(InstallARPackActivity.this);
                    dialog.setView(editText);
                    dialog.setTitle("即将开始游戏啦");
                    dialog.setMessage("请输入比赛时间（min）并确认");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editText.setError(null);
                            if(!TextUtils.isEmpty(editText.getText().toString())){
                                InstallARPackActivity.this.duration = editText.getText().toString();
                                requestForGameStart(InstallARPackActivity.this.token,InstallARPackActivity.this.roomUid,editText.getText().toString());
                            }
                            else{
                                editText.setError("比赛时间不可为空噢");
                            }
                        }
                    });
                    dialog.setNegativeButton("返回", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
                else{
                    Toast.makeText(InstallARPackActivity.this,"请先提交AR信物的位置噢～",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void initAmap(Bundle savedInstanceState){
        //获取地图控件引用
        mMapView = findViewById(R.id.map_install_arpack);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(60000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        markerOption = new MarkerOptions().draggable(true);

        aMap.getUiSettings().setZoomControlsEnabled(false);

        //添加AR信物位置点
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                InstallARPackActivity.this.currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
                Log.d("InstallARPackActivity",String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));
                //暂时默认设置五个AR信物
                for(int i=0;i<5;i++){
                    final Marker marker = aMap.addMarker(
                    markerOption.position(currentPosition)
                            .title("AR信物"+i+"号")
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_appicon))));
                    //保存到类变量
                    InstallARPackActivity.this.arMarkers.add(marker);
                }
            }
        });

        //监听AR信物放置点的位置
        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                String title = marker.getTitle();
                //硬编码获取信物序号
                int index = Integer.valueOf(title.charAt(4));
                //更新AR信物点数组的信息
                InstallARPackActivity.this.arMarkers.set(index-1,marker);
            }
        });


    }

    //请求游戏开始
    private void requestForGameStart(final String token,final String roomUid,final String duration){

        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",token);
                data.put("roomUid",roomUid);
                data.put("duration",duration);

                String postBody = JSONEncodeFormatter.parser(10011, data);

                Log.d("postBody", postBody);

                //发起okhttp请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //设置请求URL
                        .url(ARutil.getRoomURL())
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
                    String code = msg.get("code");
                    if("0".compareTo(code)==0){
                        InstallARPackActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InstallARPackActivity.this,"游戏马上开始啦！",Toast.LENGTH_LONG).show();
                                // TODO Intent implemented here
                            }
                        });
                    }
                    else{

                        InstallARPackActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(InstallARPackActivity.this,"服务器正忙，请稍后重试噢",Toast.LENGTH_LONG).show();
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

    //请求设置AR信物位置
    private void requestForARInstall(final String token,final String uuid,final String roomUid){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid", uuid);
                data.put("roomUid",roomUid);

                //取出AR信物的信息并转换
                ArrayList<Marker> markers = InstallARPackActivity.this.arMarkers;
                ArrayList<Map<String,String>> markers_info = new ArrayList<>();
                Iterator<Marker> iterator = markers.iterator();
                while(iterator.hasNext()){
                    Marker marker = iterator.next();
                    ARPackage arPackage = new ARPackage();
                    arPackage.setLongitude(marker.getPosition().longitude);
                    arPackage.setLatitude(marker.getPosition().latitude);
                    arPackage.setContent("哈哈！被你发现了！");
                    markers_info.add(arPackage.toSecureHashMap());
                }

                String postBody = JSONEncodeFormatter.parser(10010, data, markers_info);

                Log.d("postBody", postBody);

                //发起okhttp请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //设置请求URL
                        .url(ARutil.getARPackURL())
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

                        InstallARPackActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InstallARPackActivity.this,"AR信物设置好啦！",Toast.LENGTH_LONG).show();
                                FancyButton fancyButton = InstallARPackActivity.this.findViewById(R.id.btn_install_arpack_submit_ar);
                                //不可二次提交
                                fancyButton.setDisableBackgroundColor(0x122121);
                                fancyButton.setEnabled(false);
                            }
                        });
                    }
                    else{

                        InstallARPackActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(InstallARPackActivity.this,"服务器正忙，请稍后重试噢",Toast.LENGTH_LONG).show();
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        unregisterReceiver(gameStartedReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


}
