package com.mango.arproj.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.heinrichreimersoftware.materialdrawer.DrawerActivity;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerProfile;
import com.mango.arproj.R;
import com.mango.arproj.entity.User;
import com.mango.arproj.util.ARutil;
import com.mango.arproj.util.ClazzTransformer;

import com.mango.arproj.util.JSONDecodeFormatter;
import com.mango.arproj.util.JSONEncodeFormatter;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 芒果君的备注
 * 1)10.0.2.2:8080是模拟器上映射电脑127.0.0.1:8080的代理地址。若设置请求地址为localhost，则模拟器只会请求模拟器本机的localhost，而不是电脑端的localhost
 */
public class MainActivity extends DrawerActivity{

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

    private DrawerLayout drawer;

    private User currentUser;

    private String token;

    private String uuid;

    private Bitmap userIcon=null;

//    private IntentFilter intentFilter;
//
//    private CreateTeamBroadcastReceiver receiver;
//
//    class CreateTeamBroadcastReceiver extends android.content.BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(MainActivity.this,intent.getStringExtra("msg"),Toast.LENGTH_LONG).show();
//            Log.d("MainActivity+broadcast",intent.getStringExtra("msg"));
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //启动地图控件
        this.initAmap(savedInstanceState);


        //设置桌面按钮

        this.initBtns();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        initEmptyDrawer();

        SharedPreferences pref = getSharedPreferences(ARutil.getSharePreferencePath(),MODE_PRIVATE);
        token = pref.getString("token",null);
        if(token!=null){
            uuid = pref.getString("uuid",null);
            requestForUserProfile(token,uuid);
        }
        else{
            initEmptyDrawer();
        }

//        LatLng latLng1 = new LatLng(38.540103,76.978787);
//        LatLng latLng2 = new LatLng(39.90000, 116.407525);
//
//        float distance = AMapUtils.calculateLineDistance(latLng1,latLng2);
//        Log.d("distanceDemo = ",String.valueOf(distance));



    }



    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences(ARutil.getSharePreferencePath(),MODE_PRIVATE);
        token = pref.getString("token",null);
        if(token!=null){
            uuid = pref.getString("uuid",null);
            requestForUserProfile(token,uuid);
        }
        else{
            initEmptyDrawer();
        }
    }



    private void initUserProfileDrawer(User user){
        clearItems();
        clearProfiles();

        if(userIcon!=null){
            addProfile(
                    new DrawerProfile()
                            .setRoundedAvatar(MainActivity.this,userIcon)
                            .setBackground(getResources().getDrawable(R.color.default_bmb_dimColor))
                            .setName(user.getName())
                            .setDescription("一只脱离了高级趣味的程序猿")
            );
        }
        else{
            addProfile(
                    new DrawerProfile()
                            .setRoundedAvatar((BitmapDrawable)getResources().getDrawable(R.drawable.icon_appicon))
                            .setBackground(getResources().getDrawable(R.drawable.icon_appicon))
                            .setName(user.getName())
                            .setDescription("一只脱离了高级趣味的程序猿")
            );
        }


        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_phone))
                        .setTextPrimary("电话")
                        .setTextSecondary(user.getTel())
        );

        addDivider();

        String gender = (user.getGender()==1)?"纯爷们":"萌妹纸";
        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_gender))
                        .setTextPrimary("性别")
                        .setTextSecondary(gender)
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_weight))
                        .setTextPrimary("体重")
                        .setTextSecondary(String.valueOf(user.getWeight())+" kg")
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_height))
                        .setTextPrimary("身高")
                        .setTextSecondary(String.valueOf(user.getHeight())+"cm")
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_district))
                        .setTextPrimary("所在地区")
                        .setTextSecondary(user.getProvince()+" "+user.getCity())
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_step_count))
                        .setTextPrimary("步数总计")
                        .setTextSecondary(String.valueOf(user.getStepCount()))
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_thumbsup))
                        .setTextPrimary("点赞数")
                        .setTextSecondary(String.valueOf(user.getThumbsUpCount()))
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_arcount))
                        .setTextPrimary("AR数")
                        .setTextSecondary(String.valueOf(user.getArCount()))
        );

        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_joincount))
                        .setTextPrimary("参赛次数")
                        .setTextSecondary(String.valueOf(user.getJoinCount()))
        );


    }

    private void initEmptyDrawer(){
        clearItems();
        clearProfiles();

        addProfile(
                new DrawerProfile()
                        .setRoundedAvatar((BitmapDrawable)getResources().getDrawable(R.drawable.icon_appicon))
                        .setBackground(getResources().getDrawable(R.drawable.icon_appicon))
                        .setName("请登录")
                        .setOnProfileClickListener(new DrawerProfile.OnProfileClickListener() {
                            @Override
                            public void onClick(DrawerProfile drawerProfile, long l) {
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                closeDrawer();
                                startActivity(intent);
                            }
                        })
        );
        addItem(
                new DrawerItem()
                        .setImage(getResources().getDrawable(R.drawable.icon_phone))
                        .setTextPrimary("请使用手机号登录噢～")
        );
    }

    /**
     * 获取用户信息，包含头像
     * @param token
     */
    private void requestForUserProfile(final String token, final String uuid){
        final String _token = token;
        final String _uuid = uuid;

        //详细信息线程
        Thread infoThread = new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("token",_token);

                String postBody = JSONEncodeFormatter.parser(10015, data);


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
                    Log.d(TAG,re);
                    HashMap<String,Object> msg = JSONDecodeFormatter.decodeDataObject(re);
                    String code = (String) msg.get("code");
                    if("0".compareTo(code)==0){

                        HashMap<String,String> data_obj = (HashMap<String, String>) msg.get("data");
                        String uuid = data_obj.get("uuid");
                        String tel = data_obj.get("tel");
                        String name = data_obj.get("name");
                        String gender = data_obj.get("gender");
                        String age = data_obj.get("age");
                        String weight = data_obj.get("weight");
                        String height = data_obj.get("height");
                        String province = data_obj.get("province");
                        String city = data_obj.get("city");
                        String stepCount = data_obj.get("stepCount");
                        String arCount = data_obj.get("arCount");
                        String thumbsUpCount = data_obj.get("thumbsUpCount");
                        String joinCount = data_obj.get("joinCount");

                        MainActivity.this.currentUser = new User();
                        MainActivity.this.currentUser.setUuid(uuid);
                        MainActivity.this.currentUser.setTel(tel);
                        MainActivity.this.currentUser.setGender(Integer.valueOf(gender));
                        MainActivity.this.currentUser.setName(name);
                        MainActivity.this.currentUser.setAge(Integer.valueOf(age));
                        MainActivity.this.currentUser.setWeight(Float.valueOf(weight));
                        MainActivity.this.currentUser.setHeight(Float.valueOf(height));
                        MainActivity.this.currentUser.setProvince(province);
                        MainActivity.this.currentUser.setCity(city);
                        MainActivity.this.currentUser.setStepCount(Integer.valueOf(stepCount));
                        MainActivity.this.currentUser.setArCount(Integer.valueOf(arCount));
                        MainActivity.this.currentUser.setThumbsUpCount(Integer.valueOf(thumbsUpCount));
                        MainActivity.this.currentUser.setJoinCount(Integer.valueOf(joinCount));


                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_LONG).show();
                                initUserProfileDrawer(MainActivity.this.currentUser);
                            }
                        });
                    }
                    else{

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"登录过期，请重新登录",Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    //在主线程上更新本活动UI，不可在主线程上直接更新，将会造成闪退

                    Log.d("res", re);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        //获取头像线程
        Thread iconThread = new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://120.78.177.77/icon?code=10023&uuid="+_uuid+"&token="+_token)
                        .post(RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"),
                                ""))
                        .build();


                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    //获取返回的二进制字符流
                    byte[] bytes = response.body().bytes();
                    //将二进制字符流解码成位图
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Log.d(TAG+"photo",String.valueOf(bytes));
                    //在主线程上更新图片UI
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.userIcon = bitmap;
//                            initUserProfileDrawer(MainActivity.this.currentUser);
//                            notify();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        infoThread.start();

