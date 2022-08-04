package com.zhuchao.android.myapplication;

import android.app.Activity;
import android.os.Bundle;

import com.zhuchao.android.opencamera.Preview.CameraPreview;
import com.zhuchao.android.opencamera.TCameraInterface;

public class MainActivity3 extends Activity {
    private final String TAG = "MainActivity3";
    private CameraPreview preview1;
    private CameraPreview preview2;
    private TCameraInterface cameraInterface;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cameraInterface = new TCameraInterface(MainActivity3.this);
        //preview1 = new CameraPreview(cameraInterface, (this.findViewById(R.id.preview1)));


        //preview1.setCamera(0);
        //preview1.retryOpenCamera();
    }




}