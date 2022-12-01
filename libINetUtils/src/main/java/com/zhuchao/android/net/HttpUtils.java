package com.zhuchao.android.net;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;

import com.zhuchao.android.eventinterface.HttpCallback;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.FileUtils;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.ObjectList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final int TIMEOUT_IN_MILLIONS = 5000;
    private OkHttpClient okHttpClient;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private HttpUtils() {
        //LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        //loggingInterceptor.setLevel(LoggingInterceptor.Level.BODY);
        //loggingInterceptor.setLevel(LoggingInterceptor.Level.NONE);
        okHttpClient = new OkHttpClient().newBuilder().build();
    }

    private OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private static class Holder {//包装类
        private static HttpUtils httpUtils = new HttpUtils();
    }

    public static HttpUtils getInstance() {
        return Holder.httpUtils;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static void requestPost(final String tag, final String fromUrl, ObjectList requestParams, final HttpCallback RequestCallBack) {
        //RequestBody requestBody = new FormBody.Builder().build();
        FormBody.Builder formBody = new FormBody.Builder();
        Iterator<Map.Entry<String, Object>> it = requestParams.getAll().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            formBody.add(entry.getKey(), entry.getValue().toString());
        }
        RequestBody requestBody = formBody.build();
        Request request = new Request.Builder()
                .url(fromUrl)
                .post(requestBody)
                .build();
        HttpUtils.getInstance().getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //MMLog.log(TAG, "request onFailure from " + fromUrl);
                //MMLog.e(TAG, e.toString());
                ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response != null && response.isSuccessful()) {
                        result = response.body().string();
                        ResultCallBack(tag, fromUrl, "", 0, 0, result, DataID.TASK_STATUS_SUCCESS, RequestCallBack);
                    } else {
                        //MMLog.log(TAG, "Request failed from " + fromUrl);
                        ResultCallBack(tag, fromUrl, "", 0, 0, "response is null", DataID.TASK_STATUS_ERROR, RequestCallBack);
                    }
                } catch (IOException e) {
                    //MMLog.e(TAG, "request().onResponse " + e.getMessage());
                    ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
                }
                if(response != null)
                   response.close();
            }
        });
    }

    public static void requestPut(final String tag, final String fromUrl, String requestParams, final HttpCallback RequestCallBack) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(requestParams, JSON);
        } catch (Exception e) {
            //MMLog.e(TAG, e.toString());
            ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
            return;
        }
        Request request = new Request.Builder().url(fromUrl).put(requestBody).build();
        HttpUtils.getInstance().getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //MMLog.e(TAG, e.toString());
                ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response != null && response.isSuccessful()) {
                        result = response.body().string();
                        ResultCallBack(tag, fromUrl, "", 0, 0, result, DataID.TASK_STATUS_SUCCESS, RequestCallBack);
                    } else {
                        //MMLog.log(TAG, "Put Request failed from " + fromUrl);
                        ResultCallBack(tag, fromUrl, "", 0, 0, "response is null", DataID.TASK_STATUS_ERROR, RequestCallBack);
                    }
                } catch (IOException e) {
                    //MMLog.e(TAG, "request().onResponse " + e.getMessage());
                    ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
                }
                if(response != null)
                    response.close();
            }
        });
    }

    public static void requestGet(final String tag, final String fromUrl, final HttpCallback RequestCallBack) {
        if (EmptyString(fromUrl)) {
            //MMLog.log(TAG, "Expected URL scheme 'http' or 'https' but no scheme was found  fromUrl " + fromUrl);
            ResultCallBack(tag, fromUrl, "", 0, 0, "fromUrl = null", DataID.TASK_STATUS_ERROR, RequestCallBack);
            return;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(fromUrl).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //MMLog.e(TAG, e.toString());
                ResultCallBack(tag, fromUrl, "", 0, 0, "fromUrl = " + fromUrl + "," + e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response != null && response.isSuccessful()) {
                        result = response.body().string();
                        ResultCallBack(tag, fromUrl, "", 0, 0, result, DataID.TASK_STATUS_SUCCESS, RequestCallBack);
                    } else {
                        //MMLog.log(TAG, "Request failed from " + fromUrl);
                        ResultCallBack(tag, fromUrl, "", 0, 0, "response is null", DataID.TASK_STATUS_ERROR, RequestCallBack);
                    }
                } catch (IOException e) {
                    //MMLog.e(TAG, "request().onResponse " + e.getMessage());
                    ResultCallBack(tag, fromUrl, "", 0, 0, "fromUrl = " + fromUrl + "," + e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
                }
                if(response != null)
                    response.close();
            }
        });
    }

    //asynchronous 异步方法
    public static void download(final String tag, final String fromUrl, final String toUrl, final HttpCallback RequestCallBack) {
        HttpUtils.getInstance()
                .getOkHttpClient()
                .newCall(new Request.Builder()
                        .tag(tag)
                        .url(fromUrl)
                        .build())
                .enqueue(new Callback() {//asynchronous
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //MMLog.e(TAG, e.toString());
                        //MMLog.log(TAG, "download onFailure from " + fromUrl);
                        ResultCallBack(tag, fromUrl, toUrl, 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (response != null && response.isSuccessful()) {
                                InputStream inputStream = null;
                                FileOutputStream fileOutputStream = null;
                                long rwOffset = FileUtils.getFileSize(toUrl);
                                inputStream = response.body().byteStream();

                                long contentLength = response.body().contentLength();
                                long downloadLengthSum = rwOffset;
                                if (rwOffset <= 0) {
                                    //完整下载模式，覆盖模式
                                    fileOutputStream = new FileOutputStream(toUrl, false);
                                    downloadLengthSum = 0;
                                } else {
                                    inputStream.skip(rwOffset);//断点续传
                                    fileOutputStream = new FileOutputStream(toUrl, true);
                                    //MMLog.log(TAG, "continue downloading rw offset = " + rwOffset);
                                }
                                int len = 0;
                                byte[] buffer = new byte[1024 * 10];
                                while ((len = inputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, len);
                                    downloadLengthSum += len;

                                    ResultCallBack(tag, fromUrl, toUrl, downloadLengthSum, contentLength, "", DataID.TASK_STATUS_PROGRESSING, RequestCallBack);
                                }
                                fileOutputStream.flush();
                                fileOutputStream.close();
                                inputStream.close();
                                ResultCallBack(tag, fromUrl, toUrl, downloadLengthSum, contentLength, "SUCCESS", DataID.TASK_STATUS_SUCCESS, RequestCallBack);
                            } else {
                                //MMLog.log(TAG, "download failed from " + fromUrl);
                                ResultCallBack(tag, fromUrl, toUrl, 0, 0, "response is null", DataID.TASK_STATUS_ERROR, RequestCallBack);
                            }
                        } catch (Exception e) {
                            //.e(TAG, "download() response " + e.getMessage());
                            ResultCallBack(tag, fromUrl, toUrl, 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, RequestCallBack);
                        }
                        if(response != null)
                            response.close();
                    }
                });//{//asynchronous
    }

    public static void asynchronousGet(final String tag, final String fromUrl, final int requestId, final HttpCallback callBack) {
        new Thread() {
            public void run() {
                try {
                    String result = Get(fromUrl);
                    ResultCallBack(tag, fromUrl, "", 0, 0, result, DataID.TASK_STATUS_SUCCESS, callBack);
                } catch (Exception e) {
                    MMLog.e(TAG, "asynchronousPost() " + e.getMessage());
                    ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, callBack);
                }
            }
        }.start();
    }

    public static void asynchronousPost(final String tag, final String fromUrl, final String params, final HttpCallback callBack) throws Exception {
        new Thread() {
            public void run() {
                try {
                    String result = Post(fromUrl, params);
                    ResultCallBack(tag, fromUrl, "", 0, 0, result, DataID.TASK_STATUS_SUCCESS, callBack);
                } catch (Exception e) {
                    MMLog.e(TAG, "asynchronousPost() " + e.getMessage());
                    ResultCallBack(tag, fromUrl, "", 0, 0, e.toString(), DataID.TASK_STATUS_ERROR, callBack);
                }
            }
        }.start();
    }

    private static void ResultCallBack(String tag, String fromUrl, String lrl, long progress, long total, String result, int status, final HttpCallback callBack) {
        if (callBack != null) {
            callBack.onEventHttpRequest(tag, fromUrl, lrl, progress, total, result, status);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // java HttpURLConnection
    private static String Get(String fromUrl) {
        URL url = null;
        String result = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(fromUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(TIMEOUT_IN_MILLIONS);
            httpURLConnection.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = inputStream.read(buf)) != -1) {
                    byteArrayOutputStream.write(buf, 0, len);
                }
                byteArrayOutputStream.flush();
                result = byteArrayOutputStream.toString();
            } else {
                //throw new RuntimeException(" responseCode is not 200 ... ");
                MMLog.log(TAG, "get failed from " + fromUrl);
            }
            if (inputStream != null)
                inputStream.close();
            if (byteArrayOutputStream != null)
                byteArrayOutputStream.close();
            httpURLConnection.disconnect();
        } catch (Exception e) {
            //MMLog.log(TAG, e.toString());
            MMLog.e(TAG, "Get() " + e.getMessage());
        }
        return result;
    }

    private static String Post(String fromUrl, String param) {
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        String result = "";
        try {
            URL realUrl = new URL(fromUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) realUrl.openConnection();
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("charset", "utf-8");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setReadTimeout(TIMEOUT_IN_MILLIONS);
            httpURLConnection.setConnectTimeout(TIMEOUT_IN_MILLIONS);

            if (param != null && !param.trim().equals("")) {
                printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                printWriter.print(param);
                printWriter.flush();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                result += str;
            }
            if (printWriter != null)
                printWriter.close();
            if (bufferedReader != null)
                bufferedReader.close();
        } catch (Exception e) {
            MMLog.e(TAG, "Post() " + e.getMessage());
            //e.printStackTrace();
        } finally {
        }
        return result;
    }
}
