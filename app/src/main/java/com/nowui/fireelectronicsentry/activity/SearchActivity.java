package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private ListView listView;
    SimpleAdapter listAdapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

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
        setContentView(R.layout.activity_search);
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

        String departmentId = setting.getString(Helper.KeyDepartmentId, "");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=1&deptID=" + departmentId);
        map.put("text", "日常工作");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=2&deptID=" + departmentId);
        map.put("text", "战训工作");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=3&deptID=" + departmentId);
        map.put("text", "政治工作");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=4&deptID=" + departmentId);
        map.put("text", "后勤工作");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=5&deptID=" + departmentId);
        map.put("text", "防消联勤");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("url", Helper.AdminWebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolTask_List.aspx?typeID=6&deptID=" + departmentId);
        map.put("text", "其他事项");
        list.add(map);

        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new SimpleAdapter(SearchActivity.this, list, R.layout.item_list_task_list, new String[]{"text"}, new int[]{R.id.textView});
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = list.get(position);

                Intent intent = new Intent();
                intent.putExtra("url", map.get("url").toString());
                intent.setClass(SearchActivity.this, CarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });
    }

}
