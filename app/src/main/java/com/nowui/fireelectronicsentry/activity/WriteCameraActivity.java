package com.nowui.fireelectronicsentry.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

public class WriteCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private Camera camera;
    private Camera.Parameters parameters = null;
    private Button photoButton;
    private Button locationButton;
    private String code;
    public LocationClient mLocationClient;
    private String latitude = "";
    private String longitude = "";

    @Override
    protected void onResume() {
        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_write_camera);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(setting.getString(Helper.KeyDepartmentName, getResources().getString(R.string.app_name)));
        setSupportActionBar(toolbar);

        String usetType = setting.getString(Helper.KeyUserType, "");
        String departmentName = setting.getString(Helper.KeyDepartmentName, "");
        if (usetType.equals("1")) {
            toolbar.setTitle("防火监督员");
        } else if(usetType.equals("2")) {
            toolbar.setTitle("企业管理员-" + Helper.substring(departmentName, 6));
        }

        String weather = setting.getString(Helper.KeyWeather, "");
        TextView weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        weatherTextView.setText(weather);

        code = getIntent().getStringExtra("code");

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);

        locationButton = (Button) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocation();
            }
        });

        photoButton = (Button) findViewById(R.id.photoButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.takePicture(null, null, new MyPictureCallback());
                }
            }
        });

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        initLocation();

        startLocation();
    }

    public void startLocation() {
        showLoadingDialog();

        mLocationClient.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果出现异常，释放相机资源并置空
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
// 如果相机资源并不为空
        if(camera != null) {

            // 获得相机参数对象
            Camera.Parameters parameters = camera.getParameters();
            // 获取最合适的参数，为了做到拍摄的时候所见即所得，我让previewSize和pictureSize相等
            Camera.Size previewSize = Helper.getOptimalPictureSize(parameters.getSupportedPictureSizes(), 640, 480);
            Camera.Size pictureSize = Helper.getOptimalPictureSize(parameters.getSupportedPictureSizes(), 640, 480);
            System.out.println("---------------------------------------------------------------");
            System.out.println("previewSize: " + previewSize.width + ", " + previewSize.height);
            System.out.println("pictureSize: " + pictureSize.width + ", " + pictureSize.height);
            // 设置照片格式
            parameters.setPictureFormat(PixelFormat.JPEG);
            // 设置预览大小
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            // 设置自动对焦，先进行判断
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            // 设置图片保存时候的分辨率大小
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            // 给相机对象设置刚才设置的参数
            camera.setParameters(parameters);
            // 开始预览
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera != null) {
            // 停止预览
            //camera.stopPreview();
            // 释放相机资源并置空
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Helper.saveToSDCard(data, 90, "rfid");

                camera.stopPreview();
                camera.release();

                showLoadingDialog();

                Map<String, Object> jsonMap = JSON.parseObject(code, new TypeReference<Map<String, Object>>() {});

                System.out.println(code);

                System.out.println(Integer.valueOf(jsonMap.get("deptPatrolID").toString()));
                System.out.println(Integer.valueOf(jsonMap.get("DeptID").toString()));
                System.out.println(Integer.valueOf(jsonMap.get("PatrolItemID").toString()));

                String url = Helper.WebUrl + "/PatrolItemImage.ashx";
                if (Helper.isAdmin) {
                    url = Helper.AdminWebUrl + "/PatrolItemImage.ashx";
                }

                JSONObject jsonObject = new JSONObject();
                String datetime = Helper.formatDateTime();
                jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
                jsonObject.put("datetime", datetime);
                JSONObject dataObject = new JSONObject();
                dataObject.put("deptPatrolID", Integer.valueOf(jsonMap.get("deptPatrolID").toString()));
                dataObject.put("deptID", Integer.valueOf(jsonMap.get("DeptID").toString()));
                dataObject.put("patrolItemID", Integer.valueOf(jsonMap.get("PatrolItemID").toString()));
                dataObject.put("latitude", latitude);
                dataObject.put("longitude", longitude);
                dataObject.put("patrolImageBase64", Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/rfid.jpg"));
                jsonObject.put("data", dataObject);

                StringEntity stringEntity = null;
                try {
                    stringEntity = new StringEntity(jsonObject.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                System.out.println(url);
                System.out.println(jsonObject.toString());

                client.post(WriteCameraActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println(new String(responseBody));

                        materialDialog.hide();

                        Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                        });

                        if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                            Intent intent = new Intent();
                            setResult(Helper.CodeResult, intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        materialDialog.hide();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showLoadingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        materialDialog.dismiss();

        mLocationClient.stop();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");// 位置语义化信息
            sb.append(location.getLocationDescribe());
            List<Poi> list = location.getPoiList();// POI信息
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            System.out.println("BaiduLocationApiDem:" + sb.toString());

            latitude = location.getLatitude() + "";
            longitude = location.getLongitude() + "";

            mLocationClient.stop();

            materialDialog.hide();

            new MaterialDialog.Builder(WriteCameraActivity.this)
                    .content(sb.toString())
                    .positiveText("确定")
                    .positiveColor(Color.WHITE)
                    .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                    .show();
        }
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(false);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

}
