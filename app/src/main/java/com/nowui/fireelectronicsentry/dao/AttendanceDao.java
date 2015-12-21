package com.nowui.fireelectronicsentry.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.model.Attendance;
import com.nowui.fireelectronicsentry.utility.DbHelper;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

/**
 * Created by yongqiangzhong on 8/3/15.
 */
public class AttendanceDao {

    public static void create(DbHelper dbHelper) {
        //dbHelper.getWritableDatabase().execSQL("DROP TABLE attendance_info");

        StringBuffer stringBuffer = new StringBuffer("create table if not exists attendance_info (id integer primary key autoincrement");
        stringBuffer.append(", userID varchar");
        stringBuffer.append(", userName varchar");
        stringBuffer.append(", userPictureBase64 varchar");
        stringBuffer.append(", loginOrAttendance varchar");
        stringBuffer.append(", loginOrAttendanceStatus varchar");
        stringBuffer.append(")");
        dbHelper.getWritableDatabase().execSQL(stringBuffer.toString());
        dbHelper.getWritableDatabase().close();
    }

    public static void insert(DbHelper dbHelper, Attendance attendance) {
        dbHelper.getWritableDatabase().execSQL("insert into attendance_info (userID, userName, userPictureBase64, loginOrAttendance, loginOrAttendanceStatus) values ('" + attendance.getUserID() + "', '" + attendance.getUserName() + "', '" + attendance.getUserPictureBase64() + "', '" + attendance.getLoginOrAttendance() + "', '" + attendance.getLoginOrAttendanceStatus() + "')");
        dbHelper.getWritableDatabase().close();
    }

    public static void delete(DbHelper dbHelper, int id) {
        dbHelper.getWritableDatabase().execSQL("delete from attendance_info where id  = " + id);
        dbHelper.getWritableDatabase().close();
    }

    public static void post(Context inContext, AsyncHttpClient client, Attendance attendance, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        Context context = inContext.getApplicationContext();

        //String url = "http://oa.herigbit.com.cn/OA/LoginHistory.ashx";
        String url = Helper.WebUrl + "/LoginHistory.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/LoginHistory.ashx";
        }


        SharedPreferences setting = inContext.getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
        String departmentId = setting.getString(Helper.KeyDepartmentId, "");
        String baiduUserId = setting.getString(Helper.KeyBaiduUserId, "");
        String baiduChannelId = setting.getString(Helper.KeyBaiduChannelId, "");

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("deptId", Integer.valueOf(departmentId));
        dataObject.put("baiduUserId", baiduUserId);
        dataObject.put("baiduChannelId", baiduChannelId);
        dataObject.put("userID", attendance.getUserID());
        dataObject.put("userName", attendance.getUserName());
        dataObject.put("userPictureBase64", attendance.getUserPictureBase64());
        dataObject.put("loginOrAttendance", attendance.getLoginOrAttendance());
        dataObject.put("loginOrAttendanceStatus", attendance.getLoginOrAttendanceStatus());
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(context, url, stringEntity, "application/json", asyncHttpResponseHandler);
    }

}
