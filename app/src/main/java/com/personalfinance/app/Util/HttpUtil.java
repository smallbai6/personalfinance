package com.personalfinance.app.Util;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * OKHttp
 */
public class HttpUtil {
    public static OkHttpClient sendOkHttpRequest(String address, RequestBody requestBody, okhttp3.Callback callback) {

        OkHttpClient client = new OkHttpClient.Builder()
                // .connectTimeout(10, TimeUnit.SECONDS)
                //.readTimeout(20, TimeUnit.SECONDS)
                //  .connectionPool(new ConnectionPool(32,5,TimeUnit.MINUTES))
                .build();
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
        return client;
    }

}

