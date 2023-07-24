//
// Created by lenovo on 2023/7/22.
//
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include "android/log.h"

#include <jni.h>

#include "ann-cnn.h"
#include "ann-dataset.h"
#include "ann-configuration.h"

static const char *TAG = "ann-cnn-jni";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

TPNeuralNet PNeuralNetCNN_9;

JNIEXPORT void JNICALL
Java_com_zhuchao_android_c_1cnn_CCNN_CreateNeuralNetInit_1C_1CNN_19(JNIEnv *env, jobject thiz,
                                                                    jstring net_name) {
    /// TODO: implement NeuralNetInit_C_CNN_9()
    //PNeuralNetCNN_9 = NeuralNetInit_C_CNN_9("C_CNN_9_Cifar10");
    PNeuralNetCNN_9 = NeuralNetCreateAndInit_Cifar10();
    ///PNeuralNetCNN_9->trainning.data_type = Cifar10; //学习Cifar10数据集

    ///TestForNDK();
    LOGD("Java_com_zhuchao_android_c_1cnn_CCNN_CreateNeuralNetInit_1C_1CNN_19");
}

JNIEXPORT void JNICALL
Java_com_zhuchao_android_c_1cnn_CCNN_StartNeuralNetInit_1C_1CNN_19(JNIEnv *env, jobject thiz,
                                                                   jstring net_name) {
    // TODO: implement StartNeuralNetInit_C_CNN_9()

    PNeuralNetCNN_9->loadWeights(PNeuralNetCNN_9);
    PNeuralNetCNN_9->trainning.trainingSaving = true;
    PNeuralNetCNN_9->trainning.one_by_one = false;
    PNeuralNetCNN_9->trainning.batch_by_batch = false;
    PNeuralNetCNN_9->trainning.trainningGoing = true;
    //PNeuralNetCNN_9->loadWeights(PNeuralNetCNN_9);
    PNeuralNetCNN_9->trainning.randomFlip = false;
    NeuralNetStartTrainning(PNeuralNetCNN_9);
}

