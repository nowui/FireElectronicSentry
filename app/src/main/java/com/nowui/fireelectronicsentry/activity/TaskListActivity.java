package com.nowui.fireelectronicsentry.activity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.model.Task;
import com.nowui.fireelectronicsentry.utility.Helper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

public class TaskListActivity extends AppCompatActivity {

    private ListView listView;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private Button rightButton;
    MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_task_list);
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

        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());

        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                load();
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*String url = "/task_add.html";

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                String userId = setting.getString(Helper.KeyUserId, "");
                String departmentId = setting.getString(Helper.KeyDepartmentId, "");

                Map<String, Object> map = list.get(position);
                String patrolScheduleID = map.get("id").toString();

                Map<String, Object> initMap = new HashMap<String, Object>();
                initMap.put("departmentId", departmentId);
                initMap.put("patrolScheduleID", patrolScheduleID);

                Intent intent = new Intent();
                intent.putExtra(Helper.KeyUrl, url);
                intent.putExtra(Helper.KeyIsLocal, !url.contains("http://"));
                intent.putExtra("initMap", (Serializable) initMap);
                intent.setClass(TaskListActivity.this, BaseActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);*/
            }
        });

        rightButton = (Button) findViewById(R.id.rightButton);
        rightButton.setText("新增");
        rightButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "/task_add.html";

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                String userId = setting.getString(Helper.KeyUserId, "");
                String departmentId = setting.getString(Helper.KeyDepartmentId, "");

                Map<String, Object> initMap = new HashMap<String, Object>();
                initMap.put("departmentId", departmentId);
                initMap.put("patrolScheduleID", 0);

                Intent intent = new Intent();
                intent.putExtra(Helper.KeyUrl, url);
                intent.putExtra(Helper.KeyIsLocal, !url.contains("http://"));
                intent.putExtra("initMap", (Serializable) initMap);
                intent.setClass(TaskListActivity.this, BaseActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Helper.CodeRequest) {
            if (resultCode == Helper.CodeResult) {
                //Map<Object, Object> map = (Map<Object, Object>) data.getSerializableExtra(Helper.KeyParameter);
                //System.out.println(map);

                load();
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

    private void load() {
        showLoadingDialog();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String url = Helper.WebUrl + "/MobilePatrolScheduleService.ashx?method=list";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/MobilePatrolScheduleService.ashx?method=list";
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        String userId = setting.getString(Helper.KeyUserId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("userID", Integer.valueOf(userId));
        dataObject.put("selectDay", sdf.format(calendarView.getCurrentDate().getCalendar().getTime()));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(TaskListActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    list.clear();

                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("patrolScheduleList");

                    for (Map<String, Object> map : dataList) {
                        map.put("id", map.get("PatrolScheduleID"));
                        map.put("name", map.get("EventSubject"));
                        list.add(map);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(TaskListActivity.this, list, R.layout.item_list_task_list, new String[]{"name"}, new int[]{R.id.textView});
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
                }

                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                materialDialog.hide();
            }
        });
    }


    private void loadTest() {
        showLoadingDialog();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String url = Helper.WebUrl + "/MobilePatrolScheduleService.ashx?method=patrolItemList";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/MobilePatrolScheduleService.ashx?method=patrolItemList";
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        String userId = setting.getString(Helper.KeyUserId, "");
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptID", departmentId);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(TaskListActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));



                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                materialDialog.hide();
            }
        });
    }

}