package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.TaskAbnormal;
import com.nowui.fireelectronicsentry.utility.Helper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class RemarkActivity extends AppCompatActivity {

    private EditText editText;
    private EditText addressEditText;
    private Button recordButton;
    private Button playButton;
    private Button submitButton;
    MediaRecorder recorder;
    MediaPlayer player;
    File audioFile;
    Boolean isRecord = false;
    private String radioString = "";
    private String audioName;
    private List<TaskAbnormal> taskAbnormalList;

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
        setContentView(R.layout.activity_remark);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        taskAbnormalList = (List<TaskAbnormal>) getIntent().getSerializableExtra("taskAbnormalList");

        TaskAbnormal t = new TaskAbnormal();
        t.setAbnormalid("0");
        t.setAbnormalname("其他");
        taskAbnormalList.add(t);

        for(TaskAbnormal taskAbnormal : taskAbnormalList) {
            System.out.println(taskAbnormal.getAbnormalname());
        }

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

        audioName = getIntent().getStringExtra("audioName");

        String taskType = getIntent().getStringExtra("taskType");
        if (Helper.isNullOrEmpty(taskType)) {
            taskType = "";
        }

        RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);


        for(TaskAbnormal taskAbnormal : taskAbnormalList) {
            final RadioButton radio = new RadioButton(this);
            radio.setText(taskAbnormal.getAbnormalname());
            radio.setTextSize((float) 14.0);
            radio.setChecked(false);
            radio.setBackgroundResource(R.drawable.md_radio_unselected_custom);
            radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioString = String.valueOf(((RadioButton)v).getText());
                }
            });
            group.addView(radio);
        }

        editText = (EditText) findViewById(R.id.editText);

        addressEditText = (EditText) findViewById(R.id.editText);

        String taskitemid = getIntent().getStringExtra("taskitemid");
        if (taskType.equals("3")) {
            addressEditText.setVisibility(View.VISIBLE);
        }

        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    player.stop();
                }

                if (isRecord) {
                    isRecord = false;
                    recordButton.setText(R.string.remark_button_start);
                    playButton.setEnabled(true);
                    submitButton.setEnabled(true);

                    recorder.stop();
                    recorder.release();
                } else {
                    isRecord = true;
                    recordButton.setText(R.string.remark_button_stop);
                    playButton.setEnabled(false);
                    submitButton.setEnabled(false);

                    recorder = new MediaRecorder();
                    //设置音频输入源
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    //设置音频的输出格式
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    //设置音频的编码格式
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    File path = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/");
                    path.mkdirs();

                    //设置音频输出位置
                    recorder.setOutputFile(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/" + audioName + ".3gp");
                    //开始录制音频
                    try {
                        recorder.prepare();
                    } catch (Exception e) {
                        System.out.println("MediaRecorder prepare error:" + e.toString());
                    }
                    recorder.start();
                }
            }
        });

        playButton = (Button) findViewById(R.id.playButton);
        playButton.setVisibility(View.INVISIBLE);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player = new MediaPlayer();

                try {
                    player.setDataSource(audioFile.getAbsolutePath());
                    player.prepare();
                } catch (Exception e) {
                    System.out.println("Exceptio in MediaPlayer.prepare():" + e.toString());
                }
                player.start();
            }
        });

        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //player.stop();

                String taskItemSelect = "";
                for(TaskAbnormal taskAbnormal : taskAbnormalList) {
                    if(taskAbnormal.getAbnormalname().equals(radioString)) {
                        taskItemSelect = taskAbnormal.getAbnormalid();
                    }
                }

                if (Helper.isNullOrEmpty(taskItemSelect)) {
                    Toast.makeText(getApplicationContext(), "必须选择类型!", Toast.LENGTH_SHORT).show();
                } else {
                    if (radioString == "其他") {
                        radioString = "";
                    } else {
                        radioString += ": ";
                    }

                    Intent intent = new Intent();
                    intent.putExtra("taskItemSelect", taskItemSelect);
                    intent.putExtra("taskItemDesc", radioString + editText.getText().toString());
                    intent.putExtra("taskItemAddress", radioString + addressEditText.getText().toString());
                    setResult(Helper.CodeResult, intent);
                    finish();
                }
            }
        });
    }

}
