package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Month;
import com.nowui.fireelectronicsentry.model.Task;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements WeekView.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener, DateTimeInterpreter, WeekView.ScrollListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_WEEK_VIEW = 2;
    private static final int TYPE_MONTH_VIEW = 3;
    private int mWeekViewType = TYPE_MONTH_VIEW;
    private WeekView mWeekView;
    private TextView todayTextView;
    private ImageButton calendar_tab_00ImageButton;
    private ImageButton calendar_tab_01ImageButton;
    private ImageButton calendar_tab_02ImageButton;
    private GridView headerView;
    private GridView monthView;
    private GridViewCellAdapter monthAdapter;
    private List<Month> monthList = new ArrayList<Month>();
    private String dateString;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private MaterialDialog materialDialog;
    private List<Task> monthTaskList = new ArrayList<Task>();
    private List<Task> weekTaskList = new ArrayList<Task>();
    private List<Task> dayTaskList = new ArrayList<Task>();
    private int eventType = 0;
    private int total;
    private int start;
    private TextView monthHeaderView;
    private TextView monthFooterView;
    private ListView monthListView;
    private List<Map<String, Object>> monthAndWeekListForDay = new ArrayList<Map<String, Object>>();

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
        setContentView(R.layout.activity_calendar);
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

        eventType = getIntent().getIntExtra("eventType", 1);

        dateString = Helper.formatDateTime();

        String first = Helper.getMonthFirstDay();
        int count = Helper.getDayOfMonth();

        start = Helper.getDayOfWeek(first);

        total = count + (start - 1);

        todayTextView = (TextView) findViewById(R.id.todayTextView);
        todayTextView.setText(dateString.substring(0, 10));

        calendar_tab_00ImageButton = (ImageButton) findViewById(R.id.calendar_tab_00ImageButton);
        calendar_tab_00ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showMonth();

                clickMonth();
            }
        });

        calendar_tab_01ImageButton = (ImageButton) findViewById(R.id.calendar_tab_01ImageButton);
        calendar_tab_01ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    //showWeek();

                    clickWeek();
                }
            }
        });

        calendar_tab_02ImageButton = (ImageButton) findViewById(R.id.calendar_tab_02ImageButton);
        calendar_tab_02ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeekViewType != TYPE_DAY_VIEW) {

                    showDay();
                }
            }
        });


        mWeekView = (WeekView) findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setDateTimeInterpreter(this);
        mWeekView.setScrollListener(this);
        mWeekView.setXScrollingSpeed(0);

        mWeekView.setFirstDayOfWeek(Helper.getDay(dateString) + start - 1);


        List<Map<String, Object>> headerList = new ArrayList<Map<String, Object>>();
        String[] weeks = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        for (int i = 0; i < weeks.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("day", weeks[i]);
            headerList.add(map);
        }

        headerView = (GridView) findViewById(R.id.headerView);
        SimpleAdapter headerAdapter = new SimpleAdapter(this, headerList, R.layout.item_list_week_header, new String[] {"day"}, new int[] {R.id.dayTextView});
        headerView.setAdapter(headerAdapter);

        monthView = (GridView) findViewById(R.id.monthView);

        monthHeaderView = (TextView) findViewById(R.id.monthHeaderView);
        monthFooterView = (TextView) findViewById(R.id.monthFooterView);
        monthListView = (ListView) findViewById(R.id.monthListView);
        monthListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = monthAndWeekListForDay.get(position);

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                String departmentId = setting.getString(Helper.KeyDepartmentId, "");
                String usetType = setting.getString(Helper.KeyUserType, "");
                String userId = setting.getString(Helper.KeyUserId, "");


                if (usetType.equals("1")) {
                    departmentId = setting.getString(Helper.KeyChoiceDepartmentId, "");
                }

                if (usetType.equals("1") || usetType.equals("2")) {
                    Intent intent = new Intent();
                    intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolPathView.aspx?deptID=" + departmentId + "&subDeptID=" + map.get("subDeptID").toString() + "&userID=" + userId + "&usetType=" + usetType);
                    intent.setClass(CalendarActivity.this, CarActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                } else {
                    System.out.println("subDeptID:" + map.get("subDeptID").toString());

                    Intent intent = new Intent();
                    intent.putExtra("taskType", map.get("tasktype").toString());
                    intent.putExtra("taskid", map.get("taskid").toString());
                    intent.putExtra("taskname", map.get("taskname").toString());
                    intent.putExtra("subDeptID", map.get("subDeptID").toString());
                    intent.putExtra("subDeptName", map.get("subDeptName").toString());
                    intent.putExtra("taskStart", map.get("taskStart").toString());
                    intent.putExtra("taskEnd", map.get("taskEnd").toString());
                    intent.setClass(CalendarActivity.this, TaskActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                }
            }
        });

        load();
    }

    private void load() {
        showLoadingDialog();

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);

        String userId = setting.getString(Helper.KeyUserId, "");
        String deptID = setting.getString(Helper.KeyDepartmentId, "");

        String usetType = setting.getString(Helper.KeyUserType, "");
        if (usetType.equals("1")) {
            deptID  = setting.getString(Helper.KeyChoiceDepartmentId, "");
        }

        String url = Helper.WebUrl + "/TaskList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/TaskTypeList.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("userID", Integer.valueOf(userId));
        dataObject.put("deptID", Integer.valueOf(deptID));
        if (Helper.isAdmin) {
            String taskID = getIntent().getStringExtra("taskID");
            String taskName = getIntent().getStringExtra("taskName");

            dataObject.put("taskID", Integer.valueOf(taskID));
            dataObject.put("taskName", taskName);
        } else {
            dataObject.put("eventType", eventType);
        }
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(CalendarActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //System.out.println(new String(responseBody));
                Helper.show(new String(responseBody));


                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("UnfinishedPatrolTaskPOList");

                    if (Helper.isAdmin) {
                        Map<String, Object> data2Map = (Map<String, Object>) dataList.get(0);

                        List<Map<String, Object>> dayList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) data2Map.get("DayList")).get(0)).get("daytask");
                        List<Map<String, Object>> weekList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) data2Map.get("WeekList")).get(0)).get("WeekTaskList");
                        List<Map<String, Object>> monthList = (List<Map<String, Object>>) ((Map<String, Object>) ((List<Map<String, Object>>) data2Map.get("MonthList")).get(0)).get("MonthTask");

                        for (Map<String, Object> dayMap : dayList) {
                            Task task = new Task();
                            task.setTaskid(dayMap.get("taskid").toString());
                            task.setTaskname(dayMap.get("taskname").toString());
                            task.setTaskstart(dayMap.get("taskstart").toString());
                            task.setTaskend(dayMap.get("taskend").toString());

                            dayTaskList.add(task);
                        }

                        for (Map<String, Object> weekMap : weekList) {
                            Task task = new Task();
                            task.setTaskid(weekMap.get("taskid").toString());
                            task.setTaskname(weekMap.get("taskname").toString());
                            task.setTaskstart(weekMap.get("taskstart").toString());
                            task.setTaskend(weekMap.get("taskend").toString());

                            weekTaskList.add(task);
                        }

                        for (Map<String, Object> monthMap : monthList) {
                            Task task = new Task();
                            task.setTaskid(monthMap.get("taskid").toString());
                            task.setTaskname(monthMap.get("taskname").toString());
                            //task.setTaskstart(monthMap.get("taskstart").toString());
                            //task.setTaskend(monthMap.get("taskend").toString());

                            monthTaskList.add(task);
                        }
                    } else {
                        if (eventType == 1) {
                            dayTaskList = JSON.parseArray(dataMap.get("daytask").toString(), Task.class);
                        } else {
                            monthTaskList = JSON.parseArray(dataMap.get("monthtask").toString(), Task.class);
                            weekTaskList = JSON.parseArray(dataMap.get("weektask").toString(), Task.class);
                        }
                    }

                }

                System.out.println(dayTaskList.size() + "===========");

                int temp = 28;

                if (total > 28 && total <= 35) {
                    temp = 35;
                } else if (total > 35) {
                    temp = 42;
                }

                Calendar calendar = Calendar.getInstance();
                int now = calendar.get(Calendar.DATE) + start - 1;

                for(int i = 1; i <= temp; i++) {
                    Month month = new Month();

                    if (i < start) {
                        month.setDay("");
                    } else if (i > total) {
                        month.setDay("");
                    } else {
                        int nowDay = i - start + 1;
                        month.setDay("" + nowDay);

                        int weekCount = 0;
                        for(Task task : weekTaskList) {
                            int startDay = Helper.getDay(task.getTaskstart());
                            int endDay = Helper.getDay(task.getTaskend());

                            int startMonth = Helper.getMonth(task.getTaskstart());
                            int endMonth = Helper.getMonth(task.getTaskend());
                            int nowMonth = Helper.getMonth();

                            if (startMonth < nowMonth) {
                                int lastMonthTotalDay = Helper.getDayOfMonth(task.getTaskstart());
                                startDay = startDay - lastMonthTotalDay;
                            }

                            if (endMonth > nowMonth) {
                                int totalDay = Helper.getDayOfMonth();
                                endDay = totalDay + endDay;
                            }

                            /*System.out.println("nowDay:" + nowDay);
                            System.out.println("startDay:" + startDay);
                            System.out.println("endDay:" + endDay);
                            System.out.println("----------------------");*/

                            if(startDay <= nowDay && nowDay <= endDay) {
                                weekCount++;
                            }
                        }

                        int dayCount = 0;
                        for(Task task : dayTaskList) {
                            int startDay = Helper.getDay(task.getTaskstart());

                            if (startDay == nowDay) {
                                dayCount++;
                            }
                        }

                        if(i >= now) {
                            //month.setMessage(monthTaskList.size() + weekCount + dayCount);
                            month.setMessage(monthTaskList.size() + weekCount);

                            month.setMonthCount(monthTaskList.size());

                            month.setWeekCount(weekCount);
                        }
                    }

                    monthList.add(month);
                }

                monthView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        monthAdapter.setSelectIndex(position);
                        monthAdapter.notifyDataSetChanged();
                    }
                });

                monthAdapter = new GridViewCellAdapter(CalendarActivity.this, start - 2);
                monthView.setAdapter(monthAdapter);
                monthAdapter.notifyDataSetChanged();

                countMonthAndWeekList();

                if(eventType == 1) {
                    showDay();
                } else {
                    showMonth();
                }

                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                materialDialog.hide();

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    private void showMonth() {
        calendar_tab_00ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_00_active));
        calendar_tab_01ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_01));
        calendar_tab_02ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_02));

        calendar_tab_00ImageButton.setVisibility(View.VISIBLE);
        calendar_tab_01ImageButton.setVisibility(View.VISIBLE);
        calendar_tab_02ImageButton.setVisibility(View.INVISIBLE);

        headerView.setVisibility(View.VISIBLE);
        monthHeaderView.setVisibility(View.VISIBLE);
        monthFooterView.setVisibility(View.VISIBLE);
        monthView.setVisibility(View.VISIBLE);
        mWeekView.setVisibility(View.INVISIBLE);

        mWeekViewType = TYPE_MONTH_VIEW;
    }

    private void showWeek() {
        calendar_tab_00ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_00));
        calendar_tab_01ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_01_active));
        calendar_tab_02ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_02));

        headerView.setVisibility(View.INVISIBLE);
        monthHeaderView.setVisibility(View.INVISIBLE);
        monthFooterView.setVisibility(View.INVISIBLE);
        monthView.setVisibility(View.INVISIBLE);
        mWeekView.setVisibility(View.VISIBLE);

        mWeekViewType = TYPE_WEEK_VIEW;
        mWeekView.setNumberOfVisibleDays(7);

        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            calendar.setTime(dateFormat.parse(dateString));
            mWeekView.goToDate(calendar);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    private void showDay() {
        calendar_tab_00ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_00));
        calendar_tab_01ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_01));
        calendar_tab_02ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_02_active));

        calendar_tab_00ImageButton.setVisibility(View.INVISIBLE);
        calendar_tab_01ImageButton.setVisibility(View.INVISIBLE);
        calendar_tab_02ImageButton.setVisibility(View.INVISIBLE);

        headerView.setVisibility(View.INVISIBLE);
        monthHeaderView.setVisibility(View.INVISIBLE);
        monthFooterView.setVisibility(View.INVISIBLE);
        monthView.setVisibility(View.INVISIBLE);
        mWeekView.setVisibility(View.VISIBLE);
        monthListView.setVisibility(View.INVISIBLE);

        mWeekViewType = TYPE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(1);

        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            calendar.setTime(dateFormat.parse(dateString));
            mWeekView.goToDate(calendar);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    private void clickMonth() {
        mWeekViewType = TYPE_MONTH_VIEW;

        calendar_tab_00ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_00_active));
        calendar_tab_01ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_01));

        monthAdapter.notifyDataSetChanged();

        countMonthAndWeekList();
    }

    private void clickWeek() {
        mWeekViewType = TYPE_WEEK_VIEW;

        calendar_tab_00ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_00));
        calendar_tab_01ImageButton.setBackground(getResources().getDrawable(R.drawable.calendar_tab_01_active));

        monthAdapter.notifyDataSetChanged();

        countMonthAndWeekList();
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        //System.out.println("onMonthChange");

        for(Task task : monthTaskList) {
            for(int i = 0; i < total - start - 1; i++) {
                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, 0);
                startTime.set(Calendar.MINUTE, 0);
                startTime.set(Calendar.MONTH, newMonth - 1);
                startTime.set(Calendar.YEAR, newYear);
                startTime.add(Calendar.DATE, i);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.add(Calendar.HOUR, 1);
                endTime.set(Calendar.MONTH, newMonth - 1);
                WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
                event.setName(task.getTaskname());
                event.setId(Long.valueOf(task.getTaskid()));
                event.setColor(getResources().getColor(R.color.month_remind_color));
                events.add(event);
            }
        }

        for(Task task : weekTaskList) {
            int nowDay = Helper.getDay();
            int startDay = Helper.getDay(task.getTaskstart());
            int endDay = Helper.getDay(task.getTaskend());

            int startMonth = Helper.getMonth(task.getTaskstart());
            int endMonth = Helper.getMonth(task.getTaskend());
            int nowMonth = Helper.getMonth();

            if (startMonth < nowMonth) {
                int lastMonthTotalDay = Helper.getDayOfMonth(task.getTaskstart());
                startDay = startDay - lastMonthTotalDay;
            }

            if (endMonth > nowMonth) {
                int totalDay = Helper.getDayOfMonth();
                endDay = totalDay + endDay;
            }

            for(int i = 0; i < total - start - 1; i++) {

                if(startDay <= nowDay + i && nowDay + i <= endDay) {
                    Calendar taskStartTime = Calendar.getInstance();
                    taskStartTime.setTime(Helper.formatDate(task.getTaskstart()));

                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, 0);
                    startTime.set(Calendar.MINUTE, 0);
                    startTime.set(Calendar.MONTH, newMonth);
                    startTime.set(Calendar.YEAR, newYear);
                    startTime.add(Calendar.DATE, i);
                    Calendar endTime = (Calendar) startTime.clone();
                    endTime.add(Calendar.HOUR, 1);
                    endTime.set(Calendar.MONTH, newMonth);
                    WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
                    event.setName(task.getTaskname());
                    event.setId(Long.valueOf(task.getTaskid()));
                    event.setColor(getResources().getColor(R.color.month_remind_color));

                    int tempMonth = taskStartTime.get(Calendar.MONTH);
                    if(tempMonth == 0) {
                        tempMonth = 12;
                    }

                    if (newMonth == tempMonth) {
                        events.add(event);
                    }
                }
            }
        }

        /*System.out.println(dayTaskList.size() + "++++++++");

        for(Task task : dayTaskList) {
            if (task.getTaskstart().equals("2015-12-22 00:00:00")) {
                System.out.println(task.getTaskstart() + "--" + task.getTaskend() + "--" + task.getTaskid() + "--" + task.getSubDeptID() + "--" + task.getSubDeptName());
            }
        }*/

        for(Task task : dayTaskList) {
            int nowDay = Helper.getDay();
            int startDay = Helper.getDay(task.getTaskstart());
            int endDay = Helper.getDay(task.getTaskend());

            for(int i = 0; i < total - start - 1; i++) {
                //System.out.println("i:" + i + "-" + task.getTaskid());

                if(startDay == nowDay + i && endDay == nowDay + i) {
                    Calendar taskStartTime = Calendar.getInstance();
                    taskStartTime.setTime(Helper.formatDate(task.getTaskstart()));

                    Calendar taskEndTime = Calendar.getInstance();
                    taskEndTime.setTime(Helper.formatDate(task.getTaskend()));

                    //System.out.println(startDay + "-" + nowDay + "-" + i);
                    Calendar startTime = Calendar.getInstance();
                    //startTime.setTime(Helper.formatDate(task.getTaskstart()));
                    startTime.set(Calendar.HOUR_OF_DAY, taskStartTime.get(Calendar.HOUR_OF_DAY));
                    startTime.set(Calendar.MINUTE, taskStartTime.get(Calendar.MINUTE));
                    startTime.set(Calendar.MONTH, newMonth);
                    startTime.set(Calendar.YEAR, newYear);
                    startTime.add(Calendar.DATE, i);
                    Calendar endTime = (Calendar) startTime.clone();
                    endTime.set(Calendar.HOUR_OF_DAY, taskEndTime.get(Calendar.HOUR_OF_DAY));
                    endTime.set(Calendar.MINUTE, taskEndTime.get(Calendar.MINUTE));
                    endTime.set(Calendar.MONTH, newMonth);
                    endTime.add(Calendar.SECOND, -1);
                    //endTime.setTime(Helper.formatDate(task.getTaskend()));
                    WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
                    event.setName(task.getSubDeptName() + task.getTaskname());
                    event.setId(Long.valueOf(task.getSubDeptID() + task.getTaskid()));
                    //event.setColor(getResources().getColor(R.color.month_remind_color));

                    int tempMonth = taskStartTime.get(Calendar.MONTH);
                    if(tempMonth == 0) {
                        tempMonth = 12;
                    }

                    if (newMonth == tempMonth) {

                        /*System.out.println("startDay:" + startDay);
                        System.out.println("nowDay:" + nowDay);
                        System.out.println("endDay:" + endDay);
                        System.out.println("i:" + i);
                        System.out.println("----------------------------------:");*/


                        Calendar nowTime = Calendar.getInstance();
                        //System.out.println(endTime.compareTo(nowTime));
                        //System.out.println("-------------------------");
                        if(endTime.compareTo(nowTime) < 0) {
                            event.setColor(getResources().getColor(R.color.month_warm_color));
                        } else {
                            Calendar tempTime = Calendar.getInstance();
                            tempTime.set(Calendar.HOUR_OF_DAY, 2);

                            if(startTime.compareTo(tempTime) <= 0) {
                                event.setColor(getResources().getColor(R.color.event_color_05));
                            } else {
                                event.setColor(getResources().getColor(R.color.month_remind_color));
                            }
                        }

                        events.add(event);
                    }
                }
            }
        }

        return events;
    }

    private String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        //Toast.makeText(CalendarActivity.this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();

        String taskType = "1";
        String taskID = "0";
        String taskName = "";
        String subDeptID = "0";
        String subDeptName = "";
        String taskStart = "";
        String taskEnd = "";

        System.out.println(event.getId());

        for(Task task : monthTaskList) {
            if (Long.valueOf(task.getTaskid()).equals(event.getId())) {
                taskType = "3";
                taskID = task.getTaskid();
                taskName = task.getTaskname();
                subDeptID = task.getSubDeptID();
                subDeptName = task.getSubDeptName();
                taskStart = task.getTaskstart();
                taskEnd = task.getTaskend();
                break;
            }
        }

        for(Task task : weekTaskList) {
            if (Long.valueOf(task.getTaskid()).equals(event.getId())) {
                taskType = "2";
                taskID = task.getTaskid();
                taskName = task.getTaskname();
                subDeptID = task.getSubDeptID();
                subDeptName = task.getSubDeptName();
                taskStart = task.getTaskstart();
                taskEnd = task.getTaskend();
                break;
            }
        }

        for(Task task : dayTaskList) {
            if (Long.valueOf(task.getSubDeptID() + task.getTaskid()).equals(event.getId())) {
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date date = null;
                try {
                    date = sdf.parse(task.getTaskstart());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(date);

                try {
                    date = sdf.parse(task.getTaskend());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(date);

                if(startCalendar.get(Calendar.YEAR) == event.getStartTime().get(Calendar.YEAR)
                        && startCalendar.get(Calendar.MONTH) == event.getStartTime().get(Calendar.MONTH)
                        && startCalendar.get(Calendar.DATE) == event.getStartTime().get(Calendar.DATE)
                        && startCalendar.get(Calendar.HOUR) == event.getStartTime().get(Calendar.HOUR)
                        && startCalendar.get(Calendar.MINUTE) == event.getStartTime().get(Calendar.MINUTE)
                        && endCalendar.get(Calendar.YEAR) == event.getEndTime().get(Calendar.YEAR)
                        && endCalendar.get(Calendar.MONTH) == event.getEndTime().get(Calendar.MONTH)
                        && endCalendar.get(Calendar.DATE) == event.getEndTime().get(Calendar.DATE)
                        && endCalendar.get(Calendar.HOUR) == event.getEndTime().get(Calendar.HOUR)
                        && endCalendar.get(Calendar.MINUTE) == event.getEndTime().get(Calendar.MINUTE)
                        ) {
                    taskType = "1";
                    taskID = task.getTaskid();
                    taskName = task.getTaskname();
                    subDeptID = task.getSubDeptID();
                    subDeptName = task.getSubDeptName();
                    taskStart = task.getTaskstart();
                    taskEnd = task.getTaskend();
                    break;
                }

                /*System.out.println(event.getStartTime().get(Calendar.YEAR));
                System.out.println(event.getStartTime().get(Calendar.MONTH));
                System.out.println(event.getStartTime().get(Calendar.DATE));
                System.out.println(event.getStartTime().get(Calendar.HOUR));
                System.out.println(event.getStartTime().get(Calendar.MINUTE));*/

            }
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");
        String usetType = setting.getString(Helper.KeyUserType, "");
        String userId = setting.getString(Helper.KeyUserId, "");


        if (usetType.equals("1")) {
            departmentId  = setting.getString(Helper.KeyChoiceDepartmentId, "");
        }

        if (usetType.equals("1") || usetType.equals("2")) {
            Intent intent = new Intent();
            intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/PatrolTaskExecute/PatrolPathView.aspx?deptID=" + departmentId + "&subDeptID=" + subDeptID + "&userID=" + userId + "&usetType=" + usetType);
            intent.setClass(CalendarActivity.this, CarActivity.class);
            startActivityForResult(intent, Helper.CodeRequest);
        } else {
            /*System.out.println(taskID);
            System.out.println(subDeptID);
            System.out.println(taskStart);
            System.out.println(taskEnd);*/
            Intent intent = new Intent();
            intent.putExtra("taskType", taskType);
            intent.putExtra("taskid", String.valueOf(taskID));
            intent.putExtra("taskname", taskName);
            intent.putExtra("subDeptID", subDeptID);
            intent.putExtra("subDeptName", subDeptName);
            intent.putExtra("taskStart", taskStart);
            intent.putExtra("taskEnd", taskEnd);
            intent.setClass(CalendarActivity.this, TaskActivity.class);
            startActivityForResult(intent, Helper.CodeRequest);
        }
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(CalendarActivity.this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public String interpretDate(Calendar date) {
        SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        String weekday = weekdayNameFormat.format(date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("M-d", Locale.getDefault());

        weekday = String.valueOf(weekday.charAt(0));
        return format.format(date.getTime());
        //return weekday.toUpperCase();
    }

    @Override
    public String interpretTime(int hour) {
        //return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
        return hour + ":00";
    }

    @Override
    public void onFirstVisibleDayChanged(Calendar calendar, Calendar calendar1) {
        //System.out.println("--------------------------------------------------------");
    }

    public void countMonthAndWeekList() {
        int now = monthAdapter.selectIndex - monthAdapter.start;

        monthAndWeekListForDay = new ArrayList<Map<String, Object>>();

        if(mWeekViewType == TYPE_WEEK_VIEW) {
            for (Task task : weekTaskList) {
                int startDay = Helper.getDay(task.getTaskstart());
                int endDay = Helper.getDay(task.getTaskend());

                int startMonth = Helper.getMonth(task.getTaskstart());
                int endMonth = Helper.getMonth(task.getTaskend());
                int nowMonth = Helper.getMonth();

                if (startMonth < nowMonth) {
                    int lastMonthTotalDay = Helper.getDayOfMonth(task.getTaskstart());
                    startDay = startDay - lastMonthTotalDay;
                }

                if (endMonth > nowMonth) {
                    int totalDay = Helper.getDayOfMonth();
                    endDay = totalDay + endDay;
                }

                if (startDay <= now && now <= endDay) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("tasktype", 2);
                    map.put("taskid", task.getTaskid());
                    map.put("taskname", task.getTaskname());
                    map.put("taskstart", task.getTaskstart());
                    map.put("taskend", task.getTaskend());
                    map.put("subDeptID", task.getSubDeptID());
                    map.put("subDeptName", task.getSubDeptName() + task.getTaskname());
                    map.put("taskStart", task.getTaskstart());
                    map.put("taskEnd", task.getTaskend());

                    monthAndWeekListForDay.add(map);
                }
            }
        }

        if(mWeekViewType == TYPE_MONTH_VIEW) {
            String monthTaskStart = Helper.getMonthFirstDay();
            String monthTaskEnd = Helper.getMonthLastDay();

            for (Task task : monthTaskList) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("tasktype", 3);
                map.put("taskid", task.getTaskid());
                map.put("taskname", task.getTaskname());
                map.put("taskstart", monthTaskStart);
                map.put("taskend", monthTaskEnd);
                map.put("subDeptID", task.getSubDeptID());
                map.put("subDeptName", task.getSubDeptName() + task.getTaskname());
                map.put("taskStart", "");
                map.put("taskEnd", "");

                monthAndWeekListForDay.add(map);
            }
        }

        SimpleAdapter monthListViewAdapter = new SimpleAdapter(CalendarActivity.this, monthAndWeekListForDay, R.layout.item_list_task_list, new String[]{"subDeptName"}, new int[]{R.id.textView});
        monthListView.setAdapter(monthListViewAdapter);
        monthListViewAdapter.notifyDataSetChanged();
    }

    private class GridViewCellAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        public int selectIndex = -1;
        public int start;
        private int now;

        public GridViewCellAdapter(Context context, int start) {
            this.inflater = LayoutInflater.from(context);
            this.start = start;
            this.context = context;

            Calendar calendar = Calendar.getInstance();
            this.selectIndex = calendar.get(Calendar.DATE) + this.start;
            this.now = this.selectIndex;
        }

        @Override
        public int getCount() {
            return monthList.size();
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
            Month month = monthList.get(selectIndex);

            if(month.getDay() != "" && selectIndex >= this.now) {
                this.selectIndex = selectIndex;

                dateString = dateString.substring(0, 7) + "-" + (selectIndex - this.start);

                //showDay();
                countMonthAndWeekList();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MonthViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_month, parent, false);

                holder = new MonthViewHolder();
                holder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);

                if (Helper.getScreenWidth(this.context) < 720) {
                    RelativeLayout.LayoutParams dayTextViewLayoutParams = (RelativeLayout.LayoutParams) holder.dayTextView.getLayoutParams();
                    dayTextViewLayoutParams.height = Helper.formatPix(this.context, 100);
                    holder.dayTextView.setLayoutParams(dayTextViewLayoutParams);
                }

                holder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
                convertView.setTag(holder);
            } else {
                holder = (MonthViewHolder) convertView.getTag();
            }

            Month month = monthList.get(position);

            holder.messageTextView.setVisibility(View.VISIBLE);

            int maxtDay = Helper.getDayOfMonth();
            int nowDay = Helper.getDay();

            if(nowDay + 2 > maxtDay) {
                Resources resources = getResources();
                Drawable btnDrawable = resources.getDrawable(R.drawable.design_yellow_point);
                holder.messageTextView.setBackground(btnDrawable);
            }

            int message = month.getMessage();
            if (message > 0) {
                //holder.messageTextView.setText(month.getMessage() + "");

                if(mWeekViewType == TYPE_MONTH_VIEW) {
                    holder.messageTextView.setText(month.getMonthCount() + "");

                    if (month.getMonthCount() == 0) {
                        holder.messageTextView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    holder.messageTextView.setText(month.getWeekCount() + "");

                    if (month.getWeekCount() == 0) {
                        holder.messageTextView.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                holder.messageTextView.setVisibility(View.INVISIBLE);
            }

            holder.dayTextView.setText(month.getDay());

            if (this.selectIndex == position) {
                holder.dayTextView.setBackgroundColor(Color.parseColor("#11000000"));
            } else {
                holder.dayTextView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }
    }

    private class MonthViewHolder {
        public TextView dayTextView;
        public TextView messageTextView;
    }

    @Override
    protected void onDestroy() {
        materialDialog.dismiss();

        super.onDestroy();
    }

}
