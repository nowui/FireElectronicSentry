package com.nowui.fireelectronicsentry.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.utility.Helper;

public class CameraListActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private Camera camera;
    private Camera.Parameters parameters = null;
    private Button loginButton;
    private String pictureName;
    private List<byte[]> pictureList = new ArrayList<byte[]>();
    private long clickTime;
    private RelativeLayout pictureRelativeLayout;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_camera_list);
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
                if ((System.currentTimeMillis() - clickTime) < 1000) {
                    return;
                }

                clickTime = System.currentTimeMillis();

                if (pictureList.size() >= 5) {
                    return;
                }

                if (camera != null) {
                    camera.takePicture(null, null, new MyPictureCallback());
                }
            }
        });

        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for(int i = 0; i < pictureList.size(); i++) {
                        Helper.saveToSDCard(pictureList.get(i), 90, pictureName + (i + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                //intent.putExtra("pictureList", (Serializable) pictureList);
                setResult(Helper.CodeRequest1, intent);
                finish();
            }
        });

        pictureRelativeLayout = (RelativeLayout) findViewById(R.id.pictureRelativeLayout);
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
                /*Helper.saveToSDCard(data, 90, pictureName);

                camera.stopPreview();
                camera.release();

                Intent intent = new Intent();
                setResult(Helper.CodeResult, intent);
                finish();*/

                pictureList.add(data);

                updatePictureList();

                System.out.println("----------");
                camera.startPreview();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updatePictureList() {
        pictureRelativeLayout.removeAllViews();

        int width = (this.getWindowManager().getDefaultDisplay().getWidth() -  10 * (5 + 1)) / 5;

        for(int i = 0; i < pictureList.size(); i++) {
            RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(width, width);
            imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            imageLayoutParams.leftMargin = 10 * (i + 1) + width * i;
            imageLayoutParams.bottomMargin = 10;

            ImageButton imageButton = new ImageButton(CameraListActivity.this);
            imageButton.setImageBitmap(Helper.getBitmap(pictureList.get(i), 90));
            pictureRelativeLayout.addView(imageButton, imageLayoutParams);

            RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(width, width);
            buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            buttonLayoutParams.leftMargin = 10 * (i + 1) + width * i;
            buttonLayoutParams.bottomMargin = width;

            final int index = i;

            Button button = new Button(CameraListActivity.this);
            button.setText("删除");
            button.setTextSize(12);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pictureList.remove(index);

                    updatePictureList();
                }
            });
            pictureRelativeLayout.addView(button, buttonLayoutParams);
        }
    }

}
