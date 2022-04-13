package com.zhuchao.android.netutil;

import android.os.Handler;
import android.os.Looper;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.libfileutils.MLog;

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

public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils";
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
    public static void request(final String url, final NormalRequestCallback normalRequestCallBack) {
        OkHttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MLog.log(TAG, "Failed:" + url);
                if (normalRequestCallBack != null)
                    normalRequestCallBack.onRequestComplete("", -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String result = response.body().string();
                    MLog.log(TAG, "ok:" + url);
                    MLog.log(TAG, "gt:" + result);
                    if (normalRequestCallBack != null)
                        normalRequestCallBack.onRequestComplete(result, 0);
                } else {
                    MLog.log(TAG, "failed:" + url);
                    //normalRequestCallBack.onRequestComplete("", -1);
                }
            }
        });
    }

    public static void Download(final String url, final String toPath, String tag, final NormalRequestCallback normalRequestCallBack) {
        OkHttpUtils.getInstance()
                .getOkHttpClient()
                .newCall(new Request.Builder()
                        .url(url)
                        .tag(tag)
                        .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (normalRequestCallBack != null)
                            normalRequestCallBack.onRequestComplete("", -1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.isSuccessful()) {
                            //String result = response.body().string();
                            MLog.log(TAG, "Download from:" + url + " to " + toPath);
                            InputStream inputStream = response.body().byteStream();
                            FileOutputStream fos = new FileOutputStream(toPath);
                            //long tLen = response.body().contentLength();
                            long sum = 0;
                            int len = 0;
                            byte[] buffer = new byte[1024 * 10];
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                sum += len;
                                //MLog.log(TAG, "Downloading:" + sum+"/"+tlen);
                            }
                            fos.flush();
                            fos.close();
                            inputStream.close();
                            MLog.log(TAG, "Download successfully from:" + url + " to: " + toPath);
                            if (normalRequestCallBack != null)
                                normalRequestCallBack.onRequestComplete("success", 0);
                        } else {
                            MLog.log(TAG, "download failed from :" + url);
                            //normalRequestCallBack.onRequestComplete("", -1);
                        }
                    }
                });
    }
}
