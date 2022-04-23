package com.zhuchao.android.libfileutils;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.zhuchao.android.utils.MMLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {
    private static final String TAG = "DateTimeUtils";
    //public static final String FORMAT_TYPE_1 = "yyyyMMdd";
    //public static final String FORMAT_TYPE_2 = "MM月dd日 hh:mm";
    //public static final String FORMAT_TYPE_3 = "yyyy-MM-dd HH:mm:ss";
    //public static final String FORMAT_TYPE_4 = "yyyy年MM月dd日 HH时mm分ss秒";

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getCurrentDateStr(String formatType) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(formatType);
        return sdf.format(date);
    }

    public static int getDateData(String DATE1, String formatType) {

        DateFormat df = new SimpleDateFormat(formatType);
        try {
            Date dt1 = df.parse(DATE1);
            String str = df.format(dt1);
            int data = Integer.parseInt(str);
            return data;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public static long date2Long(Date date) {
        return date.getTime();
    }

    public static String date2String(Date date, String formatType) {
        return new SimpleDateFormat(formatType).format(date);
    }

    public static Date long2Date(long time, String formatType) {
        Date oldDate = new Date(time);
        String dateStr = date2String(oldDate, formatType);
        Date date = string2Date(dateStr, formatType);
        return date;
    }

    public static String long2String(long time, String formatType) {
        Date date = long2Date(time, formatType);
        String strTime = date2String(date, formatType);
        return strTime;
    }

    public static Date string2Date(String strTime, String formatType) {
        SimpleDateFormat format = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = format.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long string2Long(String strTime, String formatType) {
        Date date = string2Date(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            long time = date2Long(date);
            return time;
        }
    }

    public static int compare_date(String DATE1, String DATE2, String formatType) {
        DateFormat df = new SimpleDateFormat(formatType);
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                //System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                //System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public long dateDiff(String startTime, String endTime, String format) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(format);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        long diff;
        long day = 0;
        try {
            // 获得两个时间的毫秒时间差异
            diff = sd.parse(endTime).getTime()
                    - sd.parse(startTime).getTime();
            day = diff / nd;// 计算差多少天
            long hour = diff % nd / nh;// 计算差多少小时
            long min = diff % nd % nh / nm;// 计算差多少分钟
            long sec = diff % nd % nh % nm / ns;// 计算差多少秒
            // 输出结果
            System.out.println("时间相差：" + day + "天" + hour + "小时" + min
                    + "分钟" + sec + "秒。");
            if (day >= 1) {
                return day;
            } else {
                if (day == 0) {
                    return 1;
                } else {
                    return 0;
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    public static void setSysDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(ALARM_SERVICE)).setTime(when);
        }
    }

    public static void setSysTime(Context context, int hour, int minute, int ss) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, ss);
        c.set(Calendar.MILLISECOND, 0);

        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(ALARM_SERVICE)).setTime(when);
        }
    }


    public static String getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String getCurrentTimeZone() {
        Calendar now = Calendar.getInstance();
        return now.getTimeZone().toString();
    }

    public static void setTimeZone(Context context, String timeZone) {
        final Calendar now = Calendar.getInstance();

        MMLog.log(TAG, "getCurrentTimeZone=" + getCurrentTimeZone());
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        now.setTimeZone(tz);
        TimeZone.setDefault(tz);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        //alarm.setTimeZone(id);//默认时区的id
        alarm.setTimeZone(timeZone);

        MMLog.log(TAG, "setTimeZone=" + tz);
        MMLog.log(TAG, "getCurrentTimeZone=" + getCurrentTimeZone());
    }

    public static void setAutoTimeZone(Context context, int checked) {
        android.provider.Settings.Global.putInt(context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME_ZONE, checked);

        MMLog.log(TAG, "getCurrentTimeZone=" + getCurrentTimeZone());
    }

    public static void setAutoDateTime(Context context, int checked) {
        android.provider.Settings.Global.putInt(context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME, checked);
    }

    public static void set24Hour(Context mContext) {
        ContentResolver cv = mContext.getContentResolver();
        android.provider.Settings.System.putString(cv, Settings.System.TIME_12_24, "24");
    }

    public static boolean is24Hour(Context mContext) {
        ContentResolver cv = mContext.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        return strTimeFormat != null && strTimeFormat.equals("24");
    }

    public static String getTimeByMillisecond(long millisecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));// time为转换格式后的字符串
        String time = dateFormat.format(new Date(millisecond));
        return time;
    }

}
