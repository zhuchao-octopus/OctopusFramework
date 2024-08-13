package com.common.util;

import static com.common.util.MachineConfig.VENDOR_DIR;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import com.zhuchao.android.fbase.MMLog;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Util {
    private final static String tag = "Util";

    public static void byteArrayCopy(byte[] dest, byte[] src, int dStart, int sStart, int len) {
        if (len >= 0) System.arraycopy(src, sStart, dest, dStart, len);
    }

    // 十六进制转十进制
    public static int HToO(String a) {
        a = a.toLowerCase();
        return (Integer.parseInt(toD(a, 16)));
    }

    // 十六进制转二进制
    public static String HToB(String a) {
        String b = Integer.toBinaryString(Integer.parseInt(toD(a, 16)));
        return b;
    }

    // 二进制转十六进制
    public static String BToH(String a) {
        // 将二进制转为十进制再从十进制转为十六进制
        String b = Integer.toHexString(Integer.parseInt(toD(a, 2)));
        return b;
    }

    // 任意进制数转为十进制数
    public static String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (r + formatting(a.substring(i, i + 1)) * Math.pow(b, a.length() - i - 1));
        }
        return String.valueOf(r);
    }

    // 将十六进制中的字母转为对应的数字
    public static int formatting(String a) {
        int i = 0;
        for (int u = 0; u < 10; u++) {
            if (a.equals(String.valueOf(u))) {
                i = u;
            }
        }
        if (a.equals("a")) {
            i = 10;
        }
        if (a.equals("b")) {
            i = 11;
        }
        if (a.equals("c")) {
            i = 12;
        }
        if (a.equals("d")) {
            i = 13;
        }
        if (a.equals("e")) {
            i = 14;
        }
        if (a.equals("f")) {
            i = 15;
        }
        return i;
    }

    // 将十进制中的数字转为十六进制对应的字母
    public static String formattingH(int a) {
        String i = String.valueOf(a);
        switch (a) {
            case 10:
                i = "a";
                break;
            case 11:
                i = "b";
                break;
            case 12:
                i = "c";
                break;
            case 13:
                i = "d";
                break;
            case 14:
                i = "e";
                break;
            case 15:
                i = "f";
                break;
        }
        return i;
    }

    public static void clearBuf(byte[] buf) {
        if (buf != null) {
            Arrays.fill(buf, (byte) 0);
        }
    }

    public static void setFileValue(String file, String value) {
        try {

            FileOutputStream is = new FileOutputStream(file);
            DataOutputStream dis = new DataOutputStream(is);

            dis.write(value.getBytes());
            dis.close();
            is.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(tag, "" + e);
        }

    }

    public static void setFileValue(String path, int value) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }

            String writeString = String.valueOf(value);
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(writeString);
            pw.flush();
            pw.close();
            fos.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
    }

    public static void setFileValue(String path, byte[] buffer) {
        File file = new File(path);
        if (file.exists()) {
            try {
                FileOutputStream is = new FileOutputStream(file);
                DataOutputStream dis = new DataOutputStream(is);

                dis.write(buffer);
                dis.close();
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(tag, "" + e);
            }
        }
    }

    public static void setFileValue(byte[] buffer, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            try {
                FileOutputStream is = new FileOutputStream(file);
                DataOutputStream dis = new DataOutputStream(is);

                dis.write(buffer);
                dis.close();
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(tag, "" + e);
            }
        }
    }

    public static String getFileString(String path) {

        String topPackageName = null;

        FileReader fr = null;
        try {
            fr = new FileReader(path);
            BufferedReader reader = new BufferedReader(fr, 256);
            topPackageName = reader.readLine();
            reader.close();
            fr.close();
        } catch (Exception ignored) {
        }

        return topPackageName;

    }

    public static int getFileValue(String path) {
        int ret = -1;
        try {
            File file = new File(path);
            if (!file.exists()) {
                return -1;
            }

            FileInputStream inputStream = new FileInputStream(path);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String str = dataInputStream.readLine();
            dataInputStream.close();
            inputStream.close();
            try {
                if (str.startsWith("0x")) {
                    ret = Integer.parseInt(str.replace("0x", ""), 16);
                } else {
                    ret = Integer.parseInt(str, 10);
                }
            } catch (Exception e) {
                Log.e(tag, "" + e);
                ret = -1;
            }
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return ret;
    }

    public static int do_exec(String cmd) {
        try {
            int err = Runtime.getRuntime().exec(cmd).waitFor();
            Log.e("Util", "do_exec:" + cmd + ":err:" + err);
            return err;
        } catch (Exception e) {
            Log.e(tag, "" + e);
            return -1;
        }
    }

    @SuppressLint("PrivateApi")
    public static int setProperty(String key, String value) {
        try {
            MMLog.d("Util", "SystemProperties " + key + "=" + value);
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
            return 0;
        } catch (Exception e) {
            MMLog.e(tag, "" + e);
        }
        return -1;
    }

    @SuppressLint("PrivateApi")
    public static String getProperty(String key) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            return (String) get.invoke(c, key);
        } catch (Exception e) {
            MMLog.e(tag, "" + e);
        }
        return null;
    }

    public static void checkAKDRunning() {
        int i;
        String s;
        Util.doSleep(1);
        for (i = 0; i < 1000; ++i) {
            s = Util.getProperty("init.svc.akd");
            //			Log.d("Util", "::"+s);
            if (!"running".equals(s)) {
                break;
            }
            Util.doSleep(10);
        }
        Util.doSleep(1);
    }

    public static int sudoExec(String cmd) {
        Log.e("Util", "sudoExec:" + cmd);
        if (Build.VERSION.SDK_INT >= 25) {
            cmd = "akd:" + cmd;
            setProperty("ctl.start", cmd);
            checkAKDRunning();
            return 0;
        } else {
            return do_exec("start akd:" + cmd + "");
        }
    }

    public static int sudoExecNoCheck(String cmd) {
        Log.e("Util", "sudoExecNoCheck:" + cmd);
        if (Build.VERSION.SDK_INT >= 25) {
            cmd = "akd:" + cmd;
            setProperty("ctl.start", cmd);
            //		checkAKDRunning();
            return 0;
        } else {
            return do_exec("start akd:" + cmd + "");
        }
    }

    public static String getTimeString(long second) {
        int hours = (int) (second / (60 * 60));
        int minute = 0;
        if (hours > 0) {
            long tmp = second % (60 * 60);
            minute = (int) (tmp / 60);
        } else {
            minute = (int) (second / 60);
        }
        int seconds = (int) second % 60;
        StringBuffer sb = new StringBuffer();
        if (hours > 0) {
            sb.append(hours);
            sb.append(":");
        }
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        sb.append(":");
        if (seconds < 10) {
            sb.append("0");
        }
        sb.append(seconds);
        return sb.toString();
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     */
    public final static int daysBetween(Date smdate, Date bdate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param context     Context对象。
     * @param packageName 包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public final static String getProgramNameByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return name;
    }

    private final static Resources getResourcesByLocale(Resources res, Locale locale) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = locale;
        return new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }

    private static Locale mCurLocale;

    private final static void resetLocale(Resources res) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = mCurLocale;
        new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }

    public final static boolean isSimilarity(String s1, String s2, float f) {
        double similarity = EditDistance.getSimilarity(s1, s2);

        if (similarity >= f && similarity <= 1) {
            return true;
        }

        return false;

    }

    public final static boolean isSimilarity(String s1, String s2) {

        return isSimilarity(s1, s2, 0.75f);

    }

    public final static ActivityInfo getPackageNameByProgramName(Context context, String name) {
        EditDistance ed = new EditDistance();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : infos) {
            Context pakContext = null;
            try {
                pakContext = context.createPackageContext(info.activityInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {

                Log.e(tag, "" + e);
                break;
            }
            Resources res = pakContext.getResources();
            if (res != null) {
                // step:1 先查找启动类名字
                int labelRes = info.labelRes;
                String appName = null;
                if (labelRes == 0) {// step2:再查找应用名字
                    labelRes = info.activityInfo.applicationInfo.labelRes;
                }
                if (labelRes == 0) {
                    continue;
                }
                // step3:记录默认语言环境
                mCurLocale = res.getConfiguration().locale; // 得到当前的语言
                Resources resZh = getResourcesByLocale(res, Locale.SIMPLIFIED_CHINESE); // 得到指定语言的资源
                appName = resZh.getString(labelRes);
                // Log.d("Util", "zh appName = " + appName + ", locale = "
                // + resZh.getConfiguration().locale);
                if (appName != null) {
                    double similarity = ed.getSimilarity(appName, name);
                    // Log.d("Util", "appName = " + appName + ", name = " + name
                    // + ", similarity = " + similarity);
                    if (similarity >= 0.6 && similarity <= 1) {
                        // Log.d("Util", "appName = " + appName + ", name = " +
                        // name + ", similarity = " + similarity);
                        // Log.d("Util", "similar name --> " + name +
                        // ", pkgName = " + info.applicationInfo.packageName);
                        // step4:恢复默认语言环境
                        resetLocale(res);
                        return info.activityInfo;
                    }
                    // step4:恢复默认语言环境
                    resetLocale(resZh);
                }
            }
        }
        return null;
    }

    public static List<ResolveInfo> getActivityList(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : infos) {
            Context pakContext = null;
            try {
                pakContext = context.createPackageContext(info.activityInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {

                Log.e(tag, "" + e);
                break;
            }
            Resources res = pakContext.getResources();
            if (res != null) {
                mCurLocale = res.getConfiguration().locale; // 得到当前的语言
                Resources resZh = getResourcesByLocale(res, Locale.SIMPLIFIED_CHINESE); // 得到指定语言的资源
                String resName = resZh.getResourceName(info.activityInfo.applicationInfo.labelRes);
                String appName = resZh.getString(info.activityInfo.applicationInfo.labelRes);
                Log.d("Util", "zh appName = " + appName + ", resName = " + resName + ", locale = " + resZh.getConfiguration().locale);
                resetLocale(resZh);
                // info.activityInfo.applicationInfo.labelRes;
            }
        }
        return infos;
    }

    /**
     * 通过包名类名获取activity的名称。
     *
     * @param context     Context对象。
     * @param packageName 包名。
     * @param clzName     类名。
     * @return 返回activity的名称。
     */
    public final static String getActivityNameByPkgNameAndClzName(Context context, String packageName, String clzName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            ComponentName component = new ComponentName(packageName, clzName);
            ActivityInfo ai = pm.getActivityInfo(component, ApplicationInfo.FLAG_INSTALLED);
            if (ai != null) {
                name = ai.loadLabel(pm).toString();
            }
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return name;
    }

    public static String fileMD5(String inputFile) throws IOException {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;
            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            return byteArrayToHex(resultByteArray);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static String byteArrayToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        return hs;
    }

    // need system id
    private static int mKey = KeyEvent.KEYCODE_UNKNOWN;
    private static Instrumentation mInst;

    public static void doKeyEvent(int value) {
        mKey = value;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mKey != KeyEvent.KEYCODE_UNKNOWN) {
                    try {
                        if (mInst == null) {
                            mInst = new Instrumentation();
                        }
                        mInst.sendKeyDownUpSync(mKey);
                    } catch (Exception e) {
                        Log.e("Exception when sendPointerSync", "" + e);
                    }
                    mKey = KeyEvent.KEYCODE_UNKNOWN;
                }
            }
        });
        t.start();
    }

    public static void doSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            Log.d("allen", oldPath + ":::::" + newPath);
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.close();
                inStream.close();
                return true;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            Log.e(tag, "" + e);

        }
        return false;

    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            Log.e(tag, "" + e);

        }

    }

    public final static void setBeepToMcu() {
        Util.setFileValue("/sys/class/ak/source/beep", 2);
    }


    public final static void zeroBuf(byte[] buf) {
        if (buf != null) {
            for (int i = 0; i < buf.length; ++i) {
                buf[i] = 0;
            }
        }
    }

    public final static boolean isZero(byte[] buf) {
        if (buf != null) {
            for (int i = 0; i < buf.length; ++i) {
                if (buf[i] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * 字符串转换成十六进制字符串
     *
     * @param String str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param byte[] b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param String src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param String strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128) str.append("\\u" + strHex);
            else // 低位在前面补00  
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param String hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转  
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转  
            String s2 = s.substring(4);
            // 将16进制的string转为int  
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符  
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    public static boolean isBufEquals(byte[] b1, byte[] b2) {
        if (b1 == null || b2 == null || b1.length != b2.length) {
            return false;
        }

        for (int i = 0; i < b1.length; ++i) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSufaceFlashInWallpaperApp() {
        if (isRKSystem()) {
            return true;
        }
        return false;
    }

    public static boolean isGLCamera() {
        if (isRKSystem()) {
            return true;
        }
        return false;
    }

    public static boolean isPX5() {
        if (Build.DISPLAY.contains("px5") || Build.DISPLAY.contains("rk3368")) {
            return true;
        }
        return false;
    }

    public static boolean isPX6() {
        if (Build.DISPLAY.contains("px6") || Build.DISPLAY.contains("rk3399")) {
            return true;
        }
        return false;
    }

    public static boolean isPX30() {
        if (Build.DISPLAY.contains("px30") || Build.DISPLAY.contains("rk3326")) {
            return true;
        }
        return false;
    }

    public static boolean isRK356X() {
        if (Build.DISPLAY.contains("rk356")) {
            return true;
        }
        return false;
    }

    public static boolean isRK3566() {
        if (Build.DISPLAY.contains("rk3566")) {
            return true;
        }
        return false;
    }

    public static boolean isRK3568() {
        if (Build.DISPLAY.contains("rk3568")) {
            return true;
        }
        return false;
    }

    public static boolean isAndroidO() {
        if (Build.VERSION.SDK_INT <= 27) {        //Build.VERSION.SDK.contains(Build.P)
            return true;
        }
        return false;
    }

    public static boolean isAndroidP() {
        if (Build.VERSION.SDK_INT == 28) {        //Build.VERSION.SDK.contains(Build.P)
            return true;
        }
        return false;
    }

    public static boolean isAndroidQ() {
        if (Build.VERSION.SDK_INT == 29) {        //Build.VERSION.SDK.contains(Build.Q)
            return true;
        }
        return false;
    }

    public static boolean isAndroidR() {
        if (Build.VERSION.SDK_INT == 30) {        //Build.VERSION.SDK.contains(Build.R)
            return true;
        }
        return false;
    }

    public static boolean isAndroidS() {
        if (Build.VERSION.SDK_INT == 31) {        //Build.VERSION.SDK.contains(Build.S)
            return true;
        }
        return false;
    }

    public static boolean isAndroidLaterO() {
        if (Build.VERSION.SDK_INT >= 28) {        //Build.VERSION.SDK.contains(Build.R)
            return true;
        }
        return false;
    }

    public static boolean isAndroidLaterP() {
        if (Build.VERSION.SDK_INT >= 29) {        //Build.VERSION.SDK.contains(Build.R)
            return true;
        }
        return false;
    }

    public static boolean isAndroidLaterQ() {
        if (Build.VERSION.SDK_INT >= 30) {
            return true;
        }
        return false;
    }

    public static boolean isRKSystem() {
        if (isPX5() || isPX6() || isPX30() || isRK356X() || isAndroidLaterO()) {
            return true;
        }
        return false;
    }

    public static boolean isNexellSystem() {
        if (Build.DISPLAY.contains("s5p6818") || Build.DISPLAY.contains("aosp_avn_ref")) {
            return true;
        }
        return false;
    }

    public static boolean isNexellSystem60() {
        if (Build.DISPLAY.contains("s5p6818") && Build.VERSION.SDK_INT <= 23) {
            return true;
        }
        return false;
    }

    private final static String LOG_CONFIG = VENDOR_DIR + "log.txt";

    public static void logAccPowerOff(String log) {
        String s = getFileString(LOG_CONFIG);
        if (s != null) {
            log = s + "\r\n" + log;
        }
        setFileValue(LOG_CONFIG, log);
    }

    /*
     * 字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] array) {
        StringBuilder builder = new StringBuilder();

        for (byte b : array) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            builder.append(hex);
        }

        return builder.toString().toUpperCase();
    }

    public static void mcuReboot(int time) {
        byte bl = (byte) (time & 0xff);
        byte bh = (byte) ((time >> 8) & 0xff);
        byte[] b = {(byte) 0x55, (byte) 0xaa, (byte) 0x00, (byte) bl, (byte) bh};
        try {
            FileOutputStream out = new FileOutputStream("/sys/class/ak/source/factory");
            FileChannel fileChannel = out.getChannel();
            fileChannel.write(ByteBuffer.wrap(b));
            fileChannel.force(true);
            fileChannel.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
    }

    public static void mcuReboot() {
        byte[] b = {(byte) 0x55, (byte) 0xaa, (byte) 0x00};
        try {
            FileOutputStream out = new FileOutputStream("/sys/class/ak/source/factory");
            FileChannel fileChannel = out.getChannel();
            fileChannel.write(ByteBuffer.wrap(b));
            fileChannel.force(true);
            fileChannel.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
    }

    public static int getStatusBarHeight(Context c) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            return c.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            Log.e(tag, "getStatusBarHeight execpt:" + e);
            if (e != null) e.printStackTrace();
        }
        return -1;
    }
}
