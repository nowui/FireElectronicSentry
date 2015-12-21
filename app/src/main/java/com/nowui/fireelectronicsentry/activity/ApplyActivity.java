package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.utility.Helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplyActivity extends AppCompatActivity {

    private GridView applyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_apply);
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

        List<Map<String, Object>> applyList = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_00);
        map.put("text", "消防设施年检维护");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_01);
        map.put("text", "防火检查记录(月检)");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_02);
        map.put("text", "防火巡查记录");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_03);
        map.put("text", "消防控制室值班记录");
        applyList.add(map);

        /*map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_04);
        map.put("text", "户籍管理");
        //applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_05);
        map.put("text", "实时对讲");
        //applyList.add(map);*/

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_04);
        map.put("text", "巡查执行列表");
        applyList.add(map);

        /*map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_05);
        map.put("text", "巡查新增");
        applyList.add(map);*/

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_06);
        map.put("text", "GIS地图轨迹");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_07);
        map.put("text", "巡查异常超限");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_08);
        map.put("text", "灭火器过期提醒");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.apply_menu_09);
        map.put("text", "消防检测项其他");
        applyList.add(map);

        applyView = (GridView) findViewById(R.id.applyView);
        SimpleAdapter applyAdapter = new SimpleAdapter(this, applyList, R.layout.item_list_apply, new String[] {"image", "text"}, new int[] {R.id.imageView, R.id.textView});
        applyView.setAdapter(applyAdapter);
        applyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                String userId = setting.getString(Helper.KeyUserId, "");
                String departmentId = setting.getString(Helper.KeyDepartmentId, "");
                String choiceDepartmentId = setting.getString(Helper.KeyChoiceDepartmentId, "");
                String usetType = setting.getString(Helper.KeyUserType, "");

                if (usetType.equals("1")) {
                    departmentId = choiceDepartmentId;
                }

                Intent intent = new Intent();
                if(position == 0) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Report/FireYearMaintainReport.aspx?SubMenuID=40917&deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 1) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Report/FireMonthRecordReport.aspx?SubMenuID=40918&deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 2) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Report/FireInspectionRecordReport.aspx?SubMenuID=40916&deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 3) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Report/FireControlRoomRecordReport.aspx?SubMenuID=40919&deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                /*} else if (position == 4) {
                    intent.putExtra("url", "http://218.242.145.23:85/FrameSet/Login.aspx");
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 5) {
                    intent.setClass(ApplyActivity.this, TalkActivity.class);*/
                } else if (position == 4) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolPathView.aspx?deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                /*} else if (position == 5) {
                    intent.setClass(ApplyActivity.this, TaskListActivity.class);*/
                } else if (position == 5) {
                    intent.setClass(ApplyActivity.this, MapActivity.class);
                } else if (position == 6) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolAbnormalStatistic_List.aspx?deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 7) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/PatrolTaskExecute/AlertFireExtinguisherExpire_List.aspx?deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else if (position == 8) {
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Report/FireInspectionRecordOtherReport.aspx?deptID=" + departmentId + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                }

                /*if (position == 0) {
                    intent.putExtra("url", "http://218.242.145.23:85/FrameSet/Login.aspx");
                    intent.setClass(ApplyActivity.this, CarActivity.class);
                } else {
                    intent.setClass(ApplyActivity.this, TalkActivity.class);
                }*/
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });
    }

}
