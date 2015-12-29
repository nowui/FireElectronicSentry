package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.WHITE;

public class AdminWeekActivity extends AppCompatActivity {

    private GridView headerView;
    private List<Map<String, Object>> mainList = new ArrayList<Map<String, Object>>();
    private GridView mainView;
    private ListView listView;
    private List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    GridViewCellAdapter gridViewCellAdapter;
    SimpleAdapter listAdapter;

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
        setContentView(R.layout.activity_admin_week);
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

        List<Map<String, Object>> headerList = new ArrayList<Map<String, Object>>();
        String[] weeks = new String[]{"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        for (int i = 0; i < weeks.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("day", weeks[i]);
            headerList.add(map);
        }

        headerView = (GridView) findViewById(R.id.headerView);
        SimpleAdapter headerAdapter = new SimpleAdapter(this, headerList, R.layout.item_list_admin_week_header, new String[] {"day"}, new int[] {R.id.dayTextView});
        headerView.setAdapter(headerAdapter);

        mainView = (GridView) findViewById(R.id.mainView);
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mainList.get(position);

                if (map.get("type").toString() == "0") {
                    return;
                }

                taskList.clear();

                for(Map<String, Object> dataMap : dataList) {
                    if (map.get("id").toString().equals(dataMap.get("TaskTypeID").toString())) {
                        List<Map<String, Object>> dayList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) dataMap.get("DayList")).get(0)).get("daytask");
                        List<Map<String, Object>> weekList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) dataMap.get("WeekList")).get(0)).get("WeekTaskList");
                        List<Map<String, Object>> monthList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) dataMap.get("MonthList")).get(0)).get("MonthTask");

                        for (Map<String, Object> dayMap : dayList) {
                            if (dayMap.get("taskstart").toString().contains(map.get("day").toString())) {
                                Map<String, Object> itemMap = new HashMap<String, Object>();
                                itemMap.put("taskid", dayMap.get("taskid"));
                                itemMap.put("taskname", dayMap.get("taskname"));
                                itemMap.put("taskType", 1);
                                taskList.add(itemMap);
                            }
                        }

                        for (Map<String, Object> weekMap : weekList) {
                            Map<String, Object> itemMap = new HashMap<String, Object>();
                            itemMap.put("taskid", weekMap.get("taskid"));
                            itemMap.put("taskname", weekMap.get("taskname"));
                            itemMap.put("taskType", 2);
                            taskList.add(itemMap);
                        }

                        for (Map<String, Object> monthMap : monthList) {
                            Map<String, Object> itemMap = new HashMap<String, Object>();
                            itemMap.put("taskid", monthMap.get("taskid"));
                            itemMap.put("taskname", monthMap.get("taskname"));
                            itemMap.put("taskType", 3);
                            taskList.add(itemMap);
                        }
                    }
                }

                listAdapter.notifyDataSetChanged();
            }
        });
        gridViewCellAdapter = new GridViewCellAdapter(AdminWeekActivity.this);
        mainView.setAdapter(gridViewCellAdapter);

        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new SimpleAdapter(AdminWeekActivity.this, taskList, R.layout.item_list_task_list, new String[]{"taskname"}, new int[]{R.id.textView});
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = taskList.get(position);

                Intent intent = new Intent();
                intent.putExtra("taskType", Integer.valueOf(map.get("taskType").toString()));
                intent.putExtra("taskid", String.valueOf(map.get("taskid")));
                intent.putExtra("taskname", String.valueOf(map.get("taskname")));
                intent.setClass(AdminWeekActivity.this, TaskActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        load();
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

        String url = Helper.AdminWebUrl + "/TaskTypeList.ashx";

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();

        String userId = setting.getString(Helper.KeyUserId, "");
        String deptID = setting.getString(Helper.KeyDepartmentId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("userID", Integer.valueOf(userId));
        dataObject.put("deptID", Integer.valueOf(deptID));
        dataObject.put("taskID", Integer.valueOf("-1"));
        dataObject.put("taskName", "");
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(AdminWeekActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    dataList = (List<Map<String, Object>>) dataMap.get("UnfinishedPatrolTaskPOList");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    for(Map<String, Object> map : dataList) {
                        Map<String, Object> objectMap = new HashMap<String, Object>();
                        objectMap.put("type", 0);
                        objectMap.put("day", "");
                        objectMap.put("count", 0);
                        objectMap.put("id", map.get("TaskTypeID"));
                        objectMap.put("name", map.get("TaskTypeName"));
                        mainList.add(objectMap);

                        Calendar calendar = Calendar.getInstance();
                        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                            calendar.add(Calendar.DATE, -1);
                        }
                        calendar.add(Calendar.DATE, -1);

                        List<Map<String, Object>> dayList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) map.get("DayList")).get(0)).get("daytask");
                        List<Map<String, Object>> weekList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) map.get("WeekList")).get(0)).get("WeekTaskList");
                        List<Map<String, Object>> monthList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) map.get("MonthList")).get(0)).get("MonthTask");

                        for(int i = 0; i < 7; i++) {
                            String day = dateFormat.format(calendar.getTime());

                            int dayCount = 0;
                            for (Map<String, Object> dayMap : dayList) {
                                if (dayMap.get("taskstart").toString().contains(dateFormat.format(calendar.getTime()))) {
                                    dayCount++;
                                }
                            }

                            Map<String, Object> itemMap = new HashMap<String, Object>();
                            itemMap.put("type", 1);
                            itemMap.put("day", day);
                            itemMap.put("count", dayCount + weekList.size() + monthList.size());
                            itemMap.put("id", map.get("TaskTypeID"));
                            itemMap.put("name", map.get("TaskTypeName"));
                            mainList.add(itemMap);

                            calendar.add(Calendar.DATE, 1);
                        }
                    }

                    gridViewCellAdapter.notifyDataSetChanged();
                }

                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    materialDialog.hide();
            }
        });


    }

    private class GridViewCellAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public GridViewCellAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mainList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setSelectIndex(int selectIndex) {
            Map<String, Object> map = mainList.get(selectIndex);


        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_admin_week, parent, false);

                holder = new GridViewHolder();
                holder.titleView = (TextView) convertView.findViewById(R.id.titleView);
                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (GridViewHolder) convertView.getTag();
            }

            Map<String, Object> map = mainList.get(position);

            if (map.get("type").toString() == "0") {
                holder.titleView.setText(map.get("name").toString());

                holder.textView.setVisibility(View.INVISIBLE);
            } else {
                holder.titleView.setVisibility(View.INVISIBLE);

                holder.textView.setText(map.get("count").toString());
            }

            return convertView;
        }
    }

    private class GridViewHolder {
        public TextView titleView;
        public TextView textView;
    }

}
