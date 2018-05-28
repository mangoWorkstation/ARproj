package com.mango.arproj;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.mango.util.JSONDecodeFormatter;
import com.mango.util.JSONEncodeFormatter;
import com.mango.util.ResourceManager;
import com.mango.util.UploadUtil;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.taobao.accs.ACCSManager.mContext;


/**
 * 芒果君的备注
 * 1)10.0.2.2:8080是模拟器上映射电脑127.0.0.1:8080的代理地址。若设置请求地址为localhost，则模拟器只会请求模拟器本机的localhost，而不是电脑端的localhost
 */
public class MainActivity extends AppCompatActivity{

    private CloudPushService mPushService;

    private static final String IMAGE_UNSPECIFIED = "image/*";

    private final int IMAGE_CODE = 0;

    private Uri picURL;

    private final String baseUrl = "http://120.78.177.77/ar/api";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        MapView mMapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        AMap aMap = null;
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
    }

}
