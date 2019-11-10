package com.zhuchao.android.libfilemanager;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    /**文档类型*/
    public static final int TYPE_DOC = 0;
    /**apk类型*/
    public static final int TYPE_APK = 1;
    /**压缩包类型*/
    public static final int TYPE_ZIP = 2;


    /**
     * 判断文件是否存在
     * @param path 文件的路径
     * @return
     */
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static int getFileType(String path) {
        path = path.toLowerCase();
        if (path.endsWith(".doc") || path.endsWith(".docx") || path.endsWith(".xls") || path.endsWith(".xlsx")
                || path.endsWith(".ppt") || path.endsWith(".pptx")) {
            return TYPE_DOC;
        }else if (path.endsWith(".apk")) {
            return TYPE_APK;
        }else if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".gz")) {
            return TYPE_ZIP;
        }else{
            return -1;
        }
    }


    /**通过文件名获取文件图标*/
    public static int getFileIconByPath(String path){
        path = path.toLowerCase();
        //int iconId = R.mipmap.unknow_file_icon;
        int iconId = 0;//R.mipmap.ic_launcher;
        /*if (path.endsWith(".txt")){
            iconId = R.mipmap.type_txt;
        }else if(path.endsWith(".doc") || path.endsWith(".docx")){
            iconId = R.mipmap.type_doc;
        }else if(path.endsWith(".xls") || path.endsWith(".xlsx")){
            iconId = R.mipmap.type_xls;
        }else if(path.endsWith(".ppt") || path.endsWith(".pptx")){
            iconId = R.mipmap.type_ppt;
        }else if(path.endsWith(".xml")){
            iconId = R.mipmap.type_xml;
        }else if(path.endsWith(".htm") || path.endsWith(".html")){
            iconId = R.mipmap.type_html;
        }*/
        return iconId;
    }

    /**是否是图片文件*/
    public static boolean isPicFile(String path){
        path = path.toLowerCase();
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")){
            return true;
        }
        return false;
    }


    /** 判断SD卡是否挂载 */
    public static boolean isSDCardAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> ReadTxtFile(String filePath) {
        List newList=new ArrayList<String>();
        try {
            File file = new File(filePath);
            int count = 0;//初始化 key值
            if (file.isFile() && file.exists()) {//文件存在
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        String reds = lineTxt.split("\\+")[0];  //java 正则表达式
                        newList.add(count, reds.toString());
                        count++;
                    }
                }
                isr.close();
                br.close();
            }else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    /**
     * 从文件的全名得到文件的拓展名
     *
     * @param filename
     * @return
     */
    public static String getExtFromFileFullName(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getFileNameFromFileFullName(String filename) {
        File file=new File(filename);

        if(file.exists())
        {
           return file.getName();
        }
        return null;
    }

    /**
     * 读取文件的修改时间
     *
     * @param f
     * @return
     */
    public static String getModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        // System.out.println("修改时间[2] " + formatter.format(cal.getTime()));
        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.getTime());
    }

    public static Map<String, String>  getUDiscName(Context c) {
        StorageManager mStorageManager;
        Map<String, String> USBDiscs = new HashMap<String, String>();
        Class<?> volumeInfoClazz = null;
        Method getDescriptionComparator = null;
        Method getBestVolumeDescription = null;
        Method getVolumes = null;
        Method isMountedReadable = null;
        Method getType = null;
        Method getPath = null;
        List<?> volumes = null;
        try {
            mStorageManager = (StorageManager) c.getSystemService(Activity.STORAGE_SERVICE);
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");
            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            volumes = (List<?>) getVolumes.invoke(mStorageManager);
            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    String p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                    String p2 = path2.getPath();
                    USBDiscs.put(p1,p2);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return USBDiscs;
    }


    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                Log.d("FileUtils",oldPath+"---->"+newPath);

                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

}
