package com.zhuchao.android.netutil;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.zhuchao.android.libfileutils.FilesManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DownloadManager {
    private final String TAG = "DownloadManager-->";
    private static DownloadManager manager = null;
    private static String authoritiesAppID = null;
    private String mRemoteFileUrl = "";
    private String mLocalFilePath = "";
    private Context mContext;

    private DownloadManager() {
        //this.activity = activity;
    }

    public String getAuthoritiesAppID() {
        return authoritiesAppID;
    }

    public void setAuthoritiesAppID(String authoritiesAppID) {
        DownloadManager.authoritiesAppID = authoritiesAppID;
    }

    public synchronized static DownloadManager getInstance() {
        manager = new DownloadManager();
        return manager;
    }

    public DownloadManager(String mRemoteFileUrl, String mLocalFilePathName) {
        this.mRemoteFileUrl = mRemoteFileUrl;
        this.mLocalFilePath = mLocalFilePathName;
    }

    public String getmRemoteFileUrl() {
        return mRemoteFileUrl;
    }

    public void setmRemoteFileUrl(String mRemoteFileUrl) {
        this.mRemoteFileUrl = mRemoteFileUrl;
    }

    public String getmLocalFilePath() {
        return mLocalFilePath;
    }

    public void setmLocalFilePath(String mLocalFilePath) {
        this.mLocalFilePath = mLocalFilePath;
    }

    public DownloadManager with(Context context) {
        mContext = context;
        return this;
    }

    public DownloadManager downloadFrom(String url) {
        mRemoteFileUrl = url;
        return this;
    }

    public void to(String filePath) {
        mLocalFilePath = filePath;
        new Thread() {
            public void run() {
                Looper.prepare();
                downloadFile(mRemoteFileUrl, mLocalFilePath);
                Looper.loop();
            }
        }.start();

    }

    public String download(String url) {
        mRemoteFileUrl = url;
        //File file0 = new File(mRemouteFileUrl);

        mLocalFilePath = getDownloadCacheDir(mContext) + stringToMD5(url) + FilesManager.getExtFromFileFullName(url);     // ".mp4";
        File file = new File(mLocalFilePath);

        if (!file.exists()) {
            new Thread() {
                public void run() {
                    Looper.prepare();
                    downloadFile(mRemoteFileUrl, mLocalFilePath);
                    Looper.loop();
                }
            }.start();
        }
        return mLocalFilePath;
    }

    private void downloadFile(String fromFile, String toFile) {
        String savePath = toFile;
        String serverFilePath = fromFile;
        //int progress = 0;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                URL serverURL = new URL(serverFilePath);
                HttpURLConnection connect = (HttpURLConnection) serverURL.openConnection();
                //获取到文件的大小
                int fileLength = connect.getContentLength();
                InputStream is = connect.getInputStream();
                File file = new File(savePath);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024*100];
                int len;
                //int total = 0;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    //total += len;
                    //获取当前下载量
                    //progress = (int) (((float) total / fileLength) * 100);
                    //Message msg = new Message();
                    //msg.arg1 = progress;
                }
                fos.close();
                bis.close();
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void removeFile(String filePath) {
        if(filePath == null || filePath.length() == 0){
            return;
        }
        try {
            File file = new File(filePath);
            if(file.exists()){
                removeFile(file);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void removeFile(File file){
        //如果是文件直接删除
        if(file.isFile()){
            file.delete();
            return;
        }
        //如果是目录，递归判断，如果是空目录，直接删除，如果是文件，遍历删除
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                removeFile(f);
            }
            file.delete();
        }
    }

    public static String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public Boolean ExistsLocalCacheFile(String url) {
        String fileName = getDownloadCacheDir(mContext) + stringToMD5(url) + FilesManager.getExtFromFileFullName(url);//".mp4";
        File file = new File(fileName);
        return file.exists();
    }

    public String GetLocalCacheFile(String url) {
        String fileName = getDownloadCacheDir(mContext) + stringToMD5(url) + FilesManager.getExtFromFileFullName(url);//".mp4";
        return fileName;
    }

    public String getDownloadCacheDir(Context context) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdCard = new File(filePath);//new File("/mnt/media_rw/1716-1E0A/");//
        File file = new File(sdCard, "DownloadCache");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath()+"/";
    }


}
