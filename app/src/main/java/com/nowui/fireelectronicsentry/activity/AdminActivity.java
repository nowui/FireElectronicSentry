package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.dao.AttendanceDao;
import com.nowui.fireelectronicsentry.model.Attendance;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.model.TaskAbnormal;
import com.nowui.fireelectronicsentry.utility.DbHelper;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private long exitTime;
    private GridView applyView;
    private Attendance attendance;
    private final DbHelper dbHelper = new DbHelper(this, Helper.DatabaseName, null, Helper.DatabaseVersion);

    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private PullToRefreshListView pullRefreshListView;
    private List<Map<String, Object>> noticeList = new ArrayList<Map<String, Object>>();
    private boolean isLanch = true;
    private int page = 1;
    private List<Map<String, Object>> menuList = new ArrayList<Map<String, Object>>();
    GridViewCellAdapter gridViewCellAdapter;
    private String messageContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_admin);
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

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AdminActivity.this)
                        .title(getResources().getString(R.string.dialog_title_setting))
                        .items(R.array.admin_setting_values)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0 || which == 1) {
                                    IntentIntegrator integrator = new IntentIntegrator(AdminActivity.this);
                                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                                    integrator.setCaptureActivity(ScanActivity.class);
                                    integrator.setPrompt(getResources().getString(R.string.qr_code_prompt));
                                    integrator.setCameraId(0);
                                    integrator.setBeepEnabled(false);
                                    integrator.setOrientationLocked(false);
                                    integrator.initiateScan();
                                } else if (which == 2 || which == 3) {
                                    Intent intent = new Intent();
                                    intent.putExtra("loginOrAttendanceStatus", String.valueOf(which));
                                    intent.setClass(AdminActivity.this, AttendanceActivity.class);
                                    startActivityForResult(intent, Helper.CodeRequest);
                                } else if (which == 4) {
                                    new MaterialDialog.Builder(AdminActivity.this)
                                            .title("是否登出系统？")
                                            .neutralText("是")
                                            .positiveText("否")
                                            .neutralColor(Color.WHITE)
                                            .negativeColor(Color.WHITE)
                                            .positiveColor(Color.WHITE)
                                            .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.NEUTRAL)
                                            .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.NEGATIVE)
                                            .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onNeutral(final MaterialDialog dialog) {
                                                    attendance = new Attendance();

                                                    SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                                                    String userId = setting.getString(Helper.KeyUserId, "");
                                                    String userName = setting.getString(Helper.KeyUserName, "");

                                                    attendance.setUserID(Integer.valueOf(userId));
                                                    attendance.setUserName(userName);
                                                    //attendance.setUserPictureBase64(Helper.encodeBase64(data));
                                                    attendance.setUserPictureBase64("");
                                                    attendance.setLoginOrAttendance(0);
                                                    attendance.setLoginOrAttendanceStatus(1);

                                                    showLoadingDialog();

                                                    AttendanceDao.post(AdminActivity.this, client, attendance, new AsyncHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                            System.out.println(new String(responseBody));

                                                            Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                                                            });

                                                            relogin();

                                                            materialDialog.hide();
                                                        }

                                                        @Override
                                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                            AttendanceDao.insert(dbHelper, attendance);

                                                            relogin();

                                                            materialDialog.hide();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onPositive(MaterialDialog dialog) {

                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .show();
            }
        });

        String weather = setting.getString(Helper.KeyWeather, "");
        TextView weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        //weatherTextView.setText(weather);
        weatherTextView.setText("设置");
        weatherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /*List<Map<String, Object>> applyList = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_00);
        map.put("text", "周计划");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_01);
        map.put("text", "日常工作");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_02);
        map.put("text", "战训工作");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_03);
        map.put("text", "政治工作");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_04);
        map.put("text", "后勤工作");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_05);
        map.put("text", "消防联勤");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_06);
        map.put("text", "其他事项");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_07);
        map.put("text", "工作查询");
        applyList.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.drawable.admin_menu_08);
        map.put("text", "工作查询");
        applyList.add(map);*/

        applyView = (GridView) findViewById(R.id.applyView);
        applyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = menuList.get(position);

                if(position == 0) {
                    Intent intent = new Intent();
                    intent.setClass(AdminActivity.this, AdminWeekActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                } else if(position == menuList.size() - 2) {
                    Intent intent = new Intent();
                    intent.setClass(AdminActivity.this, SearchActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                } else if(position == menuList.size() - 1) {
                    showReportDialog();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("taskID", map.get("TaskID").toString());
                    intent.putExtra("taskName", map.get("TaskName").toString());
                    intent.setClass(AdminActivity.this, CalendarActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                }
            }
        });
        gridViewCellAdapter = new GridViewCellAdapter(AdminActivity.this);
        applyView.setAdapter(gridViewCellAdapter);

        pullRefreshListView = (PullToRefreshListView) findViewById(R.id.pullRefreshListView);
        pullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        pullRefreshListView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull));
        pullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.refreshing));
        pullRefreshListView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.release));
        pullRefreshListView.getLoadingLayoutProxy().setLastUpdatedLabel(getResources().getString(R.string.update) + Helper.formatDateTime());
        pullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;

                load();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                load();
            }
        });

        ListView listView = pullRefreshListView.getRefreshableView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = noticeList.get(position - 1);

                Intent intent = new Intent();
                intent.putExtra("messageid", map.get("id").toString());
                intent.setClass(AdminActivity.this, NoticeDetailActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        load();

        loadMenu();
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
        String url = Helper.WebUrl + "/NotificationList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/NotificationList.ashx";
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        String userId = setting.getString(Helper.KeyUserId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("personid", Integer.valueOf(userId));
        dataObject.put("pagenum", page);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(AdminActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = JSON.parseObject(jsonMap.get("data").toString(), new TypeReference<Map<String, Object>>() {});

                    List<Notice> notices = JSON.parseArray(dataMap.get("messageList").toString(), Notice.class);

                    if (page == 1) {
                        noticeList.clear();
                    }

                    for (Notice notice : notices) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("id", notice.getMessageid());
                        map.put("title", notice.getMessageTitle());
                        noticeList.add(map);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(AdminActivity.this, noticeList, R.layout.item_list_admin_notice, new String[]{"title"}, new int[]{R.id.titleTextView});
                    ListView listView = pullRefreshListView.getRefreshableView();
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    if (notices.size() > 0) {
                        page++;
                    }
                }

                pullRefreshListView.onRefreshComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult");

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                //System.out.println("MainActivity: Cancelled scan");
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //System.out.println("MainActivity: Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                System.out.println(result.getContents());
                System.out.println("------------------------");

                String code = result.getContents();

                if(code.contains("DeptID{")) {
                    Map<String, Object> jsonMap = JSON.parseObject(code.replace("DeptID{", "{"), new TypeReference<Map<String, Object>>() {});

                    String departmentId = jsonMap.get("DeptID").toString();

                    SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString(Helper.KeyDepartmentId, departmentId);
                    editor.commit();


                    //dbHelper.getWritableDatabase().execSQL("update system_info set departmentId = '" + departmentId + "'");

                    //loadEmployee(departmentId);
                } else if(code.contains("Patrol{")) {
                    Intent intent = new Intent();
                    intent.putExtra("code", code.replace("Patrol{", "{"));
                    intent.setClass(AdminActivity.this, WriteActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);
                }
            }
        } else {
            if (Helper.isAdmin) {

            } else {
                //loadMenuCount();
            }
            //System.out.println("MainActivity: Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }

        if(requestCode == Helper.CodeRequest) {
            if (resultCode == Helper.CodeResult) {
                String taskItemDesc = data.getStringExtra("taskItemDesc");

                System.out.println(taskItemDesc);

                if(!Helper.isNullOrEmpty(taskItemDesc)) {
                    //messageContent = taskItemDesc;
                }

            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                exitTime = System.currentTimeMillis();

                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void relogin() {
        Intent intent = new Intent();
        intent.setClass(AdminActivity.this, MainActivity.class);
        startActivityForResult(intent, Helper.CodeRequest);

        finish();
    }

    private void loadMenu() {
        System.out.println("loadMenu");

        showLoadingDialog();

        //String url = "http://oa.herigbit.com.cn/OA/DepartmentPersonList.ashx";
        String url = Helper.WebUrl + "/TaskTypeUnfinishedStatisticService.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/TaskTypeUnfinishedStatisticService.ashx";
        }

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
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(AdminActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("taskTypeUnfinishedTaskPOList");

                    menuList.clear();

                    Map<String, Object> menuMap = new HashMap<String, Object>();
                    menuMap.put("image", R.drawable.admin_menu_00);
                    menuMap.put("TaskID", "0");
                    menuMap.put("TaskName", "周计划");
                    menuMap.put("UnfinishedTaskCount", "0");
                    menuList.add(menuMap);

                    for (int i = 0; i  < dataList.size(); i++) {
                        Map<String, Object> map = dataList.get(i);

                        Map<String, Object> itemMap = new HashMap<String, Object>();
                        if (i == 0) {
                            itemMap.put("image", R.drawable.admin_menu_01);
                        } else if (i == 1) {
                            itemMap.put("image", R.drawable.admin_menu_02);
                        } else if (i == 2) {
                            itemMap.put("image", R.drawable.admin_menu_03);
                        } else if (i == 3) {
                            itemMap.put("image", R.drawable.admin_menu_04);
                        } else if (i == 4) {
                            itemMap.put("image", R.drawable.admin_menu_05);
                        } else if (i == 5) {
                            itemMap.put("image", R.drawable.admin_menu_06);
                        } else if (i == 6) {
                            itemMap.put("image", R.drawable.admin_menu_07);
                        } else if (i == 7) {
                            itemMap.put("image", R.drawable.admin_menu_08);
                        } else if (i == 9) {
                            itemMap.put("image", R.drawable.admin_menu_00);
                        } else if (i == 10) {
                            itemMap.put("image", R.drawable.admin_menu_01);
                        }
                        itemMap.put("TaskID", map.get("TaskID"));
                        itemMap.put("TaskName", map.get("TaskName"));
                        itemMap.put("UnfinishedTaskCount", map.get("UnfinishedTaskCount"));
                        menuList.add(itemMap);
                    }

                    menuMap = new HashMap<String, Object>();
                    menuMap.put("image", R.drawable.admin_menu_07);
                    menuMap.put("TaskID", "-1");
                    menuMap.put("TaskName", "工作查询");
                    menuMap.put("UnfinishedTaskCount", "0");
                    menuList.add(menuMap);

                    menuMap = new HashMap<String, Object>();
                    menuMap.put("image", R.drawable.admin_menu_08);
                    menuMap.put("TaskID", "-2");
                    menuMap.put("TaskName", "应急事件");
                    menuMap.put("UnfinishedTaskCount", "0");
                    menuList.add(menuMap);

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
            return menuList.size();
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
            Map<String, Object> map = menuList.get(selectIndex);


        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MenuViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_admin_menu, parent, false);

                holder = new MenuViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                holder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
                convertView.setTag(holder);
            } else {
                holder = (MenuViewHolder) convertView.getTag();
            }

            Map<String, Object> map = menuList.get(position);

            String message = map.get("UnfinishedTaskCount").toString();
            if (message == "0") {
                holder.messageTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.messageTextView.setText(message);
            }

            if (! Helper.isNullOrEmpty(map.get("TaskName"))) {
                holder.textView.setText(map.get("TaskName").toString());
            }

            holder.imageView.setImageDrawable(getResources().getDrawable((Integer) map.get("image")));

            return convertView;
        }
    }

    private class MenuViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView messageTextView;
    }

    private void showReportDialog() {
        new MaterialDialog.Builder(this)
                //.theme(Theme.LIGHT)
                .title("应急事件申报")
                .neutralText("提交")
                .negativeText("拍照")
                .positiveText("说明")
                .autoDismiss(false)
                .neutralColor(Color.WHITE)
                .negativeColor(Color.WHITE)
                .positiveColor(Color.WHITE)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.NEUTRAL)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.NEGATIVE)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(final MaterialDialog dialog) {
                        String filename = "emergencyPicture.jpg";
                        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/" + filename);
                        if (fileFolder.exists()) {
                            /*if (Helper.isNullOrEmpty(messageContent)) {
                                Toast.makeText(MainActivity.this, "请填写说明再提交应急事件", Toast.LENGTH_SHORT).show();
                            } else {*/
                            showLoadingDialog();

                            String url = Helper.WebUrl + "/EmergencyNotification.ashx";
                            if (Helper.isAdmin) {
                                url = Helper.AdminWebUrl + "/EmergencyNotification.ashx";
                            }

                            SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                            String departmentId = setting.getString(Helper.KeyDepartmentId, "");
                            String departmentName = setting.getString(Helper.KeyDepartmentName, "");
                            String userId = setting.getString(Helper.KeyUserId, "");
                            String userName = setting.getString(Helper.KeyUserName, "");
                            String baiduUserId = setting.getString(Helper.KeyBaiduUserId, "");
                            String baiduChannelId = setting.getString(Helper.KeyBaiduChannelId, "");

                            JSONObject jsonObject = new JSONObject();
                            String datetime = Helper.formatDateTime();
                            jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
                            jsonObject.put("datetime", datetime);
                            JSONObject dataObject = new JSONObject();
                            dataObject.put("deptID", Integer.valueOf(departmentId));
                            dataObject.put("deptName", departmentName);
                            dataObject.put("userID", Integer.valueOf(userId));
                            dataObject.put("userName", userName);
                            dataObject.put("baiduUserId", baiduUserId);
                            dataObject.put("baiduChannelId", baiduChannelId);
                            dataObject.put("title", "你有一件应急事件需要处理");
                            dataObject.put("messageContent", messageContent);
                            dataObject.put("messagePicture", Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/" + filename));
                            dataObject.put("messageAudio", Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/emergencyAudio.3gp"));
                            jsonObject.put("data", dataObject);

                            StringEntity stringEntity = null;
                            try {
                                stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            System.out.println(url);
                            System.out.println(jsonObject.toString());

                            client.post(AdminActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    System.out.println(new String(responseBody));

                                    Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                                    });

                                    if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                                        //Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                                        deleteEmergency();

                                        dialog.dismiss();

                                    }

                                    materialDialog.hide();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                                    materialDialog.hide();
                                }
                            });
                            //}


                        } else {
                            Toast.makeText(AdminActivity.this, "请拍照再提交应急事件", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("pictureName", "emergencyPicture");
                        intent.setClass(AdminActivity.this, CameraActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("taskAbnormalList", (Serializable) new ArrayList<TaskAbnormal>());
                        intent.putExtra("audioName", "emergencyAudio");
                        intent.setClass(AdminActivity.this, RemarkActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);
                    }
                })
                .show();
    }

    private void deleteEmergency() {
        messageContent = "";

        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/emergencyPicture.jpg");
        if (fileFolder.exists()) {
            fileFolder.delete();
        }

        File fileFolder2 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/emergencyAudio.3gp");
        if (fileFolder2.exists()) {
            fileFolder2.delete();
        }
    }

}
