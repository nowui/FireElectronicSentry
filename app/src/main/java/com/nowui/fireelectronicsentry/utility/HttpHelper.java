package com.nowui.fireelectronicsentry.utility;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by yongqiangzhong on 7/26/15.
 */
public class HttpHelper {

    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(11000);
    }

    public static void get(String urlString,AsyncHttpResponseHandler res) {
        client.get(urlString, res);
    }

    public static void get(String urlString,RequestParams params,AsyncHttpResponseHandler res) {
        client.get(urlString, params,res);
    }

    public static void get(String urlString,JsonHttpResponseHandler res) {
        client.get(urlString, res);
    }

    public static void get(String urlString,RequestParams params,JsonHttpResponseHandler res) {
        client.get(urlString, params,res);
    }

    public static void get(String uString, BinaryHttpResponseHandler bHandler) {
        client.get(uString, bHandler);
    }

    public static AsyncHttpClient getClient() {
        return client;
    }

}
