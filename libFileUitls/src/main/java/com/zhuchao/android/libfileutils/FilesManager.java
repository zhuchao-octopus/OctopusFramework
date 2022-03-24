package com.zhuchao.android.libfileutils;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.zhuchao.android.libfileutils.bean.FileBean;
import com.zhuchao.android.libfileutils.bean.ImgFolderBean;
import com.zhuchao.android.libfileutils.bean.LMusic;
import com.zhuchao.android.libfileutils.bean.LVideo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesManager {
    private final static String TAG = "FilesManager";
    /**
     * 文档类型
     */
    public static final int TYPE_DOC = 0;
    /**
     * apk类型
     */
    public static final int TYPE_APK = 1;
    /**
     * 压缩包类型
     */
    public static final int TYPE_ZIP = 2;


    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            return file.getName();
        else
            return null;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) file.delete();
        return true;
    }

    public static boolean deleteFiles(String filePath) {
        List<File> files = getFiles(filePath);
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                /**  如果是文件则删除  如果都删除可不必判断  */
                //if (file.isFile()) {
                file.delete();
                //}
            }
        }
        return true;
    }

    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

    public static void copy(String FromFile, String ToFile) {
        //int bytesum = 0;
        int byteread = 0;
        try {
            File fFile = new File(FromFile);
            if (fFile.exists()) { //文件存在时
                InputStream fStream = new FileInputStream(FromFile); //读入原文件
                FileOutputStream tStream = new FileOutputStream(ToFile);
                byte[] buffer = new byte[1024];
                //int length;
                while ((byteread = fStream.read(buffer)) != -1) {
                    //bytesum += byteread; //字节数 文件大小
                    tStream.write(buffer, 0, byteread);
                }
                fStream.close();
                tStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean pathCopy(String fromFile, String toFile) {
        final Path ff = Paths.get(fromFile);
        final Path tf = Paths.get(toFile);
        try {
            Files.copy(ff, tf, NOFOLLOW_LINKS);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean channelTransferTo(String fromFile, String toFile) {
        final Path ff = Paths.get(fromFile);
        final Path tf = Paths.get(toFile);
        try {
            FileChannel fileChannel_f = (FileChannel.open(ff, EnumSet.of(StandardOpenOption.READ)));
            FileChannel fileChannel_t = (FileChannel.open(tf, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)));
            fileChannel_f.transferTo(0L, fileChannel_f.size(), fileChannel_t);
            return true;
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long channelTransferFrom(String fromFile, String toFile) {
        final Path ff = Paths.get(fromFile);
        final Path tf = Paths.get(toFile);
        long l = 0;
        try {
            FileChannel fileChannel_f = (FileChannel.open(ff, EnumSet.of(StandardOpenOption.READ)));
            FileChannel fileChannel_t = (FileChannel.open(tf, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)));
            l = fileChannel_t.transferFrom(fileChannel_f, 0L, fileChannel_f.size());
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return l;
    }

    public static List<File> getFiles(String filePath) {
        File file = new File(filePath);
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        } else {
            for (File f : fileArray) {
                if (f.isFile()) {
                    list.add(0, f);
                } else {
                    getFiles(f.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static int getFileType(String path) {
        path = path.toLowerCase();
        if (path.endsWith(".doc") || path.endsWith(".docx") || path.endsWith(".xls") || path.endsWith(".xlsx")
                || path.endsWith(".ppt") || path.endsWith(".pptx")) {
            return TYPE_DOC;
        } else if (path.endsWith(".apk")) {
            return TYPE_APK;
        } else if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".gz")) {
            return TYPE_ZIP;
        } else {
            return -1;
        }
    }

    /**
     * 通过文件名获取文件图标
     */
    public static int getFileIconByPath(String path) {
        path = path.toLowerCase();
        int iconId = 0;//R.mipmap.ic_launcher;
        return iconId;
    }

    /**
     * 是否是图片文件
     */
    public static boolean isPicFile(String path) {
        path = path.toLowerCase();
        return path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png");
    }

    /**
     * 判断SD卡是否挂载
     */
    public static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
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
            return filename.substring(dotPosition + 1);
        }
        return "";
    }

    public static String getFileNameFromFileFullName(String filename) {
        File file = new File(filename);

        if (file.exists()) {
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

    public static Map<String, String> getUDiscName(Context c) {
        StorageManager mStorageManager;
        Map<String, String> USBDiscs = new HashMap<String, String>();
        Class<?> volumeInfoClazz = null;
        //Method getDescriptionComparator = null;
        Method getBestVolumeDescription = null;
        Method getVolumes = null;
        Method isMountedReadable = null;
        Method getType = null;
        Method getPath = null;
        List<?> volumes = null;
        String p1 = "";
        String p2 = "";
        try {
            mStorageManager = (StorageManager) c.getSystemService(Activity.STORAGE_SERVICE);
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            //getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");

            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            volumes = (List<?>) getVolumes.invoke(mStorageManager);
            for (Object vol : volumes)
            {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                    p2 = path2.getPath();
                    USBDiscs.put(p1, p2);
                    //Log.i(TAG, "getUDiscName DeviceName = " + p1 + ":" + p2);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return USBDiscs;
    }

    public static String getUDiscPath(Context c) {
        StorageManager mStorageManager;
        Map<String, String> USBDiscs = new HashMap<String, String>();
        Class<?> volumeInfoClazz = null;
        //Method getDescriptionComparator = null;
        Method getBestVolumeDescription = null;
        Method getVolumes = null;
        Method isMountedReadable = null;
        Method getType = null;
        Method getPath = null;
        List<?> volumes = null;
        String rpath = null;
        try {
            mStorageManager = (StorageManager) c.getSystemService(Activity.STORAGE_SERVICE);
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            //getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
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
                    USBDiscs.put(p1, p2);
                    rpath = p2;
                    //Log.i(TAG, "getUDiscPath DeviceName = " + p1 + ":" + p2);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rpath;
    }

    /**
     * 获取本机音乐列表
     *
     * @return
     */
    public static List<LMusic> getMusics(Context context) {
        ArrayList<LMusic> musics = new ArrayList<>();
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径

                if (!new File(path).exists()) {
                    continue;
                }

                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // 歌曲名
                String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
                String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));// 大小
                int duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 时长
                int time = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));// 歌曲的id
                // int albumId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                LMusic music = new LMusic(name, path, album, artist, size, duration);
                musics.add(music);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return musics;
    }

    /**
     * 获取本机视频列表
     *
     * @return
     */
    public static List<LVideo> getVideos(Context context) {
        List<LVideo> videos = new ArrayList<LVideo>();
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor c = null;
        try {
            // String[] mediaColumns = { "_id", "_data", "_display_name",
            // "_size", "date_modified", "duration", "resolution" };
            c = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));// 路径
                if (!new File(path).exists()) {
                    continue;
                }

                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// 视频的id
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)); // 视频名称
                String resolution = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)); //分辨率
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                long duration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长
                long date = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));//修改时间

                LVideo LVideo = new LVideo(id, path, name, resolution, size, date, duration);
                videos.add(LVideo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return videos;
    }

    // 获取视频缩略图
    public static Bitmap getVideoThumbnail(Context context, int id) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver mContentResolver = context.getContentResolver();
        //options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
        return bitmap;
    }

    /**
     * 通过文件类型得到相应文件的集合
     **/
    public static List<FileBean> getFilesByType(Context context, int fileType) {
        List<FileBean> files = new ArrayList<FileBean>();
        ContentResolver mContentResolver = context.getContentResolver();
        // 扫描files文件库
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{"_id", "_data", "_size"}, null, null, null);
            int dataindex = c.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int sizeindex = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

            while (c.moveToNext()) {
                String path = c.getString(dataindex);

                if (FilesManager.getFileType(path) == fileType) {
                    if (!FilesManager.isExists(path)) {
                        continue;
                    }
                    long size = c.getLong(sizeindex);
                    FileBean fileBean = new FileBean(path, FilesManager.getFileIconByPath(path));
                    files.add(fileBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return files;
    }

    /**
     * 得到图片文件夹集合
     */
    public static List<ImgFolderBean> getImageFolders(Context context) {
        List<ImgFolderBean> folders = new ArrayList<ImgFolderBean>();
        ContentResolver mContentResolver = context.getContentResolver();
        // 扫描图片
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
            List<String> mDirs = new ArrayList<String>();//用于保存已经添加过的文件夹目录
            while (c.moveToNext()) {
                @SuppressLint("Range") String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));// 路径
                File parentFile = new File(path).getParentFile();
                if (parentFile == null)
                    continue;

                String dir = parentFile.getAbsolutePath();
                if (mDirs.contains(dir))//如果已经添加过
                    continue;

                mDirs.add(dir);//添加到保存目录的集合中
                ImgFolderBean folderBean = new ImgFolderBean();
                folderBean.setDir(dir);
                folderBean.setFistImgPath(path);
                if (parentFile.list() == null)
                    continue;
                int count = parentFile.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png");
                    }
                }).length;

                folderBean.setCount(count);
                folders.add(folderBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return folders;
    }

    /**
     * 通过图片文件夹的路径获取该目录下的图片
     */
    public static List<String> getImgListByDir(String dir) {
        ArrayList<String> imgPaths = new ArrayList<>();
        File directory = new File(dir);
        if (directory == null || !directory.exists()) {
            return imgPaths;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            String path = file.getAbsolutePath();
            if (FilesManager.isPicFile(path)) {
                imgPaths.add(path);
            }
        }
        return imgPaths;
    }

    public static List<String> getLocalImageList() {
        List<ImgFolderBean> folders = new ArrayList<ImgFolderBean>();
        List<String> ImageList = new ArrayList<String>();
        List<String> AllImageList = new ArrayList<String>();
        for (ImgFolderBean key : folders) {
            ImageList = getImgListByDir(key.getDir());
            for (String imgFilePath : ImageList)
                AllImageList.add(imgFilePath);
        }
        return AllImageList;
    }

    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getAbsolutePath();
        } else {
            return context.getCacheDir().getAbsolutePath();
        }
    }

    public static String getDownloadDir(String downloadDir) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyDownload/";
        if (!TextUtils.isEmpty(downloadDir)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + downloadDir + "/";
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static List<String> ReadTxtFile(String filePath) {
        List newList = new ArrayList<String>();
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
                        newList.add(count, reds);
                        count++;
                    }
                }
                isr.close();
                br.close();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    public static void writeFile(String filePath, String data, boolean append) {
        FileOutputStream out;
        BufferedWriter writer = null;
        //String Path = getDiskCachePath(context) + "/" + fileName + ".xml";
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("FilesManager", "Not found file:" + filePath);
            return;
        }
        try {
            out = new FileOutputStream(file, append);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);

        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(Tag, "写入文件失败");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //Log.e(Tag, e.getMessage());
            }
        }
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void makeDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public static String getRealFilePathFromUri(Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
            if (data == null) {
                data = getImageAbsolutePath(context, uri);
            }
        }
        return data;
    }

    public static Uri getUri(final String filePath) {
        return Uri.fromFile(new File(filePath));
    }

    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMD5(File f) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bi != null)
            return bi.toString(16);
        else
            return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public static void getFiles(Context context, String FilePath, List<String> FileList) {
        //List<String> FileList = new ArrayList<String>();
        File path = new File(FilePath);
        File[] files = path.listFiles();
        getFileList(context, files, FileList);
        //return FileList;
    }

    private static void getFileList(Context context, File[] files, List<String> FileList) {
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            String filePathName = null;
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileList(context, file.listFiles(), FileList);
                } else {
                    filePathName = file.getPath();// +"  "+ file.getName() ;
                    FileList.add(filePathName);
                    sendProgressMessage(context, filePathName);
                }
            }
        }
    }

    private static void sendProgressMessage(Context context, String msg) {
        if (context != null) {
            Intent i = new Intent("com.zhuchao.android.FILE_SCAN_ACTION");
            i.putExtra("FileName", msg);
            context.sendBroadcast(i);
        }
    }

}
