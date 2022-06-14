package com.zhuchao.android.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.callbackevent.TaskCallback;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.TTask;
import com.zhuchao.android.player.dlna.DLNAContainer;
import com.zhuchao.android.player.dlna.DLNAUtil;
import com.zhuchao.android.session.TTaskManager;
import com.zhuchao.android.video.OMedia;

import org.cybergarage.upnp.Device;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;

    private SurfaceView surfaceView;
    private List<Device> devices;
    private org.cybergarage.upnp.Device ktdevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        requestAuthorization();
        MMLog.setDebugOnOff(true);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button8 = findViewById(R.id.button8);
        surfaceView = findViewById(R.id.surfaceView1);
        TTaskManager tTaskManager = new TTaskManager(getApplicationContext());
        //TCourierEventBus tCourierEventBus = new TCourierEventBus();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = TPlatform.GetSystemProperty("wifi.direct.interface");
                MMLog.log(TAG,"t507GetSystemProperty = "+str);
                //str = TPlatform.GetAudioOutputPolicy();
                //MMLog.log(TAG,"getAudioOutputPolicy = "+str);
                //tCourierEventBus.post(new EventCourier("test",0,null));
                OMedia oMedia = new OMedia("/sdcard/Movies/local_video.mp4");
                oMedia.setMagicNumber(2);
                oMedia.with(MainActivity.this).playOn(surfaceView);

               //devices = DLNAUtil.getDLNADevices();
               MMLog.log(TAG,"ktdevice = " + ktdevice.getFriendlyName());
               //DLNAUtil.shareTo(ktdevice,"rtsp://192.168.110.103:8554/1");
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TPlatform.SetSystemProperty("navigation.layout.type","3");
               TTask tTask = tTaskManager.copyDirectory("/storage/udisk/shinektv_songlib/sata2",
                        "/media/shinektv_vod/sata2", 10).callbackHandler(new TaskCallback() {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        TTask ot = (TTask)(obj);
                        MMLog.log(TAG, "Index:"+ot.getProperties().getInt("filesCount") +","+ot.getProperties().getString("fromFile"));
                    }
                });
               tTask.start();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = TPlatform.GetSystemProperty("navigation.layout.type");
                MMLog.log(TAG,"t507GetSystemProperty = "+str);
                DLNAUtil.setDLNADeviceListener(new DLNAContainer.DeviceChangeListener() {
                    @Override
                    public void onDeviceChange(org.cybergarage.upnp.Device device) {
                      MMLog.log(TAG,device.getFriendlyName());
                      if(device.getFriendlyName().contains("客厅电视"))
                        ktdevice = device;
                    }
                });
                DLNAUtil.startDLNAService(getApplicationContext());
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }



    public void requestAuthorization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWritePermission = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                //preferencesUtility.setString("storage", "true");
            }
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                //preferencesUtility.setString("storage", "true");
            }
            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                        0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("Permissions --> " + "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.out.println("Permissions --> " + "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}