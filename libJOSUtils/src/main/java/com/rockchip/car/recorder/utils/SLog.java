package com.rockchip.car.recorder.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SLog {
    public static final String PROPITY_DEBUG = "car.recorder.debug";
    public static final String PROPITY_DEBUG_LEVEL = "car.recorder.debug.level";
    private static boolean DEBUG = true;
    private static int DEBUG_LEVEL = 0;
    private static String TAG_PREFIX = "";
    private static String TAG_POSTFIX = "";
    private static String MSG_PREFIX = "";
    private static String MSG_POSTFIX = "";
    private static final boolean isSaveLog = false;
    public static final String ROOT = Environment.getExternalStorageDirectory().getPath() + "/finddreams/";
    private static final String PATH_LOG_INFO = ROOT + "info/";

    private SLog() {
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static void setDebugLevel(int debugLevel) {
        DEBUG_LEVEL = debugLevel;
    }

    public static int getDebugLevel() {
        return DEBUG_LEVEL;
    }

    public static String getTagPrefix() {
        return TAG_PREFIX;
    }

    public static void setTagPrefix(String tagPrefix) {
        TAG_PREFIX = tagPrefix;
    }

    public static String getTagPostfix() {
        return TAG_POSTFIX;
    }

    public static void setTagPostfix(String tagPostfix) {
        TAG_POSTFIX = tagPostfix;
    }

    public static String getMsgPrefix() {
        return MSG_PREFIX;
    }

    public static void setMsgPrefix(String msgPrefix) {
        MSG_PREFIX = msgPrefix;
    }

    public static String getMsgPostfix() {
        return MSG_POSTFIX;
    }

    public static void setMsgPostfix(String msgPostfix) {
        MSG_POSTFIX = msgPostfix;
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(Line:%d)"; // ռλ��
        String callerClazzName = caller.getClassName(); // ��ȡ������
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(),
                caller.getLineNumber()); // �滻
        tag = TextUtils.isEmpty(TAG_PREFIX) ? tag : TAG_PREFIX + tag;
        tag = TextUtils.isEmpty(TAG_POSTFIX) ? tag : tag + TAG_POSTFIX;
        return tag;
    }

    private static String generateMsg(String msg) {
        if (TAG_PREFIX != null) {
            msg = TAG_PREFIX + msg;
        }
        if (TAG_POSTFIX != null) {
            msg += TAG_POSTFIX;
        }
        return msg;
    }

    /**
     * custom logger
     */
    public static CustomLogger customLogger;

    public interface CustomLogger {
        void d(String tag, String content);

        void d(String tag, String content, Throwable tr);

        void e(String tag, String content);

        void e(String tag, String content, Throwable tr);

        void i(String tag, String content);

        void i(String tag, String content, Throwable tr);

        void v(String tag, String content);

        void v(String tag, String content, Throwable tr);

        void w(String tag, String content);

        void w(String tag, String content, Throwable tr);

        void w(String tag, Throwable tr);

        void wtf(String tag, String content);

        void wtf(String tag, String content, Throwable tr);

        void wtf(String tag, Throwable tr);
    }

    private static boolean checkNeedLog(int level) {
        if (DEBUG || SystemProperties.get(PROPITY_DEBUG, "true").equals("true")) {
            try {
                if (DEBUG_LEVEL <= level && Integer.parseInt(SystemProperties.get(PROPITY_DEBUG_LEVEL, "100")) <= level) {
                    return true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void d(String content) {
        if (!checkNeedLog(Log.DEBUG))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.d(tag, content);
        } else {
            Log.d(tag, content);
        }
    }

    public static void d(String content, Throwable tr) {
        if (!checkNeedLog(Log.DEBUG))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.d(tag, content, tr);
        } else {
            Log.d(tag, content, tr);
        }
    }

    public static void d(String tag, String content) {
        if (!checkNeedLog(Log.DEBUG))
            return;
        if (customLogger != null) {
            customLogger.d(tag, content);
        } else {
            Log.d(tag, content);
        }
    }

    public static void e(String content) {
        if (!checkNeedLog(Log.ERROR))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.e(tag, content);
        } else {
            Log.e(tag, content);
        }
        if (isSaveLog) {
            point(PATH_LOG_INFO, tag, content);
        }
    }

    public static void e(String content, Throwable tr) {
        if (!checkNeedLog(Log.ERROR))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.e(tag, content, tr);
        } else {
            Log.e(tag, content, tr);
        }
        if (isSaveLog) {
            point(PATH_LOG_INFO, tag, tr.getMessage());
        }
    }

    public static void e(String tag, String content) {
        if (!checkNeedLog(Log.ERROR))
            return;
        if (customLogger != null) {
            customLogger.e(tag, content);
        } else {
            Log.e(tag, content);
        }
    }

    public static void e(String tag, String content, Throwable tr) {
        if (!checkNeedLog(Log.ERROR))
            return;
        if (customLogger != null) {
            customLogger.e(tag, content, tr);
        } else {
            Log.e(tag, content, tr);
        }
    }



    public static void i(String content) {
        if (!checkNeedLog(Log.INFO))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.i(tag, content);
        } else {
            Log.i(tag, content);
        }

    }

    public static void i(String content, Throwable tr) {
        if (!checkNeedLog(Log.INFO))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.i(tag, content, tr);
        } else {
            Log.i(tag, content, tr);
        }

    }

    public static void i(String tag, String content) {
        if (!checkNeedLog(Log.INFO))
            return;
        if (customLogger != null) {
            customLogger.i(tag, content);
        } else {
            Log.i(tag, content);
        }
    }

    public static void v(String content) {
        if (!checkNeedLog(Log.VERBOSE))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.v(tag, content);
        } else {
            Log.v(tag, content);
        }
    }

    public static void v(String content, Throwable tr) {
        if (!checkNeedLog(Log.VERBOSE))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.v(tag, content, tr);
        } else {
            Log.v(tag, content, tr);
        }
    }

    public static void v(String tag, String content) {
        if (!checkNeedLog(Log.VERBOSE))
            return;
        if (customLogger != null) {
            customLogger.v(tag, content);
        } else {
            Log.v(tag, content);
        }
    }

    public static void w(String content) {
        if (!checkNeedLog(Log.WARN))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.w(tag, content);
        } else {
            Log.w(tag, content);
        }
    }

    public static void w(String content, Throwable tr) {
        if (!checkNeedLog(Log.WARN))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.w(tag, content, tr);
        } else {
            Log.w(tag, content, tr);
        }
    }

    public static void w(Throwable tr) {
        if (!checkNeedLog(Log.WARN))
            return;
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.w(tag, tr);
        } else {
            Log.w(tag, tr);
        }
    }

    public static void w(String tag, String content) {
        if (!checkNeedLog(Log.WARN))
            return;
        if (customLogger != null) {
            customLogger.w(tag, content);
        } else {
            Log.w(tag, content);
        }
    }

    public static void wtf(String content) {
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.wtf(tag, content);
        } else {
            Log.wtf(tag, content);
        }
    }

    public static void wtf(String content, Throwable tr) {
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        content = generateMsg(content);

        if (customLogger != null) {
            customLogger.wtf(tag, content, tr);
        } else {
            Log.wtf(tag, content, tr);
        }
    }

    public static void wtf(Throwable tr) {
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.wtf(tag, tr);
        } else {
            Log.wtf(tag, tr);
        }
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static void point(String path, String tag, String msg) {
        if (isStorageAvailable()) {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("",
                    Locale.SIMPLIFIED_CHINESE);
            dateFormat.applyPattern("yyyy");
            path = path + dateFormat.format(date) + "/";
            dateFormat.applyPattern("MM");
            path += dateFormat.format(date) + "/";
            dateFormat.applyPattern("dd");
            path += dateFormat.format(date) + ".log";
            dateFormat.applyPattern("[yyyy-MM-dd HH:mm:ss]");
            String time = dateFormat.format(date);
            File file = new File(path);
            if (!file.exists()) {
                createDipPath(path);
            }
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file, true)));
                out.write(time + tag + msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * �����ļ�·�� �ݹ鴴���ļ�
     * 
     * @param file
     */
    public static void createDipPath(String file) {
        String parentFile = file.substring(0, file.lastIndexOf("/"));
        File file1 = new File(file);
        File parent = new File(parentFile);
        if (!file1.exists()) {
            parent.mkdirs();
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // /**
    // * A little trick to reuse a formatter in the same thread
    // */
    // private static class ReusableFormatter {
    //
    // private Formatter formatter;
    // private StringBuilder builder;
    //
    // public ReusableFormatter() {
    // builder = new StringBuilder();
    // formatter = new Formatter(builder);
    // }
    //
    // public String format(String msg, Object... args) {
    // formatter.format(msg, args);
    // String s = builder.toString();
    // builder.setLength(0);
    // return s;
    // }
    // }
    //
    // private static final ThreadLocal<ReusableFormatter>
    // thread_local_formatter = new ThreadLocal<ReusableFormatter>() {
    // protected ReusableFormatter initialValue() {
    // return new ReusableFormatter();
    // }
    // };
    //
    // public static String format(String msg, Object... args) {
    // ReusableFormatter formatter = thread_local_formatter.get();
    // return formatter.format(msg, args);
    // }

    public static boolean isStorageAvailable() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                || Environment.getExternalStorageDirectory().exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return a String describing the calling method and location at a
     * particular stack depth.
     * 
     * @param callStack
     *            the Thread stack
     * @param depth
     *            the depth of stack to return information for.
     * @return the String describing the caller at that depth.
     */
    private static String getCaller(StackTraceElement callStack[], int depth) {
        // callStack[4] is the caller of the method that called getCallers()
        if (4 + depth >= callStack.length) {
            return "<bottom of call stack>";
        }
        StackTraceElement caller = callStack[4 + depth];
        return caller.getClassName() + "." + caller.getMethodName() + ":"
                + caller.getLineNumber();
    }

    /**
     * Return a string consisting of methods and locations at multiple call
     * stack levels.
     * 
     * @param depth
     *            the number of levels to return, starting with the immediate
     *            caller.
     * @return a string describing the call stack. {@hide}
     */
    public static String getCallers(final int depth) {
        final StackTraceElement[] callStack = Thread.currentThread()
                .getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(getCaller(callStack, i)).append(" ");
        }
        return sb.toString();
    }

    /**
     * Return a string consisting of methods and locations at multiple call
     * stack levels.
     * 
     * @param depth
     *            the number of levels to return, starting with the immediate
     *            caller.
     * @return a string describing the call stack. {@hide}
     */
    public static String getCallers(final int start, int depth) {
        final StackTraceElement[] callStack = Thread.currentThread()
                .getStackTrace();
        StringBuffer sb = new StringBuffer();
        depth += start;
        for (int i = start; i < depth; i++) {
            sb.append(getCaller(callStack, i)).append(" ");
        }
        return sb.toString();
    }

    /**
     * Like {@link #getCallers(int)}, but each location is append to the string
     * as a new line with <var>linePrefix</var> in front of it.
     * 
     * @param depth
     *            the number of levels to return, starting with the immediate
     *            caller.
     * @param linePrefix
     *            prefix to put in front of each location.
     * @return a string describing the call stack. {@hide}
     */
    public static String getCallers(final int depth, String linePrefix) {
        final StackTraceElement[] callStack = Thread.currentThread()
                .getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(linePrefix).append(getCaller(callStack, i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * 
     * @return a String describing the immediate caller of the calling method.
     */
    public static String getCaller() {
        return getCaller(Thread.currentThread().getStackTrace(), 0);
    }
}
