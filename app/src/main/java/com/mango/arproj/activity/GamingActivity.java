package com.mango.arproj.activity;

import android.content.Intent;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.mango.arproj.R;
import com.mango.arproj.activity.ar.ARCameraActivity;
import com.mango.arproj.util.JSONDecodeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.iwgang.countdownview.CountdownView;
import mehdi.sakout.fancybuttons.FancyButton;

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
    private DistanceSearch distanceSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        initAmap(savedInstanceState);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        roomUid = getIntent().getStringExtra("roomUid");

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
        mCvCountdownView.start(Long.valueOf(Long.valueOf(duration)*60000));

        FancyButton openCameraBtn = findViewById(R.id.btn_gaming_open_camera);
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GamingActivity.this, ARCameraActivity.class);
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
                    Log.d("marker","latitude = "+String.valueOf(marker.getPosition().latitude)+" longitude = "+String.valueOf(marker.getPosition().longitude));
                    LatLng markerPoint = marker.getPosition();
                    float distance = AMapUtils.calculateLineDistance(currentPoint,markerPoint);
                    Log.d("distance ",String.valueOf(distance));
                    if(distance<100){
                        GamingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GamingActivity.this.arMarkers.remove(marker);
                                aMap.clear();

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

                                FancyButton openCameraBtn = findViewById(R.id.btn_gaming_open_camera);
                                openCameraBtn.setEnabled(true);
                                Toast.makeText(GamingActivity.this,"哇哦！你已经到达AR信物的附近咯！\n打开相机吧！",Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }

            }
        });


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

}
