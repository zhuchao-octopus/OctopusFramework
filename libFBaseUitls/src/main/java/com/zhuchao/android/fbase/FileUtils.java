package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.ByteUtils.BytesToHexStr;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import android.annotation.SuppressLint;
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
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zhuchao.android.fbase.bean.DirBean;
import com.zhuchao.android.fbase.bean.FileBean;
import com.zhuchao.android.fbase.bean.ImgFolderBean;
import com.zhuchao.android.fbase.bean.LMusic;
import com.zhuchao.android.fbase.bean.LVideo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Objects;
import java.util.Random;

public class FileUtils {
    private final static String TAG = "FileUtils";
    public static final int TYPE_DOC = 0;
    public static final int TYPE_APK = 1;
    public static final int TYPE_ZIP = 2;

    @SuppressWarnings("unchecked")
    public static <T> T toType(Object obj) {
        return (T) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T toType(Object obj, Class<T> classOfT) {
        return (T) obj;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            //MMLog.e(TAG, "fromJson failed!");
            return null;
        }
    }

    public static void WaitingFor(long Millis) {
        try {
            Thread.sleep(Millis);//等待任务完成
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
    }

    public static int getRandom(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    public static boolean EmptyString(String str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean NotEmptyString(String str) {
        return !TextUtils.isEmpty(str);
    }

    public static ObjectList getPartitions() {
        return getPartitions(null);
    }

    public static ObjectList getPartitions(ObjectList Result) {
        ObjectList Partitions = null;
        long totalSize = 0;
        String cmd = "cat /proc/partitions";

        if (Result == null) Partitions = new ObjectList();
        else Partitions = Result;

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            //StringBuilder data = new StringBuilder();
            //BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error = null;
            //while ((error = ie.readLine()) != null && !error.equals("null")) {
            //    data.append(error).append("\n");
            //}
            String line = null;
            while ((line = in.readLine()) != null && !line.equals("null")) {
                line = line.trim();
                if (EmptyString(line)) continue;
                line = line.replaceAll(" +", ",");
                String[] partition = line.split(",");
                //MMLog.log(TAG, "partition = " + line + " length = " + partition.length);
                if (partition[2].equals("#blocks") || partition[3].equals("name")) continue;
                if (partition.length == 4) {
                    try {
                        if (partition[0].trim() != "179") totalSize = totalSize + Long.parseLong(partition[2].trim());
                        Partitions.putLong(partition[3].trim(), Long.valueOf(partition[2].trim()));
                    } catch (NumberFormatException e) {
                        //e.printStackTrace();
                        MMLog.e(TAG, line + "," + e.getMessage());
                    }
                }
            }
            Partitions.putLong("totalSize", totalSize);
            //ie.close();
            in.close();
        } catch (IOException ioe) {
            MMLog.e(TAG, ioe.toString());
        }
        return Partitions;
    }

    public static ObjectList getFileSystemPartitions(ObjectList Result) {
        ObjectList Partitions = null;
        String cmd = "df";

        if (Result == null) Partitions = new ObjectList();
        else Partitions = Result;

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            //StringBuilder data = new StringBuilder();
            //BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error = null;
            //while ((error = ie.readLine()) != null && !error.equals("null")) {
            //    data.append(error).append("\n");
            //}
            String line = null;
            while ((line = in.readLine()) != null && !line.equals("null")) {
                line = line.trim();
                if (EmptyString(line)) continue;
                line = line.replaceAll(" +", ",");
                String[] partition = line.split(",");
                //MMLog.log(TAG, "partition = " + line + " length = " + partition.length);
                if (partition[0].equals("Filesystem") || partition[2].equals("#blocks") || partition[3].equals("name")) continue;
                if (partition.length == 6) {
                    try {
                        Partitions.putLong(partition[0].trim(), Long.valueOf(partition[1].trim()));
                    } catch (NumberFormatException e) {
                        //e.printStackTrace();
                        MMLog.e(TAG, line + "," + e.getMessage());
                    }
                }
            }
            ///Partitions.putLong("totalSize",totalSize);
            ///ie.close();
            in.close();
        } catch (IOException ioe) {
            MMLog.e(TAG, ioe.toString());
        }
        return Partitions;
    }

    public static long getAvailableInternalStorageSpace() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    private static void makeFilePath(String filePath, String fileName) {
        File file;
        makeDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
    }

    private static void makeDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public static boolean MakeDirsExists(@NonNull String pathDir) {
        if (EmptyString(pathDir)) return false;
        File dirs = new File(pathDir);
        if (!dirs.exists()) {
            return dirs.mkdirs();
        }
        return true;
    }

    public static boolean isExternalLinks(String filePath) {
        if (EmptyString(filePath)) return false;
        return filePath.startsWith("http:") || filePath.startsWith("https:") || filePath.startsWith("ftp:") || filePath.startsWith("rtp:") || filePath.startsWith("rtsp:") || filePath.startsWith("mms:");
    }

    public static boolean existFile(String filePath) {
        if (EmptyString(filePath)) return false;
        if (filePath.startsWith("http:") || filePath.startsWith("https:") || filePath.startsWith("ftp:") || filePath.startsWith("rtp:") || filePath.startsWith("rtsp:") || filePath.startsWith("mms:"))
            return true;

        try {
            File file = new File(filePath);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            MMLog.log(TAG, e.toString());//e.printStackTrace();
        }
        return false;
    }

    public static boolean existDirectory(String filePath) {
        if (EmptyString(filePath)) return false;
        File file = new File(filePath);
        return file.exists() && file.isDirectory();
    }

    public static boolean existFileInDirectory(String fileName, String filePath) {
        if (EmptyString(fileName)) return false;
        List<File> files = getFiles(filePath);
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                if (file.getName().contains(fileName)) return true;
                if (fileName.contains(file.getName())) return true;
            }
        }
        return false;
    }

    public static String getFileInDirectory(String fileName, String filePath) {
        if (EmptyString(fileName)) return null;
        List<File> files = getFiles(filePath);
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                if (file.getName().equals(fileName)) return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String getFileInDirectory2(String fileName, String filePath) {
        if (EmptyString(fileName)) return null;
        List<File> files = getFiles(filePath);
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                if (file.getName().contains(fileName)) return file.getAbsolutePath();
                if (fileName.contains(file.getName())) return file.getAbsolutePath();
            }
        }
        return null;
    }

    //无法获得不存在资源的文件名
    public static String getFileName(String filePath) {
        String fileName = filePath;
        if (filePath.startsWith("http:") || filePath.startsWith("https:") || filePath.startsWith("ftp:") || filePath.startsWith("rtp:") || filePath.startsWith("rtsp:") || filePath.startsWith("mms:")) {//url的文件名
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
                if (file.isFile()) return file.getName();
                else return null;
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
            if (file.exists() && file.isFile()) return file.length();
            else return 0;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return file.delete();
        return false;
    }

    public static boolean deleteFiles(String filePath) {
        List<File> files = getFiles(filePath);
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                file.delete();
            }
        }
        return true;
    }

    public static boolean deleteDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            MMLog.log(TAG, "Dir is not exit! ->" + directoryPath);
            return false;
        }

        if (!directory.canWrite()) {
            MMLog.log(TAG, "Dir is not permit writing! ->" + directoryPath);
            return false;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.getAbsolutePath());
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    public static String findDirFromPath(String DirName, String Path) {
        File directory = new File(Path);
        String foundPath = "";
        if (directory.exists() && directory.isDirectory()) {
            if (directory.getName().equals(DirName)) {
                MMLog.log(TAG, "findDirFromPath ->" + DirName + ":" + directory.getAbsolutePath());
                foundPath = directory.getAbsolutePath();
            } else {
                File[] files = directory.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            foundPath = findDirFromPath(DirName, file.getAbsolutePath());
                            if (!Objects.equals(foundPath, "")) break;
                        }
                    }
                }
            }
        }
        return foundPath;
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
        ///final Path ff = Paths.get(fromFile);
        ///final Path tf = Paths.get(toFile);
        try {
            FileChannel fileChannel_f = new FileInputStream(fromFile).getChannel();
            FileChannel fileChannel_t = new FileOutputStream(toFile).getChannel();

            ///FileChannel fileChannel_f = (FileChannel.open(ff, EnumSet.of(StandardOpenOption.READ)));
            ///FileChannel fileChannel_t = (FileChannel.open(tf, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)));
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
            outbuff.flush();
            inbuff.close();
            outbuff.close();
            fileOutputStream.close();
            fileInputStream.close();
            fileChannelOutput.close();
            fileChannelInput.close();
            return true;
        } catch (IOException e) {
            MMLog.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
            return false;
        }
    }

    public static List<File> listFilesByExtName(String filePath, String extName) {
        File file = new File(filePath);
        List<File> list = new ArrayList<>();
        File[] fileArray = null;
        if (file.exists() && file.isDirectory()) fileArray = file.listFiles();

        if (fileArray != null) {
            for (File f : fileArray) {
                if (f.isFile() && f.getAbsolutePath().endsWith(extName)) {
                    list.add(0, f);
                } else {
                    getFiles(f.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static List<File> getFiles(String filePath) {
        File file = new File(filePath);
        List<File> list = new ArrayList<>();
        File[] fileArray = null;
        if (file.exists() && file.isDirectory()) fileArray = file.listFiles();

        if (fileArray != null) {
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

    public static void getFiles(String FilePath, List<String> FileList) {
        //List<String> FileList = new ArrayList<String>();
        File path = new File(FilePath);
        File[] files = path.listFiles();
        getFileList(files, FileList);
        //return FileList;
    }

    public static List<String> getDirMediaFiles(String filePath, int fileType) {
        List<String> filesList = new ArrayList<String>();
        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return filesList;

            for (File f : files) {
                if (fileType == DataID.MEDIA_TYPE_ID_VIDEO && MediaFile.isVideoFile(f.getAbsolutePath())) {
                    filesList.add(f.getAbsolutePath());
                } else if (fileType == DataID.MEDIA_TYPE_ID_AUDIO && MediaFile.isAudioFile(f.getAbsolutePath())) {
                    filesList.add(f.getAbsolutePath());
                } else if (fileType == DataID.MEDIA_TYPE_ID_AllFILE) {
                    filesList.add(f.getAbsolutePath());
                }
            }
        }
        return filesList;
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

    public static int getFileType(String path) {
        path = path.toLowerCase();
        if (path.endsWith(".doc") || path.endsWith(".docx") || path.endsWith(".xls") || path.endsWith(".xlsx") || path.endsWith(".ppt") || path.endsWith(".pptx")) {
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
        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

        if (existDirectory(downloadDir)) return downloadDir;

        if (existDirectory(path)) {
            return path;
        }

        if (existDirectory(path1)) {
            return path1;
        }

        if (EmptyString(downloadDir)) {
            downloadDir = path1;
        }
        //不存在，创建新目录
        File file = null;
        try {
            file = new File(downloadDir);
            file.mkdirs();//创建目录
            //MMLog.log(TAG,"make dir = " + path);
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
        if (file != null) return file.getAbsolutePath();
        else return null;
    }

    public static String getDirBaseExternalStorageDirectory(String myDirName) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (NotEmptyString(myDirName)) path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + myDirName;
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
        if (file != null) return file.getAbsolutePath();
        else return null;
    }

    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getAbsolutePath();
        } else {
            return context.getCacheDir().getAbsolutePath();
        }
    }

    public static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static String getExtNameFromPathName(String fileName) {
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition != -1) {
            return fileName.substring(dotPosition + 1);
        }
        return null;
    }

    //合法的文件按名字必须带扩展名，否则被认为是目录
    public static String extractFileName(String filePathName) {
        if (EmptyString(filePathName)) return null;
        if (filePathName.endsWith("\\")) return null;
        if (filePathName.endsWith("/")) return null;
        //filePathName = filePathName.replace("\\\\","\\");
        filePathName = filePathName.replace("//", "/");
        String[] split = filePathName.split("/");
        String fileName = split[split.length - 1];
        if (getExtNameFromPathName(fileName) == null) return null;//没有扩展名，则当作目录处理
        else return fileName;//合法的文件按名字必须带扩展名
    }

    public static String getFileNameFromPathName(String filePathName) {
        if (EmptyString(filePathName)) return null;
        File file = new File(filePathName);
        if (file.exists() && file.isFile()) {
            return file.getName();
        } else if (file.exists() && file.isDirectory()) {
            return null;
        } else {//从字符串中截取文件名
            return extractFileName(filePathName);
        }
    }

    public static String getFilePathFromPathName(String filePathName) {
        int dotPosition = filePathName.lastIndexOf('/');
        if (dotPosition > 0) {
            return filePathName.substring(0, dotPosition);
        }
        return null;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        // System.out.println("修改时间[2] " + formatter.format(cal.getTime()));
        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.getTime());
    }

    @SuppressLint("PrivateApi")
    public static Map<String, String> getMobileDiscName(Context context) {
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
            mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            //getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");

            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            volumes = (List<?>) getVolumes.invoke(mStorageManager);
            assert volumes != null;
            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    if (path2 != null) {
                        p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                        p2 = path2.getPath();
                        USBDiscs.put(p1, p2);
                        //MLog.log(TAG, "getUDiscName DeviceName = " + p1 + ":" + p2);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return USBDiscs;
    }

    @SuppressLint("PrivateApi")
    public static String getMobileDiscPath(Context context) {
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
            mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            //getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");
            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            volumes = (List<?>) getVolumes.invoke(mStorageManager);

            assert volumes != null;
            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    String p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                    assert path2 != null;
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

    //    METADATA_KEY_ALBUM ：检索数据源的专辑标题信息的元数据键。
    //    METADATA_KEY_ALBUMARTIST： 检索与数据源相关的表演者或艺术家的信息的元数据键。
    //    METADATA_KEY_ARTIST：检索有关数据源的艺术家的信息的元数据键。
    //    METADATA_KEY_AUTHOR：检索有关数据源作者的信息的元数据键
    //    METADATA_KEY_BITRATE：此键检索平均比特率（以比特/秒），如果可用的话。
    //    METADATA_KEY_CD_TRACK_NUMBER：元数据关键字，用于检索描述原始数据记录中音频数据源的顺序的数字字符串。
    //    METADATA_KEY_COMPILATION：检索音乐专辑编辑状态的元数据键
    //    METADATA_KEY_COMPOSER：检索有关数据源的作曲家的信息的元数据键
    //    METADATA_KEY_DATE：检索或创建数据源时的日期的元数据键
    //    METADATA_KEY_DISC_NUMBER：用于检索描述音频数据源的集合的哪一部分的数字字符串的元数据键
    //    METADATA_KEY_DURATION：检索数据源回放持续时间的元数据键
    //    METADATA_KEY_GENRE:检索数据源的内容类型或类型的元数据键
    //    METADATA_KEY_HAS_AUDIO:如果存在此密钥，则媒体包含音频内容
    //    METADATA_KEY_HAS_VIDEO:如果存在此密钥，则媒体包含视频内容
    //    METADATA_KEY_MIMETYPE:检索数据源MIME类型的元数据键。一些示例MIME类型包括："video/mp4", "audio/mp4", "audio/amr-wb"
    //    METADATA_KEY_NUM_TRACKS:元数据键，用于检索数据源（如MP4或3GPP文件）中的音轨的数目，如音频、视频、文本。
    //    METADATA_KEY_TITLE:检索数据源标题的元数据键
    //    METADATA_KEY_VIDEO_HEIGHT:如果媒体包含视频，则该键检索其高度
    //    METADATA_KEY_VIDEO_ROTATION:此键检索视频旋转角度的程度，如果可用的话。视频旋转角度可以是0, 90, 180度，也可以是270度
    //    METADATA_KEY_VIDEO_WIDTH:如果媒体包含视频，则该密钥检索其宽度
    //    METADATA_KEY_WRITER:检索数据源的作者（如歌词作者）信息的元数据键
    //    METADATA_KEY_YEAR:检索创建或修改数据源的一年的元数据密钥
    //    OPTION_CLOSEST:此选项与GETFrimeTimeTime（long，int）一起使用，以检索与最接近或给定时间的数据源相关联的帧（不一定是关键帧）
    //    OPTION_CLOSEST_SYNC:（时间）或给定时间的数据源相关联的同步（或密钥）帧。
    public static List<LMusic> getLocalSystemMusics(Context context) {
        ArrayList<LMusic> musics = new ArrayList<>();
        ContentResolver mContentResolver = context.getContentResolver();
        try (Cursor c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)) {
            if (c == null) return musics;
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径

                if (!new File(path).exists()) continue;

                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // 歌曲名
                String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
                String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));// 大小
                int duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 时长
                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));// 歌曲的id
                int albumId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑ID
                LMusic music = new LMusic(id, albumId, name, path, album, artist, size, duration);
                musics.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return musics;
    }

    public static List<LVideo> getLocalSystemVideos(Context context) {
        List<LVideo> videos = new ArrayList<LVideo>();
        ContentResolver mContentResolver = context.getContentResolver();
        try (Cursor c = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER)) {
            if (c == null) return videos;
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));// 路径
                if (!new File(path).exists()) continue;

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
        }
        return videos;
    }

    // 获取视频缩略图
    public static Bitmap getVideoThumbnail(Context context, int video_id, String filePath) {
        Bitmap bitmap = null;
        Size thumbSize = new Size(100, 100);
        Uri thumbUri = Uri.fromFile(new File(filePath));
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver mContentResolver = context.getContentResolver();
        //options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bitmap = context.getContentResolver().loadThumbnail(thumbUri, thumbSize, null);
            } else {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, video_id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }
        } catch (IOException e) {
            ///throw new RuntimeException(e);
        }
        return bitmap;
    }

    ///* 从文件当中获取专辑封面位图
    public static Bitmap getMusicThumbnail(Context context, long music_id, long album_id) {
        Bitmap bitmap = null;
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

        if (album_id < 0 && music_id < 0) return null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (album_id < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + music_id + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");

                if (pfd != null) fd = pfd.getFileDescriptor();
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            ///e.printStackTrace();
        }
        return bitmap;
    }


    ///* 通过文件类型得到相应文件的集合
    public static List<FileBean> getFilesByType(Context context, int fileType) {
        List<FileBean> files = new ArrayList<FileBean>();
        ContentResolver mContentResolver = context.getContentResolver();
        // 扫描files文件库
        try (Cursor c = mContentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{"_id", "_data", "_size"}, null, null, null)) {
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
        }
        return files;
    }

    public static List<ImgFolderBean> getImageFolders(Context context) {
        List<ImgFolderBean> folders = new ArrayList<ImgFolderBean>();
        ContentResolver mContentResolver = context.getContentResolver();
        // 扫描图片
        try (Cursor c = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED)) {
            List<String> mDirs = new ArrayList<String>();//用于保存已经添加过的文件夹目录
            while (c.moveToNext()) {
                @SuppressLint("Range") String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));// 路径
                File parentFile = new File(path).getParentFile();
                if (parentFile == null) continue;

                String dir = parentFile.getAbsolutePath();
                if (mDirs.contains(dir))//如果已经添加过
                    continue;

                mDirs.add(dir);//添加到保存目录的集合中
                ImgFolderBean folderBean = new ImgFolderBean();
                folderBean.setDir(dir);
                folderBean.setFistImgPath(path);
                if (parentFile.list() == null) continue;
                int count = Objects.requireNonNull(parentFile.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png");
                    }
                })).length;

                folderBean.setCount(count);
                folders.add(folderBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folders;
    }

    public static List<String> ReadTxtFile(String filePath) {
        List<String> newList = new ArrayList<>();
        try {
            File file = new File(filePath);
            int count = 0;//初始化 key值
            if (file.isFile() && file.exists()) {//文件存在
                InputStreamReader isr = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    isr = new InputStreamReader(Files.newInputStream(file.toPath()));
                }
                if (isr == null) return newList;

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String txtString, String filePath, String fileName) {
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = txtString + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                //MMLog.log( TAG,"Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            MMLog.log(TAG, "Error on write File:" + e);
        }
    }

    public static void writeFile(String filePath, String data, boolean append) {
        FileOutputStream out;
        BufferedWriter writer = null;
        File file = new File(filePath);
        if (!file.exists()) {
            MMLog.log(TAG, "Not found file:" + filePath);
            return;
        }
        try {
            out = new FileOutputStream(file, append);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getRealFilePathFromUri(Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) data = uri.getPath();
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

    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) return null;
        if (DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
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
            if (isGooglePhotosUri(imageUri)) return imageUri.getLastPathSegment();
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
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public static String MD5(String str) {
        if (str == null) return "null";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            ///e.printStackTrace();
        }
        return "null";
    }

    public static String getMD5(File f) {
        if (!f.exists() || !f.isFile()) return null;
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
        } catch (NoSuchAlgorithmException | IOException e) {
            //e.printStackTrace();
        }
        if (bi != null) return bi.toString(16);
        else return "";
    }

    public static String getFileMD5(File file) {
        if (!file.exists() || !file.isFile()) return null;

        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            fileInputStream.close();
            //return new String(Hex.encodeHex(MD5.digest()));
            return BytesToHexStr(MD5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileMD5(String filePathName) {
        if (EmptyString(filePathName)) return null;
        File file = new File(filePathName);
        return getFileMD5(file);
    }

    public static Object file2Object(FileInputStream fis) {

        //FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            //fis = new FileInputStream(fileName);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void object2File(Object obj, FileOutputStream fos) {
        ObjectOutputStream oos = null;
        //FileOutputStream fos = null;
        try {
            //fos = new FileOutputStream(new File(outputFile));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static ObjectList getTopLevelDir(Context context) {
        Map<String, String> hashMap;
        ObjectList objectList = new ObjectList();
        hashMap = getMobileDiscName(context);
        objectList.putString("Local Storage", "/storage/emulated/0");
        for (Map.Entry<String, String> entity : hashMap.entrySet()) {
            objectList.putString(entity.getKey(), entity.getValue());
        }
        return objectList;
    }

    public static void getSubDirList(String topDir, ObjectList dirList, int searchMode, int fileType) {//,int showMod
        File file = new File(topDir);
        File[] fileArray = null;
        if (file.exists() && file.isDirectory()) fileArray = file.listFiles();

        if (fileArray != null) {
            for (File f : fileArray) {
                if (f.isFile()) {
                    String dirName = f.getParent();
                    DirBean dirBean = (DirBean) dirList.getObject(dirName);
                    if (dirBean == null) {
                        dirBean = new DirBean(Objects.requireNonNull(f.getParentFile()).getName(), dirName);
                    }
                    if (fileType == DataID.MEDIA_TYPE_ID_VIDEO && MediaFile.isVideoFile(f.getAbsolutePath())) {
                        ///dirList.putString(f.getParent(), f.getParent());
                        ///fileList.putString(f.getName(), f.getAbsolutePath());
                        dirList.addObject(f.getParent(), dirBean);
                        dirBean.add(f.getAbsolutePath());
                    } else if (fileType == DataID.MEDIA_TYPE_ID_AUDIO && MediaFile.isAudioFile(f.getAbsolutePath())) {
                        ///dirList.putString(f.getParent(), f.getParent());
                        ///fileList.putString(f.getName(), f.getAbsolutePath());
                        dirList.addObject(f.getParent(), dirBean);
                        dirBean.add(f.getAbsolutePath());
                    } else if (fileType == DataID.MEDIA_TYPE_ID_AllFILE) {
                        ///dirList.putString(f.getParent(), f.getParent());
                        ///fileList.putString(f.getName(), f.getAbsolutePath());
                        dirList.addObject(f.getParent(), dirBean);
                        dirBean.add(f.getAbsolutePath());
                    }
                } else {
                    ///dirList.putString(f.getName(), f.getAbsolutePath());
                    if (searchMode == DataID.MEDIA_TYPE_ID_AllDIR) {
                        getSubDirList(f.getAbsolutePath(), dirList, searchMode, fileType);
                    }
                }
            }
        }
    }

    public static ObjectList getAllSubMediaDirList(Context context, String topDir, int searchMode, int fileType) {//,int showMod
        File file = new File(topDir);
        ///File[] fileArray = null;
        ///if (file.exists() && file.isDirectory()) fileArray = file.listFiles();
        ObjectList objectList = new ObjectList();
        ObjectList dirList = new ObjectList();

        if (EmptyString(topDir) || "/".equals(topDir)) objectList = getTopLevelDir(context);

        if (objectList.getCount() > 0) {
            for (HashMap.Entry<String, Object> entry : objectList.getAll().entrySet()) {
                getSubDirList(entry.getValue().toString(), dirList, searchMode, fileType);
            }
        } else if (file.exists() && file.isDirectory()) {
            getSubDirList(file.getAbsolutePath(), dirList, searchMode, fileType);
        }
        return dirList;
    }

    public static List<DirBean> getAllSubDirList(Context context, String topDir, int searchMode, int fileType) {//,int showMod
        List<DirBean> list = new ArrayList<>();
        ObjectList dirList = getAllSubMediaDirList(context, topDir, searchMode, fileType);
        for (Map.Entry<String, Object> entity : dirList.getAll().entrySet()) {
            list.add((DirBean) entity.getValue());
        }
        return list;
    }

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
}
