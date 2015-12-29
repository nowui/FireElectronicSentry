package com.nowui.fireelectronicsentry.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.utility.Helper;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private Camera camera;
    private Camera.Parameters parameters = null;
    private Button loginButton;
    private String pictureName;

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
        setContentView(R.layout.activity_camera);
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

        pictureName = getIntent().getStringExtra("pictureName");

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.takePicture(null, null, new MyPictureCallback());
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            camera = Camera.open();
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
            //camera.stopPreview();
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
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                //Helper.saveToSDCard(data, 90, "taskItemPicture");
                Helper.saveToSDCard(data, 90, pictureName);

                camera.stopPreview();
                camera.release();

                Intent intent = new Intent();
                setResult(Helper.CodeResult, intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
