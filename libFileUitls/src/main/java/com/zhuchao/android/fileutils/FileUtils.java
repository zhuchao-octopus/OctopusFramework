package com.zhuchao.android.fileutils;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.zhuchao.android.fileutils.bean.FileBean;
import com.zhuchao.android.fileutils.bean.ImgFolderBean;
import com.zhuchao.android.fileutils.bean.LMusic;
import com.zhuchao.android.fileutils.bean.LVideo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

public class FileUtils {
    private final static String TAG = "FileUtils";
    public static final int TYPE_DOC = 0;
    public static final int TYPE_APK = 1;
    public static final int TYPE_ZIP = 2;

    public static boolean EmptyString(String str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean NotEmptyString(String str) {
        return !TextUtils.isEmpty(str);
    }

    public static boolean isExternalLinks(String filePath) {
        if (EmptyString(filePath)) return false;
        if (filePath.startsWith("http:") ||
                filePath.startsWith("https:") ||
                filePath.startsWith("ftp:") ||
                filePath.startsWith("rtp:") ||
                filePath.startsWith("rtsp:") ||
                filePath.startsWith("mms:"))
            return true;
        return false;
    }

    public static boolean existFile(String filePath) {
        if (EmptyString(filePath)) return false;
        if (filePath.startsWith("http:") ||
                filePath.startsWith("https:") ||
                filePath.startsWith("ftp:") ||
                filePath.startsWith("rtp:") ||
                filePath.startsWith("rtsp:") ||
                filePath.startsWith("mms:"))
            return true;

        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile())
                return true;
            else
                return false;
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
        return false;
    }

    public static boolean existDirectory(String filePath) {
        if (EmptyString(filePath)) return false;
        File file = new File(filePath);
        if (file.exists() && file.isDirectory())
            return true;
        else
            return false;
    }

    public static void CheckDirsExists(@NonNull String pathDir) {
        if (EmptyString(pathDir)) return;
        File dirs = new File(pathDir);
        if (!dirs.exists())
            dirs.mkdirs();
    }

