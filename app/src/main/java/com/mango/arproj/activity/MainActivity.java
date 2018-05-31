package com.mango.arproj.activity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.mango.arproj.R;
import com.mango.arproj.util.ARurl;
import com.mango.arproj.util.ClazzTransformer;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.util.List;


/**
 * 芒果君的备注
 * 1)10.0.2.2:8080是模拟器上映射电脑127.0.0.1:8080的代理地址。若设置请求地址为localhost，则模拟器只会请求模拟器本机的localhost，而不是电脑端的localhost
 */
public class MainActivity extends AppCompatActivity implements GeoFenceListener, AMap.OnMapClickListener, LocationSource, AMapLocationListener {

    //TAG
    private String TAG = ClazzTransformer.getClazzTAG(this);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        MapView mMapView = findViewById(R.id.map);
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
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

//        BoomMenuButton boomMenuButton = (BoomMenuButton)findViewById(R.id.bmb);
//        boomMenuButton.setButtonEnum(ButtonEnum.SimpleCircle);
//        boomMenuButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_3_1);
//
//        for (int i = 0; i < boomMenuButton.getButtonPlaceEnum().buttonNumber(); i++) {
//            boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
//                    .normalImageRes(R.drawable.ic_launcher_background));
//        }
//        boomMenuButton.boomImmediately();
        markerOption = new MarkerOptions().draggable(true);

    }


    /**
     * implemented from AMapLocationListener
     */

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                //     tvResult.setVisibility(View.GONE);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": "
                        + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                //   tvResult.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,errText,Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * implemented from LocationSource
     */

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mlocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            // 只是为了获取当前位置，所以设置为单次定位
            mLocationOption.setOnceLocation(true);
            // 设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            mlocationClient.startLocation();

        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void addCenterMarker(LatLng latlng) {
        if (null == centerMarker) {
            centerMarker = aMap.addMarker(markerOption);
        }
        centerMarker.setPosition(latlng);
    }


    /**
     * implemented from GeoFenceListener
     */

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
