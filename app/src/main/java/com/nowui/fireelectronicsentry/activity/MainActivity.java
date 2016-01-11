package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.dao.AttendanceDao;
import com.nowui.fireelectronicsentry.dao.TaskDao;
import com.nowui.fireelectronicsentry.model.Attendance;
import com.nowui.fireelectronicsentry.model.Employee;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.model.Task;
import com.nowui.fireelectronicsentry.model.TaskAbnormal;
import com.nowui.fireelectronicsentry.model.TaskItem;
import com.nowui.fireelectronicsentry.model.TodayUnfinishedTask;
import com.nowui.fireelectronicsentry.utility.DbHelper;
import com.nowui.fireelectronicsentry.utility.Helper;
import com.nowui.fireelectronicsentry.utility.UpdateManager;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private long exitTime;
    private SurfaceView surfaceView;
    private Camera camera;
    private ImageView backgroundImageView;
    private ImageView logoImageView;
    private Button loginButton;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private final DbHelper dbHelper = new DbHelper(this, Helper.DatabaseName, null, Helper.DatabaseVersion);
    private MaterialDialog materialDialog;
    private MaterialSimpleListAdapter employeeAdapter;
    private List<Employee> employees = new ArrayList<Employee>();
    private ImageButton employeeButton;
    private Button reloadButton;
    private Employee user;
    private ImageView menuBackgroundImageView;
    private TextView menu00TextView;
    private TextView menu02TextView;
    private TextView menu03TextView;
    private ImageButton menu00ImageButton;
    private ImageButton menu01ImageButton;
    private ImageButton menu02ImageButton;
    private ImageButton menu03ImageButton;
    private ImageButton menu04ImageButton;
    private ImageButton menu05ImageButton;
    private ImageButton tab00ImageButton;
    private ImageButton tab01ImageButton;
    private ImageButton tab02ImageButton;
    private ImageButton tab03ImageButton;
    private ImageButton tab04ImageButton;
    private ImageButton emergencyImageButton;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private Attendance attendance;
    private Handler handler = new Handler();
    Toolbar toolbar;
    private String messageContent = "";
    private TextView weatherTextView;
    private List<TodayUnfinishedTask> todayUnfinishedTaskList = new ArrayList<TodayUnfinishedTask>();
    private Handler todayUnfinishedTaskHandler = new Handler();
    private List<Map<String, Object>> noticeList = new ArrayList<Map<String, Object>>();
    private Button choiceDepartmentButton;
    private List<Map<String, Object>> choiceDepartmentList = new ArrayList<Map<String, Object>>();
    private MaterialSimpleListAdapter departmentAdapter;

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

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        System.out.println(Helper.getScreenWidth(this));
        System.out.println(Helper.getScreenHeight(this));

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(setting.getString(Helper.KeyDepartmentName, getResources().getString(R.string.app_name)));
        setSupportActionBar(toolbar);

        weatherTextView = (TextView) findViewById(R.id.weatherTextView);

        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, "XiiyiGN62vCc9GZYeHy8sz2k");

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .build();

        AttendanceDao.create(dbHelper);
        TaskDao.create(dbHelper);

        loadWeather();

        deleteEmergency();

        String udid = setting.getString(Helper.KeyUDID, "");
        if(Helper.isNullOrEmpty(udid)) {
            udid = ((TelephonyManager) getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();

            editor.putString(Helper.KeyUDID, udid);
            editor.commit();
        }

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        backgroundImageView = (ImageView) findViewById(R.id.backgroundImageView);

        logoImageView = (ImageView) findViewById(R.id.logoImageView);

        menuBackgroundImageView = (ImageView) findViewById(R.id.menuBackgroundImageView);

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menuBackgroundImageViewLayoutParams = (RelativeLayout.LayoutParams) menuBackgroundImageView.getLayoutParams();
            menuBackgroundImageViewLayoutParams.topMargin = Helper.formatPix(this, 250);
            menuBackgroundImageViewLayoutParams.width = Helper.formatPix(this, 400);
            menuBackgroundImageViewLayoutParams.height = Helper.formatPix(this, 400);
            menuBackgroundImageView.setLayoutParams(menuBackgroundImageViewLayoutParams);
        }

        employeeButton = (ImageButton) findViewById(R.id.employeeButton);

        reloadButton = (Button) findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                String userType = setting.getString(Helper.KeyUserType, "");
                String departmentId = setting.getString(Helper.KeyDepartmentId, "");

                loadEmployee(userType, departmentId);
            }
        });

        menu00TextView = (TextView) findViewById(R.id.menu00TextView);

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu00TextViewLayoutParams = (RelativeLayout.LayoutParams) menu00TextView.getLayoutParams();
            menu00TextViewLayoutParams.leftMargin = Helper.formatPix(this, 550);
            menu00TextViewLayoutParams.topMargin = Helper.formatPix(this, 180);
            menu00TextView.setLayoutParams(menu00TextViewLayoutParams);
        }

        menu02TextView = (TextView) findViewById(R.id.menu02TextView);

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu02TextViewLayoutParams = (RelativeLayout.LayoutParams) menu02TextView.getLayoutParams();
            menu02TextViewLayoutParams.leftMargin = Helper.formatPix(this, 680);
            menu02TextViewLayoutParams.topMargin = Helper.formatPix(this, 370);
            menu02TextView.setLayoutParams(menu02TextViewLayoutParams);
        }

        menu03TextView = (TextView) findViewById(R.id.menu03TextView);

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu03TextViewLayoutParams = (RelativeLayout.LayoutParams) menu03TextView.getLayoutParams();
            menu03TextViewLayoutParams.leftMargin = Helper.formatPix(this, 180);
            menu03TextViewLayoutParams.topMargin = Helper.formatPix(this, 269);
            menu03TextView.setLayoutParams(menu03TextViewLayoutParams);
        }

        menu00ImageButton = (ImageButton) findViewById(R.id.menu00ImageButton);
        menu00ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("eventType", 23);
                intent.setClass(MainActivity.this, CalendarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu00ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu00ImageButton.getLayoutParams();
            menu00ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 394);
            menu00ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 153);
            menu00ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu00ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu00ImageButton.setLayoutParams(menu00ImageButtonLayoutParams);
        }

        menu01ImageButton = (ImageButton) findViewById(R.id.menu01ImageButton);
        menu01ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ApplyActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu01ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu01ImageButton.getLayoutParams();
            menu01ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 24);
            menu01ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 500);
            menu01ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu01ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu01ImageButton.setLayoutParams(menu01ImageButtonLayoutParams);
        }

        menu02ImageButton = (ImageButton) findViewById(R.id.menu02ImageButton);
        menu02ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, NoticeActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu02ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu02ImageButton.getLayoutParams();
            menu02ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 540);
            menu02ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 348);
            menu02ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu02ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu02ImageButton.setLayoutParams(menu02ImageButtonLayoutParams);
        }

        menu03ImageButton = (ImageButton) findViewById(R.id.menu03ImageButton);
        menu03ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("eventType", 1);
                intent.setClass(MainActivity.this, CalendarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu03ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu03ImageButton.getLayoutParams();
            menu03ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 32);
            menu03ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 243);
            menu03ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu03ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu03ImageButton.setLayoutParams(menu03ImageButtonLayoutParams);
        }

        menu04ImageButton = (ImageButton) findViewById(R.id.menu04ImageButton);
        menu04ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.email", "com.android.email.activity.Welcome"));
                startActivity(intent);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu04ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu04ImageButton.getLayoutParams();
            menu04ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 270);
            menu04ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 666);
            menu04ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu04ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu04ImageButton.setLayoutParams(menu04ImageButtonLayoutParams);
        }

        menu05ImageButton = (ImageButton) findViewById(R.id.menu05ImageButton);
        menu05ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("url", "http://218.242.145.23:85/FrameSet/Login.aspx");
                intent.setClass(MainActivity.this, CarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams menu05ImageButtonLayoutParams = (RelativeLayout.LayoutParams) menu05ImageButton.getLayoutParams();
            menu05ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 524);
            menu05ImageButtonLayoutParams.topMargin = Helper.formatPix(this, 500);
            menu05ImageButtonLayoutParams.width = Helper.formatPix(this, 177);
            menu05ImageButtonLayoutParams.height = Helper.formatPix(this, 150);
            menu05ImageButton.setLayoutParams(menu05ImageButtonLayoutParams);
        }

        tab00ImageButton = (ImageButton) findViewById(R.id.tab00ImageButton);
        tab00ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ApplyActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams tab00ImageButtonLayoutParams = (RelativeLayout.LayoutParams) tab00ImageButton.getLayoutParams();
            tab00ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 50);
            tab00ImageButtonLayoutParams.bottomMargin = Helper.formatPix(this, 100);
            tab00ImageButtonLayoutParams.width = Helper.formatPix(this, 90);
            tab00ImageButtonLayoutParams.height = Helper.formatPix(this, 132);
            tab00ImageButton.setLayoutParams(tab00ImageButtonLayoutParams);
        }

        tab01ImageButton = (ImageButton) findViewById(R.id.tab01ImageButton);
        tab01ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("eventType", 1);
                intent.setClass(MainActivity.this, CalendarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams tab01ImageButtonLayoutParams = (RelativeLayout.LayoutParams) tab01ImageButton.getLayoutParams();
            tab01ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 175);
            tab01ImageButtonLayoutParams.bottomMargin = Helper.formatPix(this, 100);
            tab01ImageButtonLayoutParams.width = Helper.formatPix(this, 90);
            tab01ImageButtonLayoutParams.height = Helper.formatPix(this, 132);
            tab01ImageButton.setLayoutParams(tab01ImageButtonLayoutParams);
        }

        tab02ImageButton = (ImageButton) findViewById(R.id.tab02ImageButton);
        tab02ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
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
                                logout();
                            }

                            @Override
                            public void onPositive(MaterialDialog dialog) {

                            }
                        })
                        .show();
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams tab02ImageButtonLayoutParams = (RelativeLayout.LayoutParams) tab02ImageButton.getLayoutParams();
            tab02ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 303);
            tab02ImageButtonLayoutParams.bottomMargin = Helper.formatPix(this, 100);
            tab02ImageButtonLayoutParams.width = Helper.formatPix(this, 112);
            tab02ImageButtonLayoutParams.height = Helper.formatPix(this, 141);
            tab02ImageButton.setLayoutParams(tab02ImageButtonLayoutParams);
        }

        tab03ImageButton = (ImageButton) findViewById(R.id.tab03ImageButton);
        tab03ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(getResources().getString(R.string.dialog_title_attendance))
                        .items(R.array.attendance_values)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Intent intent = new Intent();
                                intent.putExtra("loginOrAttendanceStatus", String.valueOf(which));
                                intent.setClass(MainActivity.this, AttendanceActivity.class);
                                startActivityForResult(intent, Helper.CodeRequest);
                            }
                        })
                        .show();
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams tab03ImageButtonLayoutParams = (RelativeLayout.LayoutParams) tab03ImageButton.getLayoutParams();
            tab03ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 454);
            tab03ImageButtonLayoutParams.bottomMargin = Helper.formatPix(this, 100);
            tab03ImageButtonLayoutParams.width = Helper.formatPix(this, 90);
            tab03ImageButtonLayoutParams.height = Helper.formatPix(this, 132);
            tab03ImageButton.setLayoutParams(tab03ImageButtonLayoutParams);
        }

        tab04ImageButton = (ImageButton) findViewById(R.id.tab04ImageButton);
        tab04ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(getResources().getString(R.string.dialog_title_setting))
                        .items(R.array.setting_values)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if(which == 2) {
                                    /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                                    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

                                    UpdateManager um = new UpdateManager(MainActivity.this);
                                    um.checkUpdate();*/

                                    Intent intent = new Intent();
                                    intent.putExtra("url", Helper.WebUrl + "/UpdatePage.html");
                                    intent.setClass(MainActivity.this, CarActivity.class);
                                    startActivityForResult(intent, Helper.CodeRequest);
                                } else {
                                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                                    integrator.setCaptureActivity(ScanActivity.class);
                                    integrator.setPrompt(getResources().getString(R.string.qr_code_prompt));
                                    integrator.setCameraId(0);
                                    integrator.setBeepEnabled(false);
                                    integrator.setOrientationLocked(false);
                                    integrator.initiateScan();
                                }
                            }
                        })
                        .show();
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams tab04ImageButtonLayoutParams = (RelativeLayout.LayoutParams) tab04ImageButton.getLayoutParams();
            tab04ImageButtonLayoutParams.leftMargin = Helper.formatPix(this, 580);
            tab04ImageButtonLayoutParams.bottomMargin = Helper.formatPix(this, 100);
            tab04ImageButtonLayoutParams.width = Helper.formatPix(this, 90);
            tab04ImageButtonLayoutParams.height = Helper.formatPix(this, 132);
            tab04ImageButton.setLayoutParams(tab04ImageButtonLayoutParams);
        }

        emergencyImageButton = (ImageButton) findViewById(R.id.emergencyImageButton);
        emergencyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog();
            }
        });

        if(Helper.checkCameraFacing(1)) {
            SurfaceHolder holder = surfaceView.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.setKeepScreenOn(true);
            holder.addCallback(this);
        }

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Helper.checkCameraFacing(1)) {
                    try {
                        if (user == null) {
                            Toast.makeText(getApplicationContext(), R.string.toast_employee, Toast.LENGTH_SHORT).show();
                        } else {
                            if (camera != null) {
                                camera.takePicture(null, null, new MyPictureCallback());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    chekLogin();
                }
            }
        });

        if (Helper.getScreenWidth(this) < 720) {
            RelativeLayout.LayoutParams loginButtonLayoutParams = (RelativeLayout.LayoutParams) loginButton.getLayoutParams();
            loginButtonLayoutParams.width = Helper.formatPix(this, 430);
            loginButtonLayoutParams.height = Helper.formatPix(this, 120);
            loginButton.setLayoutParams(loginButtonLayoutParams);
        }


        String choiceDepartmentName = setting.getString(Helper.KeyChoiceDepartmentName, "");

        choiceDepartmentButton = (Button) findViewById(R.id.choiceDepartmentButton);
        choiceDepartmentButton.setText(choiceDepartmentName);
        choiceDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialDialog.Builder(MainActivity.this)
                        .title("选择部门")
                        .adapter(new DepartmentAdapter(MainActivity.this), new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                MaterialSimpleListItem item = departmentAdapter.getItem(which);

                                Map<String, Object> map = choiceDepartmentList.get(which);

                                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putString(Helper.KeyChoiceDepartmentId, map.get("departmentId").toString());
                                editor.putString(Helper.KeyChoiceDepartmentName, map.get("departmentName").toString());
                                editor.commit();

                                choiceDepartmentButton.setText(map.get("departmentName").toString());

                                loadMenuCount();

                                dialog.hide();
                            }
                        }).show();

                /*new MaterialDialog.Builder(MainActivity.this)
                        .title("部门选择")
                        .adapter(new DepartmentAdapter(MainActivity.this), new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                Map<String, Object> map = choiceDepartmentList.get(which);

                                System.out.print("KeyChoiceDepartmentId:" + map.get("departmentId").toString());

                                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putString(Helper.KeyChoiceDepartmentId, map.get("departmentId").toString());
                                editor.commit();

                                //dialog.dismiss();
                            }
                        }).show();*/
            }
        });

        /*dbHelper.getWritableDatabase().execSQL("create table if not exists system_info (id int primary key, departmentId varchar)");

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("select departmentId from system_info ", null);
        if(cursor.moveToFirst()) {
            departmentId = cursor.getString(0);
        } else {
            dbHelper.getWritableDatabase().execSQL("insert into system_info (departmentId) values ('')");
        }*/

        String userType = setting.getString(Helper.KeyUserType, "");
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");

        //departmentId = "";

        if(Helper.isNullOrEmpty(departmentId)) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.setPrompt(getResources().getString(R.string.qr_code_prompt));
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        } else {
            //showLoadingDialog();

            /**/


            String choiceDepartmentId = setting.getString(Helper.KeyChoiceDepartmentId, "");
            if(Helper.isNullOrEmpty(choiceDepartmentId)) {
                editor.putString(Helper.KeyChoiceDepartmentId, departmentId);
                editor.commit();
            }

            loadEmployee(userType, departmentId);



            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

            UpdateManager um = new UpdateManager(MainActivity.this);
            um.checkUpdate();
        }

        /*Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> mAllApps = getPackageManager().queryIntentActivities(mainIntent, 0);

        for(ResolveInfo info : mAllApps) {
            System.out.println(info.activityInfo.packageName + "-" + info.activityInfo.name);
        }*/

        //checkTodayUnfinishedTaskCount();

        loginSuccess();
    }

    private void logout() {
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

        AttendanceDao.post(MainActivity.this, client, attendance, new AsyncHttpResponseHandler() {
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

    private void loadEmployee(final String userType, final String departmentId) {
        System.out.println("loadEmployee");

        employees = new ArrayList<Employee>();
        user = null;

        int type = 0;

        if (! Helper.isNullOrEmpty(userType)) {
            type = Integer.valueOf(userType);
        }

        //showLoadingDialog();

        //String url = "http://oa.herigbit.com.cn/OA/DepartmentPersonList.ashx";
        String url = Helper.WebUrl + "/DepartmentPersonList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/DepartmentPersonList.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("userType", type);
        dataObject.put("deptid", Integer.valueOf(departmentId));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                    SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);

                    String choiceDepartmentName = setting.getString(Helper.KeyChoiceDepartmentName, "");

                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString(Helper.KeyDepartmentName, dataMap.get("departmentName").toString());
                    if (Helper.isNullOrEmpty(choiceDepartmentName)) {
                        editor.putString(Helper.KeyChoiceDepartmentName, dataMap.get("departmentName").toString());
                        choiceDepartmentButton.setText(dataMap.get("departmentName").toString());
                    }
                    String departmentLogoUrl = "";
                    if (! Helper.isNullOrEmpty(dataMap.get("departmentLogoUrl"))) {
                        departmentLogoUrl = dataMap.get("departmentLogoUrl").toString();
                    }
                    editor.putString(Helper.KeyLogo, departmentLogoUrl);
                    editor.commit();

                    toolbar.setTitle(dataMap.get("departmentName").toString());

                    employees = JSON.parseArray(dataMap.get("personList").toString(), Employee.class);

                    employeeAdapter = new MaterialSimpleListAdapter(MainActivity.this);

                    int count = 0;
                    for (Employee employee : employees) {
                        employeeAdapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                                .content(employee.getName())
                                .build());

                        if (count == 0) {
                            user = employee;
                            if (user.getPictureUrl().equals(Helper.WebUrl)) {
                                employeeButton.setImageDrawable(getResources().getDrawable(R.drawable.header));
                            } else {
                                imageLoader.displayImage(user.getPictureUrl(), employeeButton, options);
                            }

                            editor.putString(Helper.KeyUserId, user.getId().toString());
                            editor.putString(Helper.KeyUserName, user.getName());
                            editor.putString(Helper.KeyUserType, user.getUserType());
                            editor.commit();

                            employeeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(getResources().getString(R.string.dialog_title_employee))
                                            .adapter(new EmployeeAdapter(MainActivity.this), new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                    MaterialSimpleListItem item = employeeAdapter.getItem(which);

                                                    user = employees.get(which);
                                                    if (user.getPictureUrl().equals(Helper.WebUrl)) {
                                                        employeeButton.setImageDrawable(getResources().getDrawable(R.drawable.header));
                                                    } else {
                                                        imageLoader.displayImage(user.getPictureUrl(), employeeButton, options);
                                                    }

                                                    SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = setting.edit();
                                                    editor.putString(Helper.KeyUserId, user.getId().toString());
                                                    editor.putString(Helper.KeyUserName, user.getName());
                                                    editor.putString(Helper.KeyUserType, user.getUserType());
                                                    editor.commit();

                                                    dialog.hide();
                                                }
                                            }).show();
                                }
                            });
                        }

                        count++;
                    }

                    reloadButton.setVisibility(View.INVISIBLE);
                } else {
                    reloadButton.setVisibility(View.VISIBLE);
                }

                //materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //materialDialog.hide();

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();

                reloadButton.setVisibility(View.VISIBLE);
            }
        });


    }

    private void loadMenuCount() {
        if(loginButton.getVisibility() == View.VISIBLE) {
            return;
        }

        System.out.println("loadMenuCount");

        menu00TextView.setVisibility(View.INVISIBLE);
        menu02TextView.setVisibility(View.INVISIBLE);
        menu03TextView.setVisibility(View.INVISIBLE);

        //String url = "http://oa.herigbit.com.cn/OA/DepartmentPersonList.ashx";
        String url = Helper.WebUrl + "/MobileTaskNotificationStaticService.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/MobileTaskNotificationStaticService.ashx";
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();

        String userId = setting.getString(Helper.KeyUserId, "");
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
        dataObject.put("userID", Integer.valueOf(userId));
        dataObject.put("deptID", Integer.valueOf(deptID));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("++++++++++++++++");
        System.out.println(jsonObject.toString());

        client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                    String myTaskCount = "0";
                    String scheduleTaskCount = "0";

                    SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                    String usetType = setting.getString(Helper.KeyUserType, "");
                    if (usetType.equals("3")) {
                        myTaskCount = dataMap.get("myTaskCount").toString();
                        scheduleTaskCount = dataMap.get("scheduleTaskCount").toString();
                    } else {
                        myTaskCount = dataMap.get("myTaskAllCount").toString();
                        scheduleTaskCount = dataMap.get("scheduleTaskAllCount").toString();
                    }

                    String unreadCount = dataMap.get("unreadCount").toString();
                    //if (myTaskCount != "0") {
                        menu00TextView.setVisibility(View.VISIBLE);
                        menu00TextView.setText(scheduleTaskCount);
                    //}

                    //if (scheduleTaskCount != "0") {
                        menu02TextView.setVisibility(View.VISIBLE);
                        menu02TextView.setText(unreadCount);
                    //}

                    //if (unreadCount != "0") {
                        menu03TextView.setVisibility(View.VISIBLE);
                        menu03TextView.setText(myTaskCount);
                    //}
                }

                if (materialDialog != null) {
                    materialDialog.hide();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

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

    private void showMessageDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            camera = Camera.open(1);
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
            camera.stopPreview();
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
        public void onPictureTaken(byte[] data, final Camera camera) {
            try {
                Helper.saveToSDCard(data, -90, "userPictureBase64");

                camera.stopPreview();

                SurfaceHolder holder = surfaceView.getHolder();
                holder.removeCallback(MainActivity.this);

                chekLogin();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void chekLogin() {
        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        String userId = setting.getString(Helper.KeyUserId, "");
        String userName = setting.getString(Helper.KeyUserName, "");

        attendance = new Attendance();

        attendance.setUserID(Integer.valueOf(userId));
        attendance.setUserName(userName);
        //attendance.setUserPictureBase64(Helper.encodeBase64(data));
        attendance.setUserPictureBase64(Helper.encodeBase64ForFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/userPictureBase64.jpg"));
        attendance.setLoginOrAttendance(0);
        attendance.setLoginOrAttendanceStatus(0);

        showLoadingDialog();

        //AttendanceDao.insert(dbHelper, attendance);

        AttendanceDao.post(MainActivity.this, client, attendance, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    //上传本地数据
                    //handler.post(task);

                    if (Helper.isAdmin) {
                        camera.release();

                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, AdminActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);

                        finish();
                    } else {
                        loginSuccess();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), jsonMap.get("message").toString(), Toast.LENGTH_SHORT).show();

                    camera.startPreview();
                }

                materialDialog.hide();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                AttendanceDao.insert(dbHelper, attendance);


                if (Helper.isAdmin) {
                    camera.release();

                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, AdminActivity.class);
                    startActivityForResult(intent, Helper.CodeRequest);

                    finish();
                } else {
                    loginSuccess();
                }

                materialDialog.hide();
            }
        });
    }

    private void loginSuccess() {
        if (Helper.checkCameraFacing(1)) {
            if(camera != null) {
                camera.release();
            }
        }

        //setting.getString(Helper.KeyDepartmentName, "")

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        imageLoader.displayImage(Helper.isAdmin ? Helper.AdminWebUrl : Helper.WebUrl + setting.getString(Helper.KeyLogo, ""), logoImageView, options);
        logoImageView.setVisibility(View.VISIBLE);

        backgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.main_bg2));
        employeeButton.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
        menuBackgroundImageView.setVisibility(View.VISIBLE);
        menu00ImageButton.setVisibility(View.VISIBLE);
        menu01ImageButton.setVisibility(View.VISIBLE);
        menu02ImageButton.setVisibility(View.VISIBLE);
        menu03ImageButton.setVisibility(View.VISIBLE);
        menu04ImageButton.setVisibility(View.VISIBLE);
        menu05ImageButton.setVisibility(View.VISIBLE);
        tab00ImageButton.setVisibility(View.VISIBLE);
        tab01ImageButton.setVisibility(View.VISIBLE);
        tab02ImageButton.setVisibility(View.VISIBLE);
        tab03ImageButton.setVisibility(View.VISIBLE);
        tab04ImageButton.setVisibility(View.VISIBLE);
        emergencyImageButton.setVisibility(View.VISIBLE);

        loadMenuCount();

        String userId = setting.getString(Helper.KeyUserId, "");
        String deptID = setting.getString(Helper.KeyDepartmentId, "");
        String deptName = setting.getString(Helper.KeyDepartmentName, "");
        String usetType = setting.getString(Helper.KeyUserType, "");

        if (usetType.equals("1")) {
            choiceDepartmentButton.setVisibility(View.VISIBLE);
            weatherTextView.setVisibility(View.INVISIBLE);

            loadDepartment(userId, deptID);

            toolbar.setTitle("防火监督员");
        } else if(usetType.equals("2")) {
            toolbar.setTitle("企业管理员-" + Helper.substring(deptName, 6));
        }

        //loadNotice();

        //上传本地数据
        handler.post(task);
    }

    private void relogin() {
        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        String userType = setting.getString(Helper.KeyUserType, "");
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");

        loadEmployee(userType, departmentId);

        SurfaceHolder holder = surfaceView.getHolder();

        try {
            camera = Camera.open(1);
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);

            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logoImageView.setVisibility(View.INVISIBLE);
        backgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.main_bg));
        employeeButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        menuBackgroundImageView.setVisibility(View.INVISIBLE);
        menu00ImageButton.setVisibility(View.INVISIBLE);
        menu01ImageButton.setVisibility(View.INVISIBLE);
        menu02ImageButton.setVisibility(View.INVISIBLE);
        menu03ImageButton.setVisibility(View.INVISIBLE);
        menu04ImageButton.setVisibility(View.INVISIBLE);
        menu05ImageButton.setVisibility(View.INVISIBLE);
        tab00ImageButton.setVisibility(View.INVISIBLE);
        tab01ImageButton.setVisibility(View.INVISIBLE);
        tab02ImageButton.setVisibility(View.INVISIBLE);
        tab03ImageButton.setVisibility(View.INVISIBLE);
        tab04ImageButton.setVisibility(View.INVISIBLE);
        emergencyImageButton.setVisibility(View.INVISIBLE);

        menu00TextView.setVisibility(View.INVISIBLE);
        menu02TextView.setVisibility(View.INVISIBLE);
        menu03TextView.setVisibility(View.INVISIBLE);

        choiceDepartmentButton.setVisibility(View.INVISIBLE);
        weatherTextView.setVisibility(View.VISIBLE);
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

                try {
                    if(code.contains("DeptID{")) {
                        Map<String, Object> jsonMap = JSON.parseObject(code.replace("DeptID{", "{"), new TypeReference<Map<String, Object>>() {
                        });

                        String departmentId = jsonMap.get("DeptID").toString();
                        String userType = jsonMap.get("userType").toString();

                        if (Helper.isNullOrEmpty(departmentId)) {
                            departmentId = "";
                        }

                        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);

                        String departID = setting.getString(Helper.KeyDepartmentId, "");

                        SharedPreferences.Editor editor = setting.edit();
                        editor.putString(Helper.KeyDepartmentId, departmentId);
                        editor.putString(Helper.KeyChoiceDepartmentId, departmentId);
                        editor.putString(Helper.KeyUserType, userType);
                        editor.commit();

                        if (! Helper.isNullOrEmpty(departID)) {
                            logout();
                        }

                        //dbHelper.getWritableDatabase().execSQL("update system_info set departmentId = '" + departmentId + "'");

                        loadEmployee(userType, departmentId);
                    } else if(code.contains("Patrol{")) {
                        Intent intent = new Intent();
                        intent.putExtra("code", code.replace("Patrol{", "{"));
                        intent.setClass(MainActivity.this, WriteActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "二维码格式不正确", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (Helper.isAdmin) {

            } else {
                loadMenuCount();
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
                    messageContent = taskItemDesc;
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

    private class EmployeeAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public EmployeeAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return employees.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            EmployeeViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_employee, parent, false);

                holder = new EmployeeViewHolder();
                holder.picture = (ImageView) convertView.findViewById(R.id.pictureImageView);
                holder.name = (TextView) convertView.findViewById(R.id.nameTextView);
                convertView.setTag(holder);
            } else {
                holder = (EmployeeViewHolder) convertView.getTag();
            }

            Employee employee = employees.get(position);

            holder.name.setText(employee.getName());

            if (employee.getPictureUrl().equals(Helper.WebUrl)) {
                holder.picture.setImageDrawable(getResources().getDrawable(R.drawable.header));
            } else {
                imageLoader.displayImage(employee.getPictureUrl(), holder.picture, options);
            }

            return convertView;
        }
    }

    private class EmployeeViewHolder {
        public ImageView picture;
        public TextView name;
    }

    private Runnable task = new Runnable() {
        public void run() {
            System.out.println("==============================");
            uploadAttendance();
        }
    };

    private void uploadAttendance() {
        Cursor attendanceCursor = dbHelper.getWritableDatabase().rawQuery("select * from attendance_info", null);
        if(attendanceCursor.moveToFirst()) {
            System.out.println(attendanceCursor.getString(attendanceCursor.getColumnIndex("id")));
            System.out.println(attendanceCursor.getString(attendanceCursor.getColumnIndex("userName")));

            Attendance object = new Attendance();

            final int id = attendanceCursor.getInt(attendanceCursor.getColumnIndex("id"));
            object.setUserID(Integer.valueOf(attendanceCursor.getString(attendanceCursor.getColumnIndex("userID"))));
            object.setUserName(attendanceCursor.getString(attendanceCursor.getColumnIndex("userName")));
            object.setUserPictureBase64(attendanceCursor.getString(attendanceCursor.getColumnIndex("userPictureBase64")));
            object.setLoginOrAttendance(Integer.valueOf(attendanceCursor.getString(attendanceCursor.getColumnIndex("loginOrAttendance"))));
            object.setLoginOrAttendanceStatus(Integer.valueOf(attendanceCursor.getString(attendanceCursor.getColumnIndex("loginOrAttendanceStatus"))));

            AttendanceDao.post(MainActivity.this, client, object, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    AttendanceDao.delete(dbHelper, id);

                    Cursor checkCursor = dbHelper.getWritableDatabase().rawQuery("select * from attendance_info where id = " + id, null);
                    if(!checkCursor.moveToFirst()) {
                        uploadAttendance();
                    } else {
                        handler.postDelayed(task, Helper.PostDelay);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    handler.postDelayed(task, Helper.PostDelay);
                }
            });

            if (!attendanceCursor.isClosed()) {
                attendanceCursor.close();
            }

        } else {

            if (!attendanceCursor.isClosed()) {
                attendanceCursor.close();
            }

            uploadTask();
        }
    }

    private void uploadTask() {
        Cursor taskCursor = dbHelper.getWritableDatabase().rawQuery("select * from task_info", null);
        if(taskCursor.moveToFirst()) {
            System.out.println("==============================");
            System.out.println(taskCursor.getCount());

            final int id = taskCursor.getInt(taskCursor.getColumnIndex("id"));
            String userId = taskCursor.getString(taskCursor.getColumnIndex("personID"));
            String userName = taskCursor.getString(taskCursor.getColumnIndex("personName"));
            String taskid = taskCursor.getString(taskCursor.getColumnIndex("taskID"));
            String taskname = taskCursor.getString(taskCursor.getColumnIndex("taskName"));

            TaskItem taskItem = new TaskItem();
            taskItem.setPatrolTaskExecutionId(taskCursor.getString(taskCursor.getColumnIndex("patrolTaskExecutionId")));
            taskItem.setTaskitemid(taskCursor.getString(taskCursor.getColumnIndex("taskItemID")));
            taskItem.setTaskitemname(taskCursor.getString(taskCursor.getColumnIndex("taskItemName")));
            taskItem.setTaskitemstatus(taskCursor.getString(taskCursor.getColumnIndex("taskItemStatus")));
            taskItem.setTaskItemPicture(taskCursor.getString(taskCursor.getColumnIndex("taskItemPicture")));
            taskItem.setTaskItemPicture2(taskCursor.getString(taskCursor.getColumnIndex("taskItemPicture2")));
            taskItem.setTaskItemPicture3(taskCursor.getString(taskCursor.getColumnIndex("taskItemPicture3")));
            taskItem.setTaskItemPicture4(taskCursor.getString(taskCursor.getColumnIndex("taskItemPicture4")));
            taskItem.setTaskItemPicture5(taskCursor.getString(taskCursor.getColumnIndex("taskItemPicture5")));
            taskItem.setTaskItemDesc(taskCursor.getString(taskCursor.getColumnIndex("taskItemDesc")));
            taskItem.setTaskItemAudio(taskCursor.getString(taskCursor.getColumnIndex("taskItemAudio")));

            TaskDao.post(MainActivity.this, client, userId, userName, "", taskid, taskname, taskItem, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("++++++++++++++++++++++++++++++");

                    TaskDao.delete(dbHelper, id);

                    Cursor checkCursor = dbHelper.getWritableDatabase().rawQuery("select * from task_info where id = " + id, null);
                    if (!checkCursor.moveToFirst()) {
                        uploadTask();
                    } else {
                        handler.postDelayed(task, Helper.PostDelay);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    System.out.println("-------------------------------");

                    handler.postDelayed(task, Helper.PostDelay);
                }
            });

            if (!taskCursor.isClosed()) {
                taskCursor.close();
            }
        } else {

            if (!taskCursor.isClosed()) {
                taskCursor.close();
            }

            handler.postDelayed(task, Helper.PostDelay);
        }
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

                            client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    System.out.println(new String(responseBody));

                                    Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                                    });

                                    if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                                        //Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");

                                        deleteEmergency();

                                    } else {

                                    }

                                    dialog.dismiss();

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
                            Toast.makeText(MainActivity.this, "请拍照再提交应急事件", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("pictureName", "emergencyPicture");
                        intent.setClass(MainActivity.this, CameraActivity.class);
                        startActivityForResult(intent, Helper.CodeRequest);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent();
                        intent.putExtra("taskAbnormalList", (Serializable) new ArrayList<TaskAbnormal>());
                        intent.putExtra("audioName", "emergencyAudio");
                        intent.setClass(MainActivity.this, RemarkActivity.class);
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

    private void loadWeather() {
        AsyncHttpClient weatherClient = new AsyncHttpClient();
        weatherClient.addHeader("apikey", "297e48e88d49aa57c9f6f062c01cf138");

        RequestParams params = new RequestParams();
        params.put("cityname", "上海");

        weatherClient.get(MainActivity.this, "http://apis.baidu.com/apistore/weatherservice/cityname", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                System.out.println(new String(responseBody));

                try {

                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("retData");
                    editor.putString(Helper.KeyWeather, dataMap.get("temp").toString() + "℃ " + dataMap.get("weather").toString() + "  ");
                    editor.commit();
                } catch (Exception e) {

                }


                String weather = setting.getString(Helper.KeyWeather, "");
                weatherTextView.setText(weather);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //System.out.println(new String(responseBody));
            }
        });
    }

    private void checkTodayUnfinishedTaskCount() {
        String url = Helper.WebUrl + "/TodayUnfinishedTaskCountService.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/TodayUnfinishedTaskCountService.ashx";
        }

        SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptID", Integer.valueOf(departmentId));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("todayUnfinishedTaskList");

                    for (Map<String, Object> map : dataList) {
                        TodayUnfinishedTask todayUnfinishedTask = new TodayUnfinishedTask();
                        todayUnfinishedTask.setPatrolParentID(Integer.valueOf(map.get("patrolParentID").toString()));
                        todayUnfinishedTask.setPatrolParentItemName(map.get("patrolParentItemName").toString());

                        List<Task> taskList = new ArrayList<Task>();
                        List<Map<String, Object>> detailList = (List<Map<String, Object>>) map.get("todayUnfinishedTask");
                        for (Map<String, Object> detailMap : detailList) {
                            Task task = new Task();
                            task.setTaskid(detailMap.get("taskid").toString());
                            task.setTaskname(detailMap.get("taskname").toString());
                            task.setTaskstart(detailMap.get("taskstart").toString());
                            task.setTaskend(detailMap.get("taskend").toString());
                            taskList.add(task);
                        }
                        todayUnfinishedTask.setTaskList(taskList);

                        todayUnfinishedTaskList.add(todayUnfinishedTask);
                    }

                    didTodayUnfinishedTask();
                    /*todayUnfinishedTaskHandler.post(todayUnfinishedTaskListTask);*/
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private Runnable todayUnfinishedTaskListTask = new Runnable() {
        public void run() {
            System.out.println("==============================");
            didTodayUnfinishedTask();
        }
    };

    private void didTodayUnfinishedTask() {
        for (TodayUnfinishedTask todayUnfinishedTask : todayUnfinishedTaskList) {
            for(Task task : todayUnfinishedTask.getTaskList()) {
                System.out.println(task.getTaskstart());
            }
        }
    }

    private void loadNotice() {
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
        dataObject.put("pagenum", 1);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = JSON.parseObject(jsonMap.get("data").toString(), new TypeReference<Map<String, Object>>() {});

                    List<Notice> notices = JSON.parseArray(dataMap.get("messageList").toString(), Notice.class);

                    for (Notice notice : notices) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("id", notice.getMessageid());
                        map.put("title", notice.getMessageTitle());
                        map.put("content", notice.getMessageContent());
                        noticeList.add(map);
                    }


                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void loadDepartment(final String userId, final String departmentId) {
        System.out.println("loadDepartment");

        //String url = "http://oa.herigbit.com.cn/OA/DepartmentPersonList.ashx";
        String url = Helper.WebUrl + "/DepartmentList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/DepartmentList.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptID", Integer.valueOf(departmentId));
        dataObject.put("userID", Integer.valueOf(userId));
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(MainActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {
                });

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {
                    Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("DeptList");

                    choiceDepartmentList = new ArrayList<Map<String, Object>>();

                    departmentAdapter = new MaterialSimpleListAdapter(MainActivity.this);

                    for(Map<String, Object> department : dataList) {
                        departmentAdapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                                .content(department.get("DeptName").toString())
                                .build());

                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("departmentId", department.get("DeptID"));
                        map.put("departmentName", department.get("DeptName"));
                        choiceDepartmentList.add(map);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }



    private class DepartmentAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public DepartmentAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return choiceDepartmentList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            DepartmentViewHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_department, parent, false);

                holder = new DepartmentViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.nameTextView);
                convertView.setTag(holder);
            } else {
                holder = (DepartmentViewHolder) convertView.getTag();
            }

            Map<String, Object> map = choiceDepartmentList.get(position);

            holder.name.setText(map.get("departmentName").toString());

            return convertView;
        }
    }

    private class DepartmentViewHolder {
        public TextView name;
    }

}
