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
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.callbackevent.TaskCallback;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.TTask;
import com.zhuchao.android.player.dlna.DLNAContainer;
import com.zhuchao.android.player.dlna.DLNAUtil;
import com.zhuchao.android.session.TTaskManager;

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
    private TextView textView1, textView2;
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
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        surfaceView = findViewById(R.id.surfaceView1);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);

        TTaskManager tTaskManager = new TTaskManager(getApplicationContext());
        //TCourierEventBus tCourierEventBus = new TCourierEventBus();
        button1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //String str = TPlatform.GetSystemProperty("wifi.direct.interface");
                //MMLog.log(TAG,"t507GetSystemProperty = "+str);
                //str = TPlatform.GetAudioOutputPolicy();
                //MMLog.log(TAG,"getAudioOutputPolicy = "+str);
                //tCourierEventBus.post(new EventCourier("test",0,null));
                //OMedia oMedia = new OMedia("/storage/6052-0866/Pictures/Screenshot_20220616_093130.png");
                //oMedia.setMagicNumber(2);
                //oMedia.with(MainActivity.this).playOn(surfaceView);

                //devices = DLNAUtil.getDLNADevices();
                //MMLog.log(TAG,"ktdevice = " + ktdevice.getFriendlyName());
                //DLNAUtil.shareTo(ktdevice,"rtsp://192.168.110.103:8554/1");
                /*long tick = System.currentTimeMillis();
                FileUtils.bufferCopyFile("/storage/4AAD-3A12/shinektv_songlib/sata2/753045.mpg","/mnt/WanC/sata2/bufferCopyFile.mpg");
                MMLog.log(TAG,"bufferCopyFile take up time: "+(System.currentTimeMillis() - tick));

                tick = System.currentTimeMillis();
                FileUtils.streamCopy("/storage/4AAD-3A12/shinektv_songlib/sata2/753045.mpg","/mnt/WanC/sata2/streamCopy.mpg");
                MMLog.log(TAG,"streamCopy take up time: "+(System.currentTimeMillis() - tick));

                tick = System.currentTimeMillis();
                FileUtils.channelTransferTo("/storage/4AAD-3A12/shinektv_songlib/sata2/753045.mpg","/mnt/WanC/sata2/channelTransferTo.mpg");
                MMLog.log(TAG,"channelTransferTo take up time: "+(System.currentTimeMillis() - tick));

                tick = System.currentTimeMillis();
                FileUtils.pathCopy("/storage/4AAD-3A12/shinektv_songlib/sata2/753045.mpg","/mnt/WanC/sata2/pathCopy.mpg");
                MMLog.log(TAG,"pathCopy take up time: "+(System.currentTimeMillis() - tick));*/
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TPlatform.SetSystemProperty("navigation.layout.type","3");
               /*TTask tTask1 = tTaskManager.copyDirectory("/storage/4AAD-3A12/shinektv_songlib/sata",
                        "/mnt/WanC/sata", 10).callbackHandler(new TaskCallback()
               {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        TTask ot = (TTask)(obj);
                        //MMLog.log(TAG, "Index:"+ot.getProperties().getInt("filesCount") +","+ot.getProperties().getString("fromFile"));
                        String str = " totalCount: "+ot.getProperties().getInt("totalCount");
                         str = str + " totalSize: "+ot.getProperties().getLong("totalSize");
                         str = str + " copiedCount: "+ot.getProperties().getInt("copiedCount");
                         //str = str + " copySpeed: "+ot.getProperties().getFloat("copySpeed");
                         str = str + " takeUpTime: "+ot.getProperties().getLong("takeUpTime");
                        textView1.setText(str);
                        if(ot.getProperties().getInt("status")== DataID.TASK_STATUS_FINISHED)
                            MMLog.log(TAG,"copy all finished.");
                    }
                });
                //tTask1.getProperties().putInt("copyMethod",2);
                tTask1.start();*/


                TTask tTask2 = tTaskManager.copyDirectory("/storage/4AAD-3A12/shinektv_songlib/sata2",
                        "/mnt/WanC/sata2").callbackHandler(new TaskCallback() {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        TTask ot = (TTask) (obj);
                        //MMLog.log(TAG, "Index:"+ot.getProperties().getInt("filesCount") +","+ot.getProperties().getString("fromFile"));
                        String str = " totalCount: " + ot.getProperties().getInt("totalCount");
                        str = str + " totalSize: " + ot.getProperties().getLong("totalSize");
                        str = str + " copiedCount: " + ot.getProperties().getInt("copiedCount");
                        //str = str + " copySpeed: "+ot.getProperties().getFloat("copySpeed");
                        str = str + " takeUpTime: " + ot.getProperties().getLong("takeUpTime");
                        textView2.setText(str);
                        if (ot.getProperties().getInt("status") == DataID.TASK_STATUS_FINISHED) {
                            MMLog.log(TAG, "copy all finished.");
                            tTaskManager.deleteTask(ot);
                        }
                    }
                });
                tTask2.getProperties().putInt("copyMethod", 1);
                tTask2.start();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = TPlatform.GetSystemProperty("navigation.layout.type");
                MMLog.log(TAG, "t507GetSystemProperty = " + str);
                DLNAUtil.setDLNADeviceListener(new DLNAContainer.DeviceChangeListener() {
                    @Override
                    public void onDeviceChange(org.cybergarage.upnp.Device device) {
                        MMLog.log(TAG, device.getFriendlyName());
                        if (device.getFriendlyName().contains("客厅电视"))
                            ktdevice = device;
                    }
                });
                DLNAUtil.startDLNAService(getApplicationContext());
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
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