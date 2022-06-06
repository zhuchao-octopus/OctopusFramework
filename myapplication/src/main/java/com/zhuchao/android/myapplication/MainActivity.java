package com.zhuchao.android.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhuchao.android.TCourierEventBus;
import com.zhuchao.android.TPlatform;
import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.playerutil.dlna.DLNAContainer;
import com.zhuchao.android.playerutil.dlna.DLNAUtil;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private Button button1;
    private Button button2;
    private Button button3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        MMLog.setDebugOnOff(true);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        //TCourierEventBus tCourierEventBus = new TCourierEventBus();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = TPlatform.GetSystemProperty("wifi.direct.interface");
                MMLog.log(TAG,"t507GetSystemProperty = "+str);
                //str = TPlatform.GetAudioOutputPolicy();
                //MMLog.log(TAG,"getAudioOutputPolicy = "+str);
                //tCourierEventBus.post(new EventCourier("test",0,null));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TPlatform.SetSystemProperty("navigation.layout.type","3");
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
                    }
                });
                DLNAUtil.startDLNAService(getApplicationContext());
            }
        });

    }


}