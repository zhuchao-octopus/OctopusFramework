package com.zhuchao.android.c_cnn;

public class CCNN {
    static {
        System.loadLibrary("c-cnn");
    }

    public static native void CreateNeuralNetInit_C_CNN_9(String NetName);
    public native void StartNeuralNetInit_C_CNN_9(String NetName);

    public static void  CreateNeuralNetInit_C_CNN_9()
    {
        CreateNeuralNetInit_C_CNN_9("");
    }
}
