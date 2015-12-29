package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.dao.AttendanceDao;
import com.nowui.fireelectronicsentry.model.Attendance;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WriteActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    private IntentFilter[] mWriteTagFilters;
    String[][] mTechLists;
    boolean isWrite = true;
    private String code;
    private Handler handler = new Handler();
    private MaterialDialog materialDialog;
    private Button photoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_write);
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

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // 写入标签权限
        IntentFilter writeFilter = new IntentFilter(
                NfcAdapter.ACTION_TECH_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { writeFilter };
        mTechLists = new String[][] {
                new String[] { MifareClassic.class.getName() },
                new String[] { NfcA.class.getName() } };// 允许扫描的标签类型
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();


        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                mWriteTagFilters, mTechLists);
    }

    // 写入模式时，才执行写入操作
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        if (isWrite == true && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage ndefMessage = getNoteAsNdef();
            if (ndefMessage != null) {
                boolean isSuccess = writeTag(getNoteAsNdef(), tag);

                if (isSuccess) {
                    handler.postDelayed(task, 1000);
                }
            } else {
                showToast("请输入您要写入标签的内容");
            }
        }
    }

    // 根据文本生成一个NdefRecord
    private NdefMessage getNoteAsNdef() {
        if (code.equals("")) {
            return null;
        } else {
            byte[] textBytes = new byte[0];
            try {
                textBytes = code.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // image/jpeg text/plain
            NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    "text/plain".getBytes(), new byte[] {}, textBytes);
            return new NdefMessage(new NdefRecord[] { textRecord });
        }

    }

    // 写入tag
    boolean writeTag(NdefMessage message, Tag tag) {

        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    showToast("tag不允许写入");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    showToast("文件大小超出容量");
                    return false;
                }

                ndef.writeNdefMessage(message);
                showToast("写入数据成功.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        showToast("格式化tag并且写入message");
                        return true;
                    } catch (IOException e) {
                        showToast("格式化tag失败.");
                        return false;
                    }
                } else {
                    showToast("Tag不支持NDEF");
                    return false;
                }
            }
        } catch (Exception e) {
            showToast("写入数据失败");
        }

        return false;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private Runnable task = new Runnable() {
        public void run() {
            Intent intent = new Intent();
            intent.putExtra("code", code);
            intent.setClass(WriteActivity.this, WriteCameraActivity.class);
            startActivityForResult(intent, Helper.CodeRequest);

            finish();
        }
    };

    private void showLoadingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

}
