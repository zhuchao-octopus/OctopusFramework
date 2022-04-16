package com.zhuchao.android.netutil;

import com.zhuchao.android.callbackevent.HttpCallBack;
import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.libfileutils.MMLog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils";
    private OkHttpClient okHttpClient;

    private OkHttpUtils() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? LoggingInterceptor.Level.BODY : LoggingInterceptor.Level.NONE);
        okHttpClient = new OkHttpClient().newBuilder().build();
    }

    private static class Holder {
        private static OkHttpUtils httpUtils = new OkHttpUtils();
    }

    public static OkHttpUtils getInstance() {
        return Holder.httpUtils;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    //异步请求
    public static void request(final String url, final HttpCallBack RequestCallBack) {
        OkHttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //MMLog.log(TAG, "Failed:" + url);
                if (RequestCallBack != null)
                    RequestCallBack.onHttpRequestProgress("", url, "", -1, 0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String result = response.body().string();
                    if (RequestCallBack != null)
                        RequestCallBack.onHttpRequestProgress("", url, result, 0, 0);
                } else {
                    MMLog.log(TAG, "Request failed:" + url);
                    //normalRequestCallBack.onRequestComplete("", -1);
                }
            }
        });
    }

    //asynchronous 异步方法
    public static void Download(String tag, final String fromUrl, final String toUrl, final HttpCallBack RequestCallBack) {
        OkHttpUtils.getInstance()
                .getOkHttpClient()
                .newCall(new Request.Builder()
                        .tag(tag)
                        .url(fromUrl)
                        .build())
                .enqueue(new Callback() {//asynchronous
                    @Override
                    public void onFailure(Call call, IOException e) {
                        MMLog.log(TAG, "download failed from " + fromUrl);
                        if (RequestCallBack != null)
                            RequestCallBack.onHttpRequestComplete(tag, fromUrl, toUrl, -1, -1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException
                    {
                        if (response != null && response.isSuccessful())
                        {
                            InputStream inputStream = null;
                            FileOutputStream fileOutputStream = null;
                            try {
                                inputStream = response.body().byteStream();
                                fileOutputStream = new FileOutputStream(toUrl);
                                long contentLength = response.body().contentLength();
                                long sum = 0;
                                int len = 0;
                                byte[] buffer = new byte[1024 * 10];
                                while ((len = inputStream.read(buffer)) != -1)
                                {
                                    fileOutputStream.write(buffer, 0, len);
                                    sum += len;
                                    if (RequestCallBack != null)
                                        RequestCallBack.onHttpRequestProgress(tag, fromUrl, toUrl, sum, contentLength);
                                }
                                fileOutputStream.flush();
                                fileOutputStream.close();
                                inputStream.close();
                                if (RequestCallBack != null)
                                    RequestCallBack.onHttpRequestComplete(tag, fromUrl, toUrl, sum, contentLength);
                            } catch (Exception e) {
                                MMLog.log(TAG, e.toString());
                                if (RequestCallBack != null)
                                    RequestCallBack.onHttpRequestComplete(tag, fromUrl, toUrl, 0 - 1, -1);
                            }
                        } else {
                            MMLog.log(TAG, "download failed from " + fromUrl);
                            if (RequestCallBack != null)
                                RequestCallBack.onHttpRequestComplete(tag, fromUrl, toUrl, -1, -1);
                        }

                    }
                });//{//asynchronous
    }
}
