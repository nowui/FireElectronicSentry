package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class NoticeDetailActivity extends AppCompatActivity {

    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private ImageView imageView;
    private Button playButton;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_notice_detail);
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

        showLoadingDialog();

        String messageid = getIntent().getStringExtra("messageid");
        String userId = setting.getString(Helper.KeyUserId, "");

        String url = Helper.WebUrl + "/NotificationDetail.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/NotificationDetail.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("messageid", messageid);
        dataObject.put("userID", userId);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        client.post(NoticeDetailActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Notice notice = (Notice) JSON.parseObject(jsonMap.get("data").toString(), Notice.class);

                    TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
                    titleTextView.setText(notice.getMessageTitle());

                    TextView contentTextView = (TextView) findViewById(R.id.contentTextView);
                    contentTextView.setText(notice.getMessageContent());

                    playButton = (Button) findViewById(R.id.playButton);

                    if (Helper.isNullOrEmpty(notice.getMessageAudioBase64())) {
                        playButton.setVisibility(View.GONE);
                    } else {
                        playButton.setVisibility(View.VISIBLE);

                        decoderBase64File(notice.getMessageAudioBase64(), Environment.getExternalStorageDirectory() + "/FireElectronicSentry/notice.3gp");
                    }

                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (player != null) {
                                player.stop();
                            }

                            player = new MediaPlayer();

                            try {
                                player.setDataSource(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/notice.3gp");
                                player.prepare();
                            } catch (Exception e) {
                                System.out.println("Exceptio in MediaPlayer.prepare():" + e.toString());
                            }
                            player.start();
                        }
                    });

                    imageView = (ImageView) findViewById(R.id.imageView);
                    if (Helper.isNullOrEmpty(notice.getMessagePictureBase64())) {
                        imageView.setVisibility(View.GONE);
                    } else {
                        imageView.setImageBitmap(getBitmap(notice.getMessagePictureBase64()));
                    }
                }


                materialDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                materialDialog.dismiss();
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

    public static Bitmap getBitmap(String imgBase64Str){
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(imgBase64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void decoderBase64File(String base64Code, String savePath) {
        byte[] buffer =Base64.decode(base64Code, Base64.DEFAULT);
        try {
            FileOutputStream out = new FileOutputStream(savePath);
            out.write(buffer);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