    //无法获得不存在资源的文件名
    public static String getFileName(String filePath) {
        String fileName = filePath;
        if (filePath.startsWith("http:") ||
                filePath.startsWith("https:") ||
                filePath.startsWith("ftp:") ||
                filePath.startsWith("rtp:") ||
                filePath.startsWith("rtsp:") ||
                filePath.startsWith("mms:")) {//url的文件名
            int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash >= 0) {
                lastSlash++;
                if (lastSlash < filePath.length()) {
                    {
                        fileName = filePath.substring(lastSlash);
                        return fileName;
                    }
                }
            }
        } else//本地文件名
        {
            File file = new File(filePath);
            if (file.exists()) {
                if (file.isFile())
                    return file.getName();
                else
                    return null;
            }
        }
        return null;
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    public static void setFilePermissions(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                file.setReadable(true, false);
                file.setWritable(true, false);
            } catch (Exception e) {
                //e.printStackTrace();
                MMLog.log(TAG, e.toString());
            }
        }
    }

    public static void setFilePermissions2(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                String permissionsCmd = "chmod -R 777 " + filePath;
                Runtime.getRuntime().exec(permissionsCmd);
            } catch (Exception e) {
                //e.printStackTrace();
                MMLog.log(TAG, e.toString());
            }
        }
    }

    public static long getFileSize(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists() && file.isFile())
                return file.length();
            else
                return 0;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0;
    }

    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) file.delete();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFiles(String filePath) {
        List<File> files = getFiles(filePath);
        if (files == null) return true;
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

    public static boolean renameFile(String fromFilePathName, String newFilePathName) {
        boolean bRet = false;
        try {
            File fromFile = new File(fromFilePathName);
            File newFile = new File(newFilePathName);
            if (!fromFile.exists()) {
                MMLog.log(TAG, "fromFile not exist " + fromFile);
                return false;
            }
            if (!newFile.exists()) {  //  确保新的文件名不存在
                bRet = fromFile.renameTo(newFile);
            } else {
                MMLog.log(TAG, "file already exist " + newFilePathName);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, "renameFile failed to " + newFilePathName);
        }
        return bRet;
    }

    public static boolean streamCopy(String FromFile, String ToFile) {
        try {
            InputStream inputStream = new FileInputStream(FromFile); //读入原文件
            OutputStream outputStream = new FileOutputStream(ToFile);
            byte[] buffer = new byte[1024];
            int byteRead = 0;

            while ((byteRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.close();
            //return true;
        } catch (Exception e) {
            MMLog.log(TAG, e.toString());
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean pathCopy(String fromFile, String toFile) {
        final Path ff = Paths.get(fromFile);
        final Path tf = Paths.get(toFile);
        try {
            Files.copy(ff, tf, NOFOLLOW_LINKS);
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
            return false;
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean channelTransferTo(String fromFile, String toFile) {
        //final Path ff = Paths.get(fromFile);
        //final Path tf = Paths.get(toFile);
        try {
            FileChannel fileChannel_f = new FileInputStream(fromFile).getChannel();
            FileChannel fileChannel_t = new FileOutputStream(toFile).getChannel();

            //FileChannel fileChannel_f = (FileChannel.open(ff, EnumSet.of(StandardOpenOption.READ)));
            //FileChannel fileChannel_t = (FileChannel.open(tf, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)));
            fileChannel_f.transferTo(0L, fileChannel_f.size(), fileChannel_t);
            fileChannel_f.close();
            fileChannel_t.close();

            return true;
        } catch (IOException ex) {
            //System.err.println(ex);
            MMLog.log(TAG, ex.toString());
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
            //System.err.println(ex);
            MMLog.log(TAG, ex.toString());
        }
        return l;
    }

    public static boolean bufferCopyFile(String fromFile, String toFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel fileChannelOutput = null;
        FileChannel fileChannelInput = null;
        BufferedInputStream inbuff = null;
        BufferedOutputStream outbuff = null;
        try {
            fileInputStream = new FileInputStream(fromFile);
            inbuff = new BufferedInputStream(fileInputStream);
            fileOutputStream = new FileOutputStream(toFile); // 新建文件输出流并对它进行缓冲
            outbuff = new BufferedOutputStream(fileOutputStream);
            //int fileVolume = (int) (dirSize / (1024 * 1024));
            fileChannelOutput = fileOutputStream.getChannel();
            fileChannelInput = fileInputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            //long transferSize = 0;
            //int progress = 0;
            while (fileChannelInput.read(buffer) != -1) {
                buffer.flip();
                fileChannelOutput.write(buffer);
                buffer.clear();
            }

        } catch (FileNotFoundException e) {
            MMLog.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
            return false;
        } catch (IOException e) {
            MMLog.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
            return false;
        } finally {
            try {
                outbuff.flush();
                inbuff.close();
                outbuff.close();
                fileOutputStream.close();
                fileInputStream.close();
                fileChannelOutput.close();
                fileChannelInput.close();
                return true;
            } catch (IOException e) {
                //e.printStackTrace();
                return false;
            }
        }
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

    //获取目录，没有就创建
    public static String getDownloadDir(String downloadDir) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        if (existDirectory(downloadDir))
            return downloadDir;
        if (NotEmptyString(downloadDir))
            path = downloadDir;
        if (existDirectory(path)) {
            return path;
        }
        File file = null;
        try {
            file = new File(path);
            if (file.exists() && file.isFile()) {
                return null;//是一个已经存在的文件，返回
            } else {
                file.mkdirs();//创建目录
                //MMLog.log(TAG,"make dir = " + path);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
        if (file != null)
            return file.getAbsolutePath();
        else
            return null;
    }

    /**
     * 判断SD卡是否挂载
     */
    public static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public static String getExtNameFromPathName(String fileName) {
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition != -1) {
            return fileName.substring(dotPosition + 1);
        }
        return null;
    }

    public static String getFileNameFromPathName(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.getName();
        }
        return null;
    }

    public static String getFilePathFromPathName(String filePathName) {
        int dotPosition = filePathName.lastIndexOf('/');
        if (dotPosition > 0) {
            return filePathName.substring(0, dotPosition);
        }
        return null;
    }

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
            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                    p2 = path2.getPath();
                    USBDiscs.put(p1, p2);
                    //MLog.log(TAG, "getUDiscName DeviceName = " + p1 + ":" + p2);
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
                    //MLog.log(TAG, "getUDiscPath DeviceName = " + p1 + ":" + p2);
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
            int dataIndex = c.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int sizeIndex = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

            while (c.moveToNext()) {
                String path = c.getString(dataIndex);
                if (FileUtils.getFileType(path) == fileType) {
                    if (!FileUtils.existFile(path)) {
                        continue;
                    }
                    long size = c.getLong(sizeIndex);
                    FileBean fileBean = new FileBean(path, 0);
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

    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getAbsolutePath();
        } else {
            return context.getCacheDir().getAbsolutePath();
        }
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
            MMLog.log("FilesManager", "Not found file:" + filePath);
            return;
        }
        try {
            out = new FileOutputStream(file, append);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);

        } catch (Exception e) {
            e.printStackTrace();
            //log(Tag, "写入文件失败");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //log(Tag, e.getMessage());
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
                MMLog.log("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            MMLog.log("TestFile", "Error on write File:" + e);
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
            MMLog.log("error:", e + "");
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

    public static String md5(String str) {
        if (str == null) return "null";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
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
        return "null";
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


    public static void getFiles(String FilePath, List<String> FileList) {
        //List<String> FileList = new ArrayList<String>();
        File path = new File(FilePath);
        File[] files = path.listFiles();
        getFileList(files, FileList);
        //return FileList;
    }

    private static void getFileList(File[] files, List<String> FileList) {
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            String filePathName = null;
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileList(file.listFiles(), FileList);
                } else {
                    filePathName = file.getPath();// +"  "+ file.getName() ;
                    FileList.add(filePathName);
                }
            }
        }
    }

}