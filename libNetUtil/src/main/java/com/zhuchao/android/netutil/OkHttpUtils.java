package com.zhuchao.android.netutil;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.zhuchao.android.callbackevent.NormalRequestCallBack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.BuildConfig;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZTZ on 2018/3/20.
 */

public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils>>>>>";
    private OkHttpClient okHttpClient;
    private Handler mHandler;

    private OkHttpUtils() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)).build();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static OkHttpUtils getInstance() {
        return Holder.httpUtils;
    }

    private static class Holder {
        private static OkHttpUtils httpUtils = new OkHttpUtils();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    //异步请求
    public static void request(final String url, final NormalRequestCallBack normalRequestCallBack) {
        OkHttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "failed:" + url);
                normalRequestCallBack.onRequestComplete("", -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    Log.d(TAG, "successfully:" + url);
                    Log.d(TAG, response.toString());
                    String result = response.body().string();
                    normalRequestCallBack.onRequestComplete(result, 0);
                }
            }
        });
    }

    public static void Download(final String url,final String toPath,String tag, final NormalRequestCallBack normalRequestCallBack) {
        OkHttpUtils.getInstance()
                .getOkHttpClient()
                .newCall(new Request.Builder()
                        .url(url)
                        .tag(tag)
                        .build())
                .enqueue(new Callback()

                {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Download failed from :" + url);
                normalRequestCallBack.onRequestComplete("", -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful())
                {
                    String result = response.body().string();
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream fos = new FileOutputStream(toPath);

                    int len = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    inputStream.close();
                    Log.d(TAG, "Download successfully from:" + url);
                    normalRequestCallBack.onRequestComplete(result, 0);

                } else {
                    Log.d(TAG, "download failed from :" + url);
                    normalRequestCallBack.onRequestComplete("", -1);
                }
            }
        });
    }
}
