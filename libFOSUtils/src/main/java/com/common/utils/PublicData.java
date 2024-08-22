package com.common.utils;

import static com.common.utils.MachineConfig.VENDOR_DIR;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class PublicData {
    private final static String TAG = "PublicData";
    public final static String KEY_TOP_ACTIVITY = "key_top_activity";

    private final static String PROJECT_CONFIG = ".public_data";
    private final static String SYSTEM_CONFIG = VENDOR_DIR + PROJECT_CONFIG;

    private static final Properties mProperties = new Properties();

    // only CarService use these functions below. other use Settings
    private static void getConfigProperties() {
        InputStream inputStream = null;
        File configFile = new File(SYSTEM_CONFIG);
        if (configFile.exists()) {
            try {
                inputStream = new FileInputStream(configFile);
                mProperties.load(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initConfigProperties() {
        //		if (mPoperties.size() == 0) {
        getConfigProperties();
        //		}
    }

    public static void updateConfigProperties() {
        Log.d(TAG, "updateConfigProperties()");
        File file = new File(SYSTEM_CONFIG);
        try {
            if (!file.exists()) {
                file.createNewFile();
                //FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);
            }
            //FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

            FileOutputStream out = new FileOutputStream(file);
            mProperties.store(out, "");

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getProperty(String name) {
        initConfigProperties();
        return mProperties.getProperty(name);
    }

    public static Object setProperty(String name, String value) {
        Object ret;
        initConfigProperties();
        ret = mProperties.setProperty(name, value);
        updateConfigProperties();
        return ret;
    }

}
