package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.dao.TaskDao;
import com.nowui.fireelectronicsentry.model.TaskAbnormal;
import com.nowui.fireelectronicsentry.model.TaskItem;
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

public class TaskActivity extends AppCompatActivity {

    private TextView textView;
    private GridView gridview;
    private SimpleAdapter adapter;
    private ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private int index = -1;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private MaterialDialog loadingDialog;
    private MaterialDialog taskItemDialog;
    private List<TaskItem> taskItemList = new ArrayList<TaskItem>();
    private boolean isRead = false;
    private TaskItem uploadTaskItem = new TaskItem();
    private String userId;
    private String userName;
    private String taskType;
    private String taskid;
    private String taskname;
    private String taskitemid;
    private String taskitemname;
    private String patrolTaskExecutionId;
    private String deptPatrolID;
    private String subDeptID;
    private String subDeptName;
    private String taskStart;
    private String taskEnd;
    private List<TaskAbnormal> taskAbnormalList;
    private final DbHelper dbHelper = new DbHelper(this, Helper.DatabaseName, null, Helper.DatabaseVersion);
    private List<byte[]> pictureList = new ArrayList<byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_task);
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

        taskType = getIntent().getStringExtra("taskType");
        taskid = getIntent().getStringExtra("taskid");
        taskname = getIntent().getStringExtra("taskname");
        subDeptID = getIntent().getStringExtra("subDeptID");
        subDeptName = getIntent().getStringExtra("subDeptName");
        taskStart = getIntent().getStringExtra("taskStart");
        taskEnd = getIntent().getStringExtra("taskEnd");

        textView = (TextView) findViewById(R.id.textView);
        textView.setText(subDeptName + "-" + taskname);

        showLoadingDialog();

        userId = setting.getString(Helper.KeyUserId, "");
        userName = setting.getString(Helper.KeyUserName, "");
        String deptID = setting.getString(Helper.KeyDepartmentId, "");

        if (usetType.equals("1")) {
            deptID  = setting.getString(Helper.KeyChoiceDepartmentId, "");
        }

        String url = Helper.WebUrl + "/TaskDetail.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/TaskDetail.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptID", Integer.valueOf(deptID));
        dataObject.put("taskid", Integer.valueOf(taskid));
        dataObject.put("subDeptID", Integer.valueOf(subDeptID));
        dataObject.put("subDeptName", subDeptName);
        dataObject.put("taskStartDatetime", taskStart);
        dataObject.put("taskEndDatetime", taskEnd);
        dataObject.put("eventType", Integer.valueOf(taskType));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(TaskActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                    taskItemList = JSON.parseArray(dataMap.get("taskitemList").toString(), TaskItem.class);

                    for(TaskItem taskItem : taskItemList) {
                        System.out.println("=============" + taskItem.getTaskitemstatus());
                        Map<String, Object> map = new HashMap<String, Object>();
                        if (taskItem.getTaskitemstatus().equals("-1")) {
                            map.put("ItemImage", R.drawable.light_error);
                        } else if (taskItem.getTaskitemstatus().equals("0")) {
                            map.put("ItemImage", R.drawable.light);
                        } else if (taskItem.getTaskitemstatus().equals("1")) {
                            map.put("ItemImage", R.drawable.light_active);
                        }
                        map.put("ItemText", taskItem.getTaskitemname());
                        list.add(map);
                    }

                    adapter = new SimpleAdapter(TaskActivity.this, list, R.layout.item_list_task, new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});

                    gridview.setAdapter(adapter);
                }

                loadingDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loadingDialog.hide();

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
            }
        });

        gridview = (GridView) findViewById(R.id.GridView);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.no_nfc), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.open_nfc),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        ndef.addCategory("*/*");
        mFilters = new IntentFilter[] { ndef };
        mTechLists = new String[][] {
                new String[] { MifareClassic.class.getName() },
                new String[] { NfcA.class.getName() } };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            processIntent(intent);
        }
    }

    private void processIntent(Intent intent) {
        if(isRead) {
            return;
        }

        Parcelable[] rawmsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawmsgs[0];
        NdefRecord[] records = msg.getRecords();
        String result = new String(records[0].getPayload());

        Map<String, Object> dataMap = JSON.parseObject(result, new TypeReference<Map<String, Object>>() {});

        if(Helper.isNullOrEmpty(dataMap)) {
            return;
        }

        if(Helper.isNullOrEmpty(dataMap.get("PatrolItemID"))) {
            return;
        }

        boolean isExit = false;

        for(int i = 0; i < taskItemList.size(); i++) {
            TaskItem taskItem = taskItemList.get(i);
            if(dataMap.get("deptPatrolID").toString().contains(taskItem.getDeptPatrolID())) {
                isExit = true;

                uploadTaskItem = new TaskItem();
                uploadTaskItem.setPatrolTaskExecutionId(taskItem.getPatrolTaskExecutionId());
                uploadTaskItem.setTaskitemid(taskItem.getTaskitemid());
                uploadTaskItem.setTaskitemname(taskItem.getTaskitemname());
                uploadTaskItem.setTaskItemPicture("");
                uploadTaskItem.setTaskItemPicture2("");
                uploadTaskItem.setTaskItemPicture3("");
                uploadTaskItem.setTaskItemPicture4("");
                uploadTaskItem.setTaskItemPicture5("");
                uploadTaskItem.setTaskitemstatus("1");
                uploadTaskItem.setTaskItemDesc("");
                uploadTaskItem.setTaskItemAudio("");

                patrolTaskExecutionId = taskItem.getPatrolTaskExecutionId();
                taskitemid = taskItem.getTaskitemid();
                taskitemname = taskItem.getTaskitemname();
                taskAbnormalList = taskItem.getTaskAbnormalList();

                index = i;

                break;
            }
        }

        if (isExit) {
            isRead = true;

            deleteTask();

            showDialog();
        } else {

        }
    }

    private void showDialog() {
        String titleString = "该区域是否正常?";
        String yesString = "是";
        String noString = "否";
        if (Helper.isAdmin) {
            titleString = "是否提交任务?";
            yesString = " 提交";
            noString = "备注";
        }

        taskItemDialog = new MaterialDialog.Builder(this)
                //.theme(Theme.LIGHT)
                .title(titleString)
                .positiveText(noString)
                .neutralText(yesString)
                .cancelable(false)
                .autoDismiss(false)
                .positiveColor(Color.WHITE)
                .neutralColor(Color.WHITE)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.NEUTRAL)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(final MaterialDialog dialog) {
                        uploadTaskItem.setTaskItemPicture("");
                        uploadTaskItem.setTaskItemPicture2("");
                        uploadTaskItem.setTaskItemPicture3("");
                        uploadTaskItem.setTaskItemPicture4("");
                        uploadTaskItem.setTaskItemPicture5("");
                        uploadTaskItem.setTaskItemAudio("");
                        uploadTaskItem.setTaskItemDesc("");
                        uploadTaskItem.setTaskitemstatus("1");

                        showLoadingDialog();

                        /*String url = Helper.WebUrl + "/PatrolTaskExecution.ashx";

                        JSONObject jsonObject = new JSONObject();
                        String datetime = Helper.formatDateTime();
                        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
                        jsonObject.put("datetime", datetime);
                        JSONObject dataObject = new JSONObject();
                        dataObject.put("personID", Integer.valueOf(userId));
                        dataObject.put("personName", userName);
                        dataObject.put("personPicture", Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/personPicture.jpg"));
                        dataObject.put("taskID", Integer.valueOf(taskid));
                        dataObject.put("taskName", taskname);
                        dataObject.put("taskItemID", Integer.valueOf(uploadTaskItem.getTaskitemid()));
                        dataObject.put("taskItemName", uploadTaskItem.getTaskitemname());
                        dataObject.put("taskItemStatus", Integer.valueOf(uploadTaskItem.getTaskitemstatus()));
                        dataObject.put("taskItemPicture", uploadTaskItem.getTaskItemPicture());
                        dataObject.put("taskItemDesc", uploadTaskItem.getTaskItemDesc());
                        dataObject.put("taskItemAudio", uploadTaskItem.getTaskItemAudio());
                        jsonObject.put("data", dataObject);

                        StringEntity stringEntity = null;
                        try {
                            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        System.out.println("++++++++++++++++");
                        System.out.println(jsonObject.toString());*/

                        //TaskDao.insert(dbHelper, userId, userName, "", taskid, taskname, uploadTaskItem);

                        TaskDao.post(TaskActivity.this, client, userId, userName, "", taskid, taskname, uploadTaskItem, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                System.out.println(new String(responseBody));

                                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                                });

                                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {


                                    isRead = false;

                                    Map<String, Object> map = list.get(index);
                                    map.put("ItemImage", R.drawable.light_active);
                                    list.set(index, map);

                                    adapter.notifyDataSetChanged();

                                    dialog.dismiss();
                                }

                                loadingDialog.hide();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                TaskDao.insert(dbHelper, userId, userName, "", taskid, taskname, uploadTaskItem);

                                //Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                                dialog.dismiss();

                                loadingDialog.hide();
                            }
                        });

                        //dialog.dismiss();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        //dialog.dismiss();

                        showReportDialog();
                    }
                })
                .show();

        /*taskItemDialog = new MaterialDialog.Builder(TaskActivity.this)
                .title(taskitemname)
                .cancelable(false)
                .adapter(new TaskAbnormalAdapter(TaskActivity.this), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                    }
                }).show();*/
    }

    private void updateGridForError() {
        Map<String, Object> map = list.get(index);
        map.put("ItemImage", R.drawable.light_error);
        list.set(index, map);

        adapter.notifyDataSetChanged();
    }

    private void showReportDialog() {
        String title = "故障申报";

        if (Helper.isAdmin) {
            title = "任务备注说明";
        }

        new MaterialDialog.Builder(this)
                //.theme(Theme.LIGHT)
                .title(title)
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
                        String filename = "taskItemPicture.jpg";
                        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture1.jpg");
                        File fileFolder2 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture2.jpg");
                        File fileFolder3 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture3.jpg");
                        File fileFolder4 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture4.jpg");
                        File fileFolder5 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture5.jpg");
                        if (fileFolder.exists()) {
                            uploadTaskItem.setTaskItemPicture(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture1.jpg"));
                            if (fileFolder2.exists()) {
                                uploadTaskItem.setTaskItemPicture2(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture2.jpg"));
                            } else {
                                uploadTaskItem.setTaskItemPicture2("");
                            }
                            if (fileFolder3.exists()) {
                                uploadTaskItem.setTaskItemPicture3(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture3.jpg"));
                            } else {
                                uploadTaskItem.setTaskItemPicture3("");
                            }
                            if (fileFolder4.exists()) {
                                uploadTaskItem.setTaskItemPicture4(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture4.jpg"));
                            } else {
                                uploadTaskItem.setTaskItemPicture4("");
                            }
                            if (fileFolder5.exists()) {
                                uploadTaskItem.setTaskItemPicture5(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture5.jpg"));
                            } else {
                                uploadTaskItem.setTaskItemPicture5("");
                            }
                            uploadTaskItem.setTaskItemAudio(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemAudio.3gp"));
                            uploadTaskItem.setTaskitemstatus("-1");

                            showLoadingDialog();

                            /*String url = Helper.WebUrl + "/PatrolTaskExecution.ashx";

                            JSONObject jsonObject = new JSONObject();
                            String datetime = Helper.formatDateTime();
                            jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
                            jsonObject.put("datetime", datetime);
                            JSONObject dataObject = new JSONObject();
                            dataObject.put("personID", Integer.valueOf(userId));
                            dataObject.put("personName", userName);
                            dataObject.put("personPicture", Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/personPicture.jpg"));
                            dataObject.put("taskID", Integer.valueOf(taskid));
                            dataObject.put("taskName", taskname);
                            dataObject.put("taskItemID", Integer.valueOf(uploadTaskItem.getTaskitemid()));
                            dataObject.put("taskItemName", uploadTaskItem.getTaskitemname());
                            dataObject.put("taskItemStatus", Integer.valueOf(uploadTaskItem.getTaskitemstatus()));
                            dataObject.put("taskItemPicture", uploadTaskItem.getTaskItemPicture());
                            dataObject.put("taskItemDesc", uploadTaskItem.getTaskItemDesc());
                            dataObject.put("taskItemAudio", uploadTaskItem.getTaskItemAudio());
                            jsonObject.put("data", dataObject);

                            StringEntity stringEntity = null;
                            try {
                                stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            System.out.println("++++++++++++++++");
                            System.out.println(jsonObject.toString());*/

                            TaskDao.post(TaskActivity.this, client, userId, userName, "", taskid, taskname, uploadTaskItem, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    System.out.println(new String(responseBody));

                                    Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                                    });

                                    if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                                        //Map<String, Object> dataMap = JSON.parseObject(jsonMap.get("data").toString(), new TypeReference<Map<String, Object>>() {});


                                        isRead = false;

                                        updateGridForError();

                                        dialog.dismiss();
                                    }

                                    taskItemDialog.dismiss();

                                    loadingDialog.hide();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    TaskDao.insert(dbHelper, userId, userName, "", taskid, taskname, uploadTaskItem);

                                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                                    dialog.dismiss();

                                    taskItemDialog.dismiss();

                                    loadingDialog.hide();
                                }
                            });
                        } else {
                            Toast.makeText(TaskActivity.this, "请拍照再提交故障申报", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("pictureName", "taskItemPicture");
                        intent.setClass(TaskActivity.this, CameraListActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest1);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("taskType", taskType);
                        intent.putExtra("taskitemid", taskitemid);
                        intent.putExtra("taskAbnormalList", (Serializable) taskAbnormalList);
                        intent.putExtra("audioName", "taskItemAudio");
                        intent.setClass(TaskActivity.this, RemarkActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);
                    }
                })
                .show();
    }

    private void showLoadingDialog() {
        loadingDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Helper.CodeRequest) {
            if (resultCode == Helper.CodeResult) {
                String taskItemSelect = data.getStringExtra("taskItemSelect");
                String taskItemDesc = data.getStringExtra("taskItemDesc");
                String taskItemAddress = data.getStringExtra("taskItemAddress");

                System.out.println(taskItemDesc);

                if(!Helper.isNullOrEmpty(taskItemSelect)) {
                    uploadTaskItem.setTaskItemSelect(taskItemSelect);
                }

                if(!Helper.isNullOrEmpty(taskItemDesc)) {
                    uploadTaskItem.setTaskItemDesc(taskItemDesc);
                }

                if(!Helper.isNullOrEmpty(taskItemAddress)) {
                    uploadTaskItem.setTaskItemAddress(taskItemAddress);
                }
            }
        }

        if(requestCode == Helper.CodeRequest1) {
            if (resultCode == Helper.CodeRequest1) {
                //pictureList = (List<byte[]>) data.getSerializableExtra("pictureList");
                System.out.println(pictureList.size());
            }
        }
    }

    /*private class TaskAbnormalAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public TaskAbnormalAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return taskAbnormalList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TaskAbnormalViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_task_abnormal, parent, false);

                holder = new TaskAbnormalViewHolder();
                holder.button = (Button) convertView.findViewById(R.id.button);
                convertView.setTag(holder);
            } else {
                holder = (TaskAbnormalViewHolder) convertView.getTag();
            }

            TaskAbnormal taskAbnormal = taskAbnormalList.get(position);

            holder.button.setText(taskAbnormal.getAbnormalname());
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskAbnormal taskAbnormal = taskAbnormalList.get(position);

                    System.out.println("position:" + position);

                    System.out.println(taskAbnormal.getAbnormaltype());

                    if (taskAbnormal.getAbnormaltype().equals("0")) {

                    } else {
                        showReportDialog();
                    }
                }
            });

            return convertView;
        }
    }

    private class TaskAbnormalViewHolder {
        public Button button;
    }*/

    private void deleteTask() {

        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture1.jpg");
        if (fileFolder.exists()) {
            fileFolder.delete();
        }

        File fileFolder2 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture2.jpg");
        if (fileFolder2.exists()) {
            fileFolder2.delete();
        }

        File fileFolder3 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture3.jpg");
        if (fileFolder3.exists()) {
            fileFolder3.delete();
        }

        File fileFolder4 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture4.jpg");
        if (fileFolder4.exists()) {
            fileFolder4.delete();
        }

        File fileFolder5 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemPicture5.jpg");
        if (fileFolder5.exists()) {
            fileFolder5.delete();
        }

        File fileFolder6 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/taskItemAudio.3gp");
        if (fileFolder6.exists()) {
            fileFolder6.delete();
        }

        File fileFolder7 = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/personPicture.jpg");
        if (fileFolder7.exists()) {
            fileFolder7.delete();
        }
    }

    @Override
    protected void onDestroy() {
        loadingDialog.dismiss();

        super.onDestroy();
    }

}
