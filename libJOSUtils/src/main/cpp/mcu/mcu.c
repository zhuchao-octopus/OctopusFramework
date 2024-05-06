#include "mcu.h"

#define LOG_TAG "Mcu"
//#undef LOG
//#include <utils/Log.h>

#include<android/log.h>

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

#include <errno.h>
#include <fcntl.h>
#include <termios.h>
#include <pthread.h>
#include <stdlib.h>
#include <poll.h>
#include <sys/time.h>
#include <string.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close
#include "ak_mcu.h"

#define LOGDEBUG_ON  0
typedef unsigned char BYTE;

#if LOGDEBUG_ON
#define LOGDEBUG_BUF_LEN 100
#define LOGDEBUG					LOGD
static int LogDebugBuf(BYTE *pucData,int nLen)
{
    int i;
    LOGD("len=%d ", nLen);
    for (i = 0; i < nLen; i++) {
        LOGD("%02x", *(pucData+i));
    }
    LOGD("\n");
    return 0;
}
#else
#define LOGDEBUG
#define LogDebugBuf
#endif

struct mcu_spec {
    JavaVM *jvm;
    jobject thiz;
    jclass clazz;

    int fd;

    pthread_t tid;
    //pthread_t tid_volume;
    pthread_t tid_reverse;
    unsigned int texit;

    unsigned char buff[255];
};

struct mcu_spec mcu = {
        .fd = -1,
};

static void *mcu_thread(void *arg) {
    struct mcu_spec *mcu = (struct mcu_spec *) arg;
    JavaVM *jvm = mcu->jvm;
    jmethodID mcuCallback;
    JNIEnv *env;
    int event;
    int param;
    int iNum = 0;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);
    mcuCallback = (*env)->GetMethodID(env, mcu->clazz, "dataCallback", "([BI)V");
    while (!mcu->texit) {
        event = 0xffffffff;
        memset(mcu->buff, 0, sizeof(mcu->buff));
        if ((ioctl(mcu->fd, AK_MCU_WAIT_EVENT, &event) == 0)) {
            if (event == AK_MCU_COMMON_DATA) {
                if (mcuCallback) {
                    ioctl(mcu->fd, AK_MCU_READ_COMMON_DATA, mcu->buff);
                    int len = (mcu->buff[0] & 0xff);
                    LogDebugBuf(mcu->buff, mcu->buff[0]);
                    jbyteArray bytes = (*env)->NewByteArray(env, len);
                    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte * )(mcu->buff + 1));
                    (*env)->CallVoidMethod(env, mcu->thiz, mcuCallback, bytes, len);
                    (*env)->DeleteLocalRef(env, bytes);
                }
            }
        }
    }

    (*jvm)->DetachCurrentThread(jvm);
    return NULL;
}


static void *reverse_thread(void *arg) {
    struct mcu_spec *mcu = (struct mcu_spec *) arg;
    JavaVM *jvm = mcu->jvm;
    JNIEnv *env;
    jmethodID kernelCallback;
    int ret;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);
    kernelCallback = (*env)->GetMethodID(env, mcu->clazz, "kernelCallback", "([BI)V");
    char buf[255];
    int len = 10;
    while (!mcu->texit) {
        unsigned int event = 0xffffffff;
        memset(buf, 0, sizeof(buf));
        ret = ioctl(mcu->fd, AK_REVERSE_WAIT_EVENT, buf);

        //LOGE("reverse_thread : %d\n", ret);
        if (ret == 0) {
            len = (buf[0] & 0xff) - 1;

            jbyteArray bytes = (*env)->NewByteArray(env, len);
            (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte * )(buf + 1));
            (*env)->CallVoidMethod(env, mcu->thiz, kernelCallback, bytes, len);
            (*env)->DeleteLocalRef(env, bytes);
        }
    }
    (*jvm)->DetachCurrentThread(jvm);
    return NULL;
}

/**/
JNIEXPORT jint JNICALL
Java_com_zhuchao_android_mcu_nativeSendCommand(JNIEnv *env, jobject thiz, jint cmd, jint param1, jbyteArray param2) {
    int ret = 0;
    //LOGD("Java_com_car_hardware_Mcu_nativeSendCommand cmd %d ", cmd);
    switch (cmd) {
        case com_car_hardware_Mcu_MCU_WRITE_DATA: {
            jbyte *jparam = (*env)->GetByteArrayElements(env, param2, 0);
            if (jparam != NULL && mcu.fd >= 0) {
                write(mcu.fd, jparam, param1);
            } else {
                LOGE("'/dev/mcu' is not opend");
            }
            break;
        }
        case com_car_hardware_Mcu_READ_KERNEL_PRO: {
            jbyte *jparam = (*env)->GetByteArrayElements(env, param2, 0);
            if (jparam != NULL && mcu.fd >= 0) {
                return ioctl(mcu.fd, AK_READ_KERNEL_APP_PRO_DATA, jparam);
            }
            break;
        }
        case com_car_hardware_Mcu_MCU_OPEN:
            if (mcu.fd < 0) {
                jclass clazz;

                mcu.fd = open("/dev/mcu", O_RDWR, 0666);
                if (mcu.fd < 0) {
                    LOGE("open '/dev/mcu' error(%d)", errno);
                    return -1;
                }
                clazz = (*env)->GetObjectClass(env, thiz);
                (*env)->GetJavaVM(env, &mcu.jvm);
                mcu.thiz = (*env)->NewGlobalRef(env, thiz);
                mcu.clazz = (*env)->NewGlobalRef(env, clazz);
                mcu.texit = 0;

                pthread_create(&mcu.tid, NULL, mcu_thread, &mcu);

                pthread_create(&mcu.tid_reverse, NULL, reverse_thread, &mcu);
            }
            break;

        case com_car_hardware_Mcu_MCU_CLOSE:
            if (mcu.fd >= 0) {
                mcu.texit = 1;
                pthread_join(mcu.tid, NULL);
                pthread_join(mcu.tid_reverse, NULL);

                (*env)->DeleteGlobalRef(env, mcu.clazz);
                (*env)->DeleteGlobalRef(env, mcu.thiz);
                close(mcu.fd);
                mcu.fd = -1;
            }
            break;
    }

    return ret;
}

