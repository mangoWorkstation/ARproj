package com.mango.arproj.activity;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.mango.arproj.R;
import com.mango.arproj.util.JSONDecodeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        initAmap(savedInstanceState);

        token = getIntent().getStringExtra("token");
        uuid = getIntent().getStringExtra("uuid");
        roomUid = getIntent().getStringExtra("roomUid");

        //初始化
        HashMap<String,Object> res = JSONDecodeFormatter.decodeDataArray(getIntent().getStringExtra("msg"));
        ArrayList<HashMap<String,String>> data = (ArrayList<HashMap<String, String>>) res.get("data");
        Iterator<HashMap<String,String>> iterator = data.iterator();
        int index = 0;
        while(iterator.hasNext()){
            HashMap<String,String> temp = iterator.next();
            Log.d("GamingActivity",temp.toString());
            final Marker marker = aMap.addMarker(
                    markerOption
                            .position(new LatLng(Double.valueOf(temp.get("latitude")),Double.valueOf(temp.get("longitude"))))
                            .title("AR信物"+index+"号")
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_appicon))));
            this.arMarkers.add(marker);
            index++;
        }

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
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        markerOption = new MarkerOptions().draggable(true);

        aMap.getUiSettings().setZoomControlsEnabled(false);
    }

}
