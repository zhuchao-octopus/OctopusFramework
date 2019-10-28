package com.zhuchao.android.netutil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeDateUtils {
    // String类型时间的几种格式
    public static final String FORMAT_TYPE_1 = "yyyyMMdd";
    public static final String FORMAT_TYPE_2 = "MM月dd日 hh:mm";
    public static final String FORMAT_TYPE_3 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_TYPE_4 = "yyyy年MM月dd日 HH时mm分ss秒";

    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前日期和时间
     *
     * @param formatType
     * @return
     */
    public static String getCurrentDateStr(String formatType) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(formatType);
        return sdf.format(date);
    }

    public static int getDateData(String DATE1,String formatType) {

        DateFormat df = new SimpleDateFormat(formatType);
        try {
            Date dt1 = df.parse(DATE1);
            String str = df.format(dt1);
            int data = Integer.parseInt(str);
            return  data;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    /**
     * 时间转换 Date ——> long
     *
     * @param date
     * @return
     */
    public static long date2Long(Date date) {
        return date.getTime();
    }

    /**
     * 时间转换 Date ——> String
     *
     * @param date
     * @param formatType
     * @return
     */
    public static String date2String(Date date, String formatType) {
        return new SimpleDateFormat(formatType).format(date);
    }

    /**
     * 时间转换 long ——> Date
     *
     * @param time
     * @param formatType
     * @return
     */
    public static Date long2Date(long time, String formatType) {
        Date oldDate = new Date(time);
        String dateStr = date2String(oldDate, formatType);
        Date date = string2Date(dateStr, formatType);
        return date;
    }

    /**
     * 时间转换 long ——> String
     *
     * @param time
     * @param formatType
     * @return
     */
    public static String long2String(long time, String formatType) {
        Date date = long2Date(time, formatType);
        String strTime = date2String(date, formatType);
        return strTime;
    }

    /**
     * 时间转换 String ——> Date
     *
     * @param strTime
     * @param formatType
     * @return
     */
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

    /**
     * 时间转换 String ——> long
     *
     * @param strTime
     * @param formatType
     * @return
     */
    public static long string2Long(String strTime, String formatType) {
        Date date = string2Date(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            long time = date2Long(date);
            return time;
        }
    }

    public static int compare_date(String DATE1, String DATE2,String formatType) {

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
            if (day>=1) {
                return day;
            }else {
                if (day==0) {
                    return 1;
                }else {
                    return 0;
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

}