//        synchronized (this){
//            iconThread.start();
//        }


    }



    private void initAmap(Bundle savedInstanceState){
        //获取地图控件引用
        mMapView = findViewById(R.id.map_main);
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
                        if(MainActivity.this.token!=null){
                            openDrawer();
                        }
                        else {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .normalImageRes(R.drawable.icon_user));

        //进入设置
        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_setting)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        if(MainActivity.this.token!=null) {
                            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("uuid", uuid);
                            SharedPreferences pref = getSharedPreferences(ARutil.getSharePreferencePath(),MODE_PRIVATE);
                            String tel = pref.getString("tel",null);
                            intent.putExtra("tel",tel);
                            startActivity(intent);
                        }
                        else{
                            openDrawer();
                            Toast.makeText(MainActivity.this,"请先登录噢！",Toast.LENGTH_SHORT).show();
                        }
                    }
                }));



        //查看统计记录
        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_stats)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        if(MainActivity.this.token!=null) {
                            Intent intent = new Intent(MainActivity.this, RecordResultActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                        }
                        else{
                            openDrawer();
                            Toast.makeText(MainActivity.this,"请先登录噢！",Toast.LENGTH_SHORT).show();
                        }
                    }
                }));

        //创建队伍
        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_createteam)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        if(MainActivity.this.token!=null) {
                            Intent intent = new Intent(MainActivity.this, CreateTeamActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                        }
                        else{
                            openDrawer();
                            Toast.makeText(MainActivity.this,"请先登录噢！",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        );

        //加入队伍
        boomMenuButton.addBuilder(new SimpleCircleButton.Builder()
                .normalImageRes(R.drawable.icon_join)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        if(MainActivity.this.token!=null) {

                            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                            final EditText editText = new EditText(MainActivity.this);
                            dialog.setView(editText);
                            dialog.setTitle("输入6位数字邀请码");
                            dialog.setMessage("快去查看好友的界面上显示的邀请码哟");
                            dialog.setCancelable(true);
                            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                                        editText.setError(null);
                                        Intent intent = new Intent(MainActivity.this, JoinTeamActivity.class);
                                        intent.putExtra("token", token);
                                        intent.putExtra("uuid", uuid);
                                        intent.putExtra("joinCode", editText.getText().toString());
                                        startActivity(intent);
                                    } else {
                                        editText.setError("邀请码不可为空噢");
                                    }

                                }
                            });

                            dialog.show();
                        }
                        else{
                            openDrawer();
                            Toast.makeText(MainActivity.this,"请先登录噢！",Toast.LENGTH_SHORT).show();
                        }


                    }
                }));



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(receiver);
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
