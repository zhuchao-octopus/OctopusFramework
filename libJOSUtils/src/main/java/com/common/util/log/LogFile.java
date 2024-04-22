package com.common.util.log;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFile {

    public static void init(String file) {
        mLogFile = file;
        File f = new File(file);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                Log.d("", e.toString());
            }
        }
        mIsLog = true;
    }

    private static String mLogFile = "/sdcard/car_logfile.txt";
    private static boolean mIsLog = false;

    public static void setFileValueEx(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void d(String tag, String msg) {
        if (mIsLog) {
            setFileValueEx(mLogFile, tag + "   " + msg + "\n");
        }
        Log.d(tag, msg);
    }
}
