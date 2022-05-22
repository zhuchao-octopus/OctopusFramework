package com.zhuchao.android.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhuchao.android.TCourierEventBus;
import com.zhuchao.android.TPlatform;
import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.libfileutils.MMLog;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        MMLog.setDebugOnOff(true);
        button = findViewById(R.id.button1);
        TCourierEventBus tCourierEventBus = new TCourierEventBus();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String str = TPlatform.GetSystemProperty("wifi.direct.interface");
                //MMLog.log(TAG,"t507GetSystemProperty = "+str);
                //str = TPlatform.GetAudioOutputPolicy();
                //MMLog.log(TAG,"getAudioOutputPolicy = "+str);
                tCourierEventBus.post(new EventCourier("test",0,null));
            }
        });


    }


}