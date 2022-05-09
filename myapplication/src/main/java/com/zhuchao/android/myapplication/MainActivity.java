package com.zhuchao.android.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.libfileutils.MMLog;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        button = findViewById(R.id.button1);
        MMLog.setDebugOnOff("true");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = TPlatform.t507GetSystemProperty("wifi.direct.interface");
                MMLog.log(TAG,"t507GetSystemProperty = "+str);
                str = TPlatform.getAudioOutputPolicy();
                MMLog.log(TAG,"t507GetSystemProperty = "+str);
            }
        });


    }


}