package com.mango.arproj.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.mango.arproj.R;
import com.mango.arproj.activity.ar.ARCameraActivity;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import cn.iwgang.countdownview.CountdownView;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GamingActivity extends AppCompatActivity {

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
    private int currentARfound = 0;
    private boolean isRoomCreator;
    private Marker nearestMarker;
    private GamingActivityBroadcastReceiver gameOverReceiver;

    private class GamingActivityBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            if(ARutil.getActionGameOver().compareTo(intent.getAction())==0){
                //TODO 跳转到排行榜页面
                Log.d("rank result",intent.getStringExtra("msg"));

                Intent newIntent = new Intent(GamingActivity.this,RankResultActivity.class);
                newIntent.putExtra("msg",intent.getStringExtra("msg"));

                startActivity(newIntent);

                finish();

            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        initAmap(savedInstanceState);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        roomUid = getIntent().getStringExtra("roomUid");

        gameOverReceiver = new GamingActivityBroadcastReceiver();
        IntentFilter intentFilter_gameover = new IntentFilter(ARutil.getActionGameOver());
        registerReceiver(gameOverReceiver,intentFilter_gameover);

        isRoomCreator = "true".compareTo(getIntent().getStringExtra("isRoomCreator"))==0?true:false;

        //初始化MARKERS
        HashMap<String,Object> res = JSONDecodeFormatter.decodeDataArray(getIntent().getStringExtra("msg"));
        duration = (String) res.get("duration");
        ArrayList<HashMap<String,String>> data = (ArrayList<HashMap<String, String>>) res.get("data");
        final Iterator<HashMap<String,String>> iterator = data.iterator();
        int index = 0;
        while(iterator.hasNext()){
            HashMap<String,String> temp = iterator.next();
            Log.d("GamingActivity",temp.toString());
            final Marker marker = aMap.addMarker(
                    markerOption
                            .position(new LatLng(Double.valueOf(temp.get("latitude")),Double.valueOf(temp.get("longitude"))))
                            .title("AR信物"+index+"号")
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_appicon))));
            index++;
            arMarkers.add(marker);
        }

        CountdownView mCvCountdownView = (CountdownView)findViewById(R.id.countdown_gaming);
        mCvCountdownView.start(Long.valueOf(duration)*60000);
        //倒计时到时，触发房主请求结束游戏
        mCvCountdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                if(isRoomCreator){
                    GamingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            requestForGameOverWhenTimeUp(token,uuid,roomUid);
                        }
                    });
                }
                else{
                    Toast.makeText(GamingActivity.this,"时间到！游戏自动结束！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        FancyButton openCameraBtn = findViewById(R.id.btn_gaming_open_camera);
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GamingActivity.this, ARCameraActivity.class);
//                startActivityForResult(intent,RESULT_OK);
//                removeMarker(nearestMarker);
                GamingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!GamingActivity.this.arMarkers.isEmpty()) {
                            // TODO 上报完成进度
                            removeMarker(nearestMarker);
                            requestForUpdate(token, uuid, roomUid);
                        }
                        else{
                            requestForMemberFinished(token,uuid,roomUid);
                        }
                    }
                });
                startActivity(intent);
            }
        });
        openCameraBtn.setEnabled(false);

        //设置监听器,多次轮训计算五个AR信物点与当前位置的直线距离，当存在距离delta(l)<20米时，允许点击打开按钮
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                Log.d("locationChange",location.toString());
                LatLng currentPoint = new LatLng(location.getLatitude(),location.getLongitude());


                Iterator<Marker> arIterator = GamingActivity.this.arMarkers.iterator();
                while(arIterator.hasNext()){
                    final Marker marker = arIterator.next();
                    LatLng markerPoint = marker.getPosition();
                    float distance = AMapUtils.calculateLineDistance(currentPoint,markerPoint);
                    Log.d("distance ",String.valueOf(distance));

                    //暂定判别距离为20米
                    if(distance<20){
                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nearestMarker = marker;
//                                removeMarker(nearestMarker);

                                FancyButton openCameraBtn = findViewById(R.id.btn_gaming_open_camera);
                                openCameraBtn.setEnabled(true);
                                if(!GamingActivity.this.arMarkers.isEmpty()) {
                                    Toast.makeText(GamingActivity.this, "哇哦！你已经到达AR信物的附近咯！\n打开相机吧！", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                        break;
                    }
                }

            }
        });

    }

    private void removeMarker(Marker oldmarker){
        //清理附近已打开的点，从地图上移除
        GamingActivity.this.arMarkers.remove(oldmarker);
        aMap.clear();
        nearestMarker = null;

        Iterator<Marker> markerIterator = GamingActivity.this.arMarkers.iterator();
        //重新加载markers
        while(markerIterator.hasNext()){
            Marker temp = markerIterator.next();
            Log.d("GamingActivity",temp.toString());
            final Marker marker = aMap.addMarker(
                    markerOption
                            .position(new LatLng(temp.getPosition().latitude,temp.getPosition().longitude))
                            .title("AR信物")
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_appicon))));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gameOverReceiver);
    }

    private void initAmap(Bundle savedInstanceState){
        //获取地图控件引用
        mMapView = findViewById(R.id.map_gaming);
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
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        markerOption = new MarkerOptions().draggable(true);

        aMap.getUiSettings().setZoomControlsEnabled(false);

    }

    private void requestForUpdate(final String token,final String uuid,final String roomUid){
        this.currentARfound++;
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String,String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid",uuid);
                data.put("roomUid",roomUid);
                data.put("percent",String.valueOf(Float.valueOf(currentARfound)*1.0/5.0));
                data.put("arCount",String.valueOf(currentARfound));
                // TODO 步数暂定为1，计步器后期解决
                data.put("stepCount","1");

                String postBody = JSONEncodeFormatter.parser(10012, data);

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

                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GamingActivity.this,"您已找到一个AR信物！上报成功",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        // TODO 测试用，后期删掉
                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(GamingActivity.this,"服务器正忙，上报失败噢！",Toast.LENGTH_LONG).show();
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

    private void requestForMemberFinished(final String token,final String uuid,final String roomUid){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String,String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid",uuid);
                data.put("roomUid",roomUid);
                data.put("arCount",String.valueOf(currentARfound));
                // TODO 步数暂定为1，计步器后期解决
                data.put("stepCount","1");
                data.put("end_t",String.valueOf(System.currentTimeMillis()/1000));

                String postBody = JSONEncodeFormatter.parser(10013, data);

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
                Response response;
                try {
                    response = client.newCall(request).execute();
                    //获取response中的字符
                    final String re = response.body().string();
                    HashMap<String,String> msg = JSONDecodeFormatter.decodeSimpleMsg(re);
                    String code = (String) msg.get("code");
                    if("0".compareTo(code)==0){

                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GamingActivity.this,"您已经完成了所有任务，请等待其他队员完成",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        // TODO 测试用，后期删掉
                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(GamingActivity.this,"服务器正忙，上报失败噢！",Toast.LENGTH_LONG).show();
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

    private void requestForGameOverWhenTimeUp(final String token,final String uuid,final String roomUid){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String,String> data = new HashMap<>();
                data.put("token",token);
                data.put("uuid",uuid);
                data.put("roomUid",roomUid);

                String postBody = JSONEncodeFormatter.parser(10014, data);

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

                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GamingActivity.this,"时间到，游戏自动结束！",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        // TODO 测试用，后期删掉
                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //使用json格式化解码器处理短回复
                                Toast.makeText(GamingActivity.this,"服务器正忙，上报失败噢！",Toast.LENGTH_LONG).show();
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
