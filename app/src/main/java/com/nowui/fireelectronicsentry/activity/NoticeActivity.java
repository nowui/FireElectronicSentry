package com.nowui.fireelectronicsentry.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nowui.fireelectronicsentry.R;
import com.nowui.fireelectronicsentry.model.Notice;
import com.nowui.fireelectronicsentry.utility.Helper;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeActivity extends AppCompatActivity {

    private MaterialDialog materialDialog;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private WarningAdapter warningAdapter;
    private PullToRefreshListView warningPullRefreshListView;
    private List<Notice> warningList = new ArrayList<Notice>();
    private int warningPage = 1;
    private boolean isLanch = true;
    private NoticeAdapter noticeAdapter;
    private PullToRefreshListView noticePullRefreshListView;
    private List<Notice> noticeList = new ArrayList<Notice>();
    private int noticePage = 1;

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
        setContentView(R.layout.activity_notice);
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

        warningPullRefreshListView = (PullToRefreshListView) findViewById(R.id.warningPullRefreshListView);
        warningPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        warningPullRefreshListView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull));
        warningPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.refreshing));
        warningPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.release));
        warningPullRefreshListView.getLoadingLayoutProxy().setLastUpdatedLabel(getResources().getString(R.string.update) + Helper.formatDateTime());
        warningPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                warningPage = 1;

                loadWarning();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadWarning();
            }
        });

        ListView warningListView = warningPullRefreshListView.getRefreshableView();
        warningListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notice notice = warningList.get(position - 1);

                notice.setMessageHasReaded("1");

                warningAdapter.notifyDataSetChanged();

                /*Intent intent = new Intent();
                intent.putExtra("messageid", map.get("id").toString());
                intent.setClass(NoticeActivity.this, NoticeDetailActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);*/

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                String userId = setting.getString(Helper.KeyUserId, "");

                Intent intent = new Intent();
                intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Notifiaction/NotificationDetail.aspx?messageID=" + notice.getMessageid() + "&userID=" + userId);
                intent.setClass(NoticeActivity.this, CarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });

        warningAdapter = new WarningAdapter(NoticeActivity.this);
        warningListView.setAdapter(warningAdapter);

        showLoadingDialog();

        loadWarning();



        noticePullRefreshListView = (PullToRefreshListView) findViewById(R.id.noticePullRefreshListView);
        noticePullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        noticePullRefreshListView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull));
        noticePullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.refreshing));
        noticePullRefreshListView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.release));
        noticePullRefreshListView.getLoadingLayoutProxy().setLastUpdatedLabel(getResources().getString(R.string.update) + Helper.formatDateTime());
        noticePullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                noticePage = 1;

                loadNotice();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNotice();
            }
        });

        ListView noticeListView = noticePullRefreshListView.getRefreshableView();
        noticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notice notice = noticeList.get(position - 1);

                notice.setMessageHasReaded("1");

                noticeAdapter.notifyDataSetChanged();

                SharedPreferences setting = getSharedPreferences(Helper.KeyAppSetting, Activity.MODE_PRIVATE);
                String userId = setting.getString(Helper.KeyUserId, "");

                Intent intent = new Intent();
                intent.putExtra("url", Helper.WebUrl + "/Herigbit/WebSite/Notifiaction/NotificationDetail.aspx?messageID=" + notice.getMessageid() + "&userID=" + userId);
                intent.setClass(NoticeActivity.this, CarActivity.class);
                startActivityForResult(intent, Helper.CodeRequest);
            }
        });


        noticeAdapter = new NoticeAdapter(NoticeActivity.this);
        noticeListView.setAdapter(noticeAdapter);

        loadNotice();

        /*new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                pullRefreshListView.setRefreshing(true);
            }
        }, 500);*/
    }

    private void showLoadingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_wait))
                .content(getResources().getString(R.string.refreshing))
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    private void loadWarning() {
        String url = Helper.WebUrl + "/NotificationMessageTypeList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/NotificationMessageTypeList.ashx";
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
        dataObject.put("pagenum", warningPage);
        dataObject.put("messageCompositeType", 2);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(NoticeActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = JSON.parseObject(jsonMap.get("data").toString(), new TypeReference<Map<String, Object>>() {});

                    List<Notice> notices = JSON.parseArray(dataMap.get("messageTypeList").toString(), Notice.class);

                    if (warningPage == 1) {
                        warningList.clear();
                    }

                    for (Notice notice : notices) {
                        warningList.add(notice);
                    }

                    if (notices.size() > 0) {
                        warningPage++;
                    }

                    warningAdapter.notifyDataSetChanged();
                }

                warningPullRefreshListView.onRefreshComplete();

                if (isLanch) {
                    isLanch = false;

                    materialDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                materialDialog.hide();
            }
        });
    }


    private void loadNotice() {
        String url = Helper.WebUrl + "/NotificationMessageTypeList.ashx";
        if (Helper.isAdmin) {
            url = Helper.AdminWebUrl + "/NotificationMessageTypeList.ashx";
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
        dataObject.put("pagenum", noticePage);
        dataObject.put("messageCompositeType", 1);
        jsonObject.put("data", dataObject);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(url);
        System.out.println(jsonObject.toString());

        client.post(NoticeActivity.this, url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));

                Map<String, Object> jsonMap = JSON.parseObject(new String(responseBody), new TypeReference<Map<String, Object>>() {});

                if (Integer.valueOf(jsonMap.get("result").toString()) == 1) {

                    Map<String, Object> dataMap = JSON.parseObject(jsonMap.get("data").toString(), new TypeReference<Map<String, Object>>() {});

                    List<Notice> notices = JSON.parseArray(dataMap.get("messageTypeList").toString(), Notice.class);

                    if (noticePage == 1) {
                        noticeList.clear();
                    }

                    for (Notice notice : notices) {
                        noticeList.add(notice);
                    }

                    if (notices.size() > 0) {
                        noticePage++;
                    }

                    noticeAdapter.notifyDataSetChanged();
                }

                noticePullRefreshListView.onRefreshComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private class WarningAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public WarningAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return warningList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setSelectIndex(int selectIndex) {
            //Map<String, Object> map = warningList.get(selectIndex);


        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WarningHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_notice, parent, false);

                holder = new WarningHolder();
                holder.messageHasReadedTextView = (TextView) convertView.findViewById(R.id.messageHasReadedTextView);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                convertView.setTag(holder);
            } else {
                holder = (WarningHolder) convertView.getTag();
            }

            Notice notice = warningList.get(position);

            holder.titleTextView.setText(notice.getMessageTitle());

            if (! notice.getMessageHasReaded().equals("1")) {
                holder.messageHasReadedTextView.setVisibility(View.VISIBLE);
            }



            return convertView;
        }
    }

    private class WarningHolder {
        public TextView messageHasReadedTextView;
        public TextView titleTextView;
    }

    private class NoticeAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public NoticeAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return noticeList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setSelectIndex(int selectIndex) {
            //Map<String, Object> map = warningList.get(selectIndex);


        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NoticeHolder holder = null;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_list_notice, parent, false);

                holder = new NoticeHolder();
                holder.messageHasReadedTextView = (TextView) convertView.findViewById(R.id.messageHasReadedTextView);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                convertView.setTag(holder);
            } else {
                holder = (NoticeHolder) convertView.getTag();
            }

            Notice notice = noticeList.get(position);

            holder.titleTextView.setText(notice.getMessageTitle());

            if (! notice.getMessageHasReaded().equals("1")) {
                holder.messageHasReadedTextView.setVisibility(View.VISIBLE);
            } else {
                holder.messageHasReadedTextView.setVisibility(View.INVISIBLE);
            }



            return convertView;
        }
    }

    private class NoticeHolder {
        public TextView messageHasReadedTextView;
        public TextView titleTextView;
    }

}
