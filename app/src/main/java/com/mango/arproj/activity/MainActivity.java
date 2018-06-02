package com.mango.arproj.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.ImageButton;

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
import com.mango.arproj.util.ClazzTransformer;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;


/**
 * 芒果君的备注
 * 1)10.0.2.2:8080是模拟器上映射电脑127.0.0.1:8080的代理地址。若设置请求地址为localhost，则模拟器只会请求模拟器本机的localhost，而不是电脑端的localhost
 */
public class MainActivity extends AppCompatActivity{

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
    private MapView mMapView;

    private BoomMenuButton boomMenuButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动地图控件
        this.initAmap(savedInstanceState);


        //设置桌面按钮

        this.initBtns();


    }

    private void initAmap(Bundle savedInstanceState){
        //获取地图控件引用
        mMapView = findViewById(R.id.map);
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

    private void initBtns(){

        //初始化综合菜单
        boomMenuButton = (BoomMenuButton)findViewById(R.id.btn_main_menu);
        boomMenuButton.setButtonEnum(ButtonEnum.SimpleCircle);

        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .normalImageRes(R.drawable.icon_user));

        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_setting));

        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_record));

        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_thumbup));

        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_stats));


        //初始化"组建队伍"和"加入队伍"按钮

        ImageButton createTeamBtn = findViewById(R.id.btn_main_createTeam);
        createTeamBtn.setImageResource(R.drawable.icon_createteam);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
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
