package com.nowui.fireelectronicsentry.dao;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.model.TaskItem;
import com.nowui.fireelectronicsentry.utility.DbHelper;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

/**
 * Created by yongqiangzhong on 8/4/15.
 */
public class TaskDao {

    public static void create(DbHelper dbHelper) {
        //dbHelper.getWritableDatabase().execSQL("DROP TABLE task_info");

        StringBuffer stringBuffer = new StringBuffer("create table if not exists task_info (id integer primary key autoincrement");
        stringBuffer.append(", personID varchar");
        stringBuffer.append(", personName varchar");
        stringBuffer.append(", personPicture varchar");
        stringBuffer.append(", taskID varchar");
        stringBuffer.append(", taskName varchar");
        stringBuffer.append(", patrolTaskExecutionId varchar");
        stringBuffer.append(", taskItemID varchar");
        stringBuffer.append(", taskItemName varchar");
        stringBuffer.append(", taskItemStatus varchar");
        stringBuffer.append(", taskItemPicture varchar");
        stringBuffer.append(", taskItemPicture2 varchar");
        stringBuffer.append(", taskItemPicture3 varchar");
        stringBuffer.append(", taskItemPicture4 varchar");
        stringBuffer.append(", taskItemPicture5 varchar");
        stringBuffer.append(", taskItemDesc varchar");
        stringBuffer.append(", taskItemAudio varchar");
        stringBuffer.append(")");
        dbHelper.getWritableDatabase().execSQL(stringBuffer.toString());
    }

    public static void insert(DbHelper dbHelper, String personID, String personName, String personPicture, String taskID, String taskName, TaskItem taskItem) {

        StringBuffer stringBuffer = new StringBuffer("insert into task_info ( ");
        stringBuffer.append("personID");
        stringBuffer.append(", personName");
        stringBuffer.append(", personPicture");
        stringBuffer.append(", taskID");
        stringBuffer.append(", taskName");
        stringBuffer.append(", patrolTaskExecutionId");
        stringBuffer.append(", taskItemID");
        stringBuffer.append(", taskItemName");
        stringBuffer.append(", taskItemStatus");
        stringBuffer.append(", taskItemPicture");
        stringBuffer.append(", taskItemPicture2");
        stringBuffer.append(", taskItemPicture3");
        stringBuffer.append(", taskItemPicture4");
        stringBuffer.append(", taskItemPicture5");
        stringBuffer.append(", taskItemDesc");
        stringBuffer.append(", taskItemAudio");
        stringBuffer.append(") values (");
        stringBuffer.append("'" + personID + "'");
        stringBuffer.append(", '" + personName + "'");
        stringBuffer.append(", '" + personPicture + "'");
        stringBuffer.append(", '" + taskID + "'");
        stringBuffer.append(", '" + taskName + "'");
        stringBuffer.append(", '" + taskItem.getPatrolTaskExecutionId() + "'");
        stringBuffer.append(", '" + taskItem.getTaskitemid() + "'");
        stringBuffer.append(", '" + taskItem.getTaskitemname() + "'");
        stringBuffer.append(", '" + taskItem.getTaskitemstatus() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemPicture() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemPicture2() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemPicture3() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemPicture4() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemPicture5() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemDesc() + "'");
        stringBuffer.append(", '" + taskItem.getTaskItemAudio() + "'");
        stringBuffer.append(") ");

        dbHelper.getWritableDatabase().execSQL(stringBuffer.toString());
    }

    public static void delete(DbHelper dbHelper, int id) {
        dbHelper.getWritableDatabase().execSQL("delete from task_info where id  = " + id);
    }

    public static void post(Context inContext, AsyncHttpClient client, String personID, String personName, String personPicture, String taskID, String taskName, TaskItem taskItem, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        Context context = inContext.getApplicationContext();

        String url = Helper.WebUrl + "/PatrolTaskExecution.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/PatrolTaskExecution.ashx";
        }

        JSONObject jsonObject = new JSONObject();
        String datetime = Helper.formatDateTime();
        jsonObject.put("key", Helper.MD5("FireElectronicSentry" + datetime));
        jsonObject.put("datetime", datetime);
        JSONObject dataObject = new JSONObject();
        dataObject.put("personID", Integer.valueOf(personID));
        dataObject.put("personName", personName);
        dataObject.put("personPicture", personPicture);
        dataObject.put("taskID", Integer.valueOf(taskID));
        dataObject.put("taskName", taskName);
        dataObject.put("patrolTaskExecutionId", Integer.valueOf(taskItem.getPatrolTaskExecutionId()));
        dataObject.put("taskItemID", Integer.valueOf(taskItem.getTaskitemid()));
        dataObject.put("taskItemName", taskItem.getTaskitemname());
        dataObject.put("taskItemStatus", Integer.valueOf(taskItem.getTaskitemstatus()));
        dataObject.put("taskItemPicture", taskItem.getTaskItemPicture());
        dataObject.put("taskItemPicture2", taskItem.getTaskItemPicture2());
        dataObject.put("taskItemPicture3", taskItem.getTaskItemPicture3());
        dataObject.put("taskItemPicture4", taskItem.getTaskItemPicture4());
        dataObject.put("taskItemPicture5", taskItem.getTaskItemPicture5());
        dataObject.put("taskItemDesc", taskItem.getTaskItemDesc());
        dataObject.put("taskItemSelect", taskItem.getTaskItemSelect());
        dataObject.put("longitude", "");
        dataObject.put("latitude", "");
        dataObject.put("fireAddress", taskItem.getTaskItemAddress());
        dataObject.put("taskItemAudio", taskItem.getTaskItemAudio());
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
