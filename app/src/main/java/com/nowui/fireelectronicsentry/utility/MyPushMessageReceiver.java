package com.nowui.fireelectronicsentry.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.nowui.fireelectronicsentry.activity.MainActivity;

import java.io.IOException;
import java.util.List;

/**
 * Created by yongqiangzhong on 8/16/15.
 */
public class MyPushMessageReceiver extends PushMessageReceiver {
    private int count = 0;

    @Override
    public void onBind(Context context, int errorCode, String appid, String userId, String channelId, String requestId) {
        System.out.println("onBind---------------");

        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        System.out.println(responseString);

        if (errorCode == 0) {
            SharedPreferences setting = context.getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = setting.edit();
            editor.putString(Helper.KeyBaiduUserId, userId);
            editor.putString(Helper.KeyBaiduChannelId, channelId);
            editor.commit();
        }
    }

    @Override
    public void onUnbind(Context context, int i, String s) {
        System.out.println("onUnbind---------------");
    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {
        System.out.println("onSetTags---------------");
    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {
        System.out.println("onDelTags---------------");
    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {
        System.out.println("onListTags---------------");
    }

    @Override
    public void onMessage(Context context, String s, String s1) {
        System.out.println("onMessage---------------");
    }

    @Override
    public void onNotificationClicked(Context context, String s, String s1, String s2) {
        System.out.println("onNotificationClicked---------------");

        boolean isOpen = Helper.isAppOnForeground(context, "com.nowui.fireelectronicsentry");

        if (isOpen) {

        } else {
            Intent intent = new Intent();
            intent.setClass(context.getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }
    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {
        System.out.println("onNotificationArrived---------------");

        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(5000);

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getAssets().openFd("push.mp3");
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (count >= 1) {
                        count = 0;

                        mp.release();
                    } else {
                        count++;

                        mp.start();
                    }
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
