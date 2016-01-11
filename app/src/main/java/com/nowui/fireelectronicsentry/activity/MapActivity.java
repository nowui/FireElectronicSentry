package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Patrol;
import com.nowui.fireelectronicsentry.utility.Helper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private MapView mMapView;
    BaiduMap mBaiduMap;
    public LocationClient mLocationClient;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private MaterialDialog materialDialog;
    private ListView listView;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    MaterialCalendarView calendarView;
    private Button dateButton;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String selectDay;
    private InfoWindow mInfoWindow;
    private boolean isLoacation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

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

        mMapView = (MapView) findViewById(R.id.bmapView);

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams mMapViewLayoutParams = (RelativeLayout.LayoutParams) mMapView.getLayoutParams();
            mMapViewLayoutParams.height = Helper.formatPix(this, 700);
            mMapView.setLayoutParams(mMapViewLayoutParams);
        }

        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(24.0f);
        mBaiduMap.setMapStatus(msu);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Map<String, Object> map = list.get(marker.getZIndex());

                Button button = new Button(getApplicationContext());
                button.setText(map.get("PatrolName").toString());
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mBaiduMap.hideInfoWindow();
                    }
                });

                LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);

                return true;
            }
        });

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        selectDay = sdf.format(calendar.getTime());

        dateButton = (Button) findViewById(R.id.dateButton);
        dateButton.setText(selectDay);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarView.getVisibility() == View.VISIBLE) {
                    calendarView.setVisibility(View.INVISIBLE);
                } else {
                    calendarView.setVisibility(View.VISIBLE);
                }
            }
        });

        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);

        calendarView.setSelectedDate(calendar.getTime());
        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                calendarView.setVisibility(View.INVISIBLE);

                selectDay = sdf.format(calendarDay.getDate());

                dateButton.setText(selectDay);

                load();
            }
        });

        load();

        initLocation();
        //mLocationClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLocationClient.stop();

        mBaiduMap.setMyLocationEnabled(false);

        mMapView.onDestroy();
        mMapView = null;
    }
    @Override
    protected void onResume() {
        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        super.onResume();

        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLocationClient.stop();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null)
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

            mLocationClient.stop();

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

            /*BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
            OverlayOptions ooA = new MarkerOptions().position(ll).icon(bdA).zIndex(9).draggable(true);

            mBaiduMap.addOverlay(ooA);*/
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

    private void showLoadingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    private void load() {
        showLoadingDialog();

        String url = Helper.WebUrl + "/PatrolPathBaiduMapService.ashx";

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);

        //String userId = setting.getString(Helper.KeyUserId, "");
        String deptID = setting.getString(Helper.KeyDepartmentId, "");

        String usetType = setting.getString(Helper.KeyUserType, "");
        if (usetType.equals("1")) {
            deptID  = setting.getString(Helper.KeyChoiceDepartmentId, "");
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptID", Integer.valueOf(deptID));
        dataObject.put("startDatetime", selectDay + " 00:00:00");
        dataObject.put("endDatetime", selectDay + " 23:59:59");
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("++++++++++++++++");
        System.out.println(jsonObject.toString());

        client.post(MapActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                    list.clear();
                    mBaiduMap.clear();

                    double minLatitude = 0;
                    double maxLatitude = 0;
                    double minLongitude = 0;
                    double maxLongitude = 0;

                    int count = 0;

                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("baiduMapPatrolPath");
                    for (Map<String, Object> map : dataList) {
                        map.put("name", map.get("PatrolName"));
                        list.add(map);

                        if (Helper.isNullOrEmpty(map.get("Latitude")) || Helper.isNullOrEmpty(map.get("Longitude"))) {

                        } else {
                            String latitudeString = map.get("Latitude").toString();
                            String longitudeString = map.get("Longitude").toString();

                            double latitude = Double.valueOf(latitudeString);
                            double longitude = Double.valueOf(longitudeString);

                            if (count == 0) {
                                minLatitude = latitude;
                                maxLatitude = latitude;
                                minLongitude = longitude;
                                maxLongitude = longitude;
                            }

                            if (latitude < minLatitude) {
                                minLatitude = latitude;
                            }

                            if (latitude > maxLatitude) {
                                maxLatitude = latitude;
                            }

                            if (longitude < minLongitude) {
                                minLongitude = longitude;
                            }

                            if (longitude > maxLongitude) {
                                maxLongitude = longitude;
                            }

                            count++;
                        }
                    }

                    if (minLongitude == 0 && maxLongitude == 0 && minLongitude == 0 && maxLongitude == 0) {
                        if(! isLoacation) {
                            isLoacation = true;

                            mLocationClient.start();
                        }
                    } else {
                        LatLng southwest = new LatLng(minLatitude, minLongitude);
                        LatLng northeast = new LatLng(maxLatitude, maxLongitude);
                        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
                                .include(southwest).build();

                        MapStatusUpdate u = MapStatusUpdateFactory
                                .newLatLng(bounds.getCenter());
                        mBaiduMap.setMapStatus(u);
                    }

                    for (int i = 0; i < dataList.size(); i++) {
                        Map<String, Object> map = (Map<String, Object>) dataList.get(i);

                        if (Helper.isNullOrEmpty(map.get("Latitude")) || Helper.isNullOrEmpty(map.get("Longitude"))) {

                        } else {
                            String latitude = map.get("Latitude").toString();
                            String longitude = map.get("Longitude").toString();

                            LatLng ll = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                            BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
                            OverlayOptions ooA = new MarkerOptions().position(ll).icon(bdA).zIndex(i).draggable(true);

                            mBaiduMap.addOverlay(ooA);
                        }
                    }

                    /*for(Patrol patrol : patrols) {
                        System.out.println(patrol.getPatrolName() + "-" + patrol.getLatitude() + "-" + patrol.getLongitude());
                    }*/

                    SimpleAdapter adapter = new SimpleAdapter(MapActivity.this, list, R.layout.item_list_map_list, new String[]{"name"}, new int[]{R.id.textView});
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                materialDialog.hide();
            }
        });
    }

}
