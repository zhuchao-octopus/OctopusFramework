#include "com_my_hardware_mud_MubUpdate.h"

#define LOG_TAG "MubUpdate"
//#undef LOG
//#include <utils/Log.h>

#include<android/log.h>

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)


#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <termios.h>
#include <pthread.h>
#include <stdlib.h>
#include <poll.h>

#include "mub_update.h"

struct MubUpdate {
    unsigned int mask;
    JavaVM *jvm;
    jobject thiz;
    jclass clazz;
    int fd;
    pthread_t tid;
    unsigned int texit;
};

static void *update_thread(void *arg) {
    struct MubUpdate *mud = (struct MubUpdate *) arg;
    JavaVM *jvm = mud->jvm;
    JNIEnv *env;
    jmethodID mudCallback;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);
    mudCallback = (*env)->GetMethodID(env, mud->clazz, "mudCallback", "(I)V");
    while (!mud->texit) {
        unsigned int event = mud->mask;
        if ((ioctl(mud->fd, MUB_UPDATE_WAIT_EVENT, &event) == 0) && (event & 0xff)) {
            (*env)->CallVoidMethod(env, mud->thiz, mudCallback, event);
        }
    }
    (*jvm)->DetachCurrentThread(jvm);
    return NULL;
}

static struct MubUpdate mud = {
        .fd = -1,
};

JNIEXPORT jint JNICALL Java_com_my_hardware_mud_MubUpdate_nativeSendMUDCommand2(JNIEnv *env, jobject thiz, jstring value) {
    if (mud.fd < 0) {
        jclass clazz = (*env)->GetObjectClass(env, thiz);
        const char *str = (*env)->GetStringUTFChars(env, value, 0);

        LOGE("update file=%s", str);

        if (str == NULL) {
            return -1;
        }

        FILE *fp = fopen(str, "rb");
        if (!fp) {
            return -1;
        }

        fseek(fp, 0, SEEK_END);
        unsigned int length = ftell(fp);
        LOGI("update.bin %d", length);
        unsigned char *file = malloc(length + 4);
        *((unsigned int *) file) = length;
        fseek(fp, 0, SEEK_SET);
        fread(file + 4, length, 1, fp);
        fclose(fp);
        unsigned short sum = 0;
        unsigned int i;
        for (i = 4; i < length + 2; i++) {
            sum += file[i];
        }
        unsigned short checksum = file[i] | (file[i + 1] << 8);
        LOGI("noooo 2222 sum=%x checksum=%x", sum, checksum);
        //	if(sum != checksum){
        //		free(file);
        //		return -2;
        //	}
        mud.fd = open("/dev/update", O_RDWR, 0666);
        if (mud.fd < 0) {
            LOGE("open '/dev/update' error(%d)", errno);
            return -3;
        }
        ioctl(mud.fd, MUB_UPDATE_START, file);
        //	jclass clazz = (*env)->GetObjectClass(env, thiz);
        jfieldID mMUDMask = (*env)->GetFieldID(env, clazz, "mMUDMask", "I");
        mud.mask = (*env)->GetIntField(env, thiz, mMUDMask);
        (*env)->GetJavaVM(env, &mud.jvm);
        mud.thiz = (*env)->NewGlobalRef(env, thiz);
        mud.clazz = (*env)->NewGlobalRef(env, clazz);
        mud.texit = 0;
        pthread_create(&mud.tid, NULL, update_thread, &mud);
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_my_hardware_mud_MubUpdate_nativeSendMUDCommand(JNIEnv *env, jobject thiz, jint value) {
    switch (value) {
        case com_my_hardware_mud_MubUpdate_MUD_START:
            if (mud.fd < 0) {
                FILE *fp = fopen("/storage/sdcard1/update.bin", "rb");
                if (!fp) {
                    fp = fopen("/storage/sdcard2/update.bin", "rb");
                    if (!fp) {
                        fp = fopen("/storage/usbdisk1/update.bin", "rb");
                        if (!fp) {
                            fp = fopen("/storage/usbdisk2/update.bin", "rb");
                            if (!fp) {
                                fp = fopen("/storage/usbdisk3/update.bin", "rb");
                                if (!fp) {
                                    fp = fopen("/storage/usbdisk4/update.bin", "rb");
                                    if (!fp) {
                                        fp = fopen("/sdcard/update.bin", "rb");
                                        if (!fp) {
                                            LOGE("open '/xxxx/update.bin' error(%d)", errno);
                                            return -1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                fseek(fp, 0, SEEK_END);
                unsigned int length = ftell(fp);
                LOGI("update.bin %d", length);
                unsigned char *file = malloc(length + 4);
                *((unsigned int *) file) = length;
                fseek(fp, 0, SEEK_SET);
                fread(file + 4, length, 1, fp);
                fclose(fp);
                unsigned short sum = 0;
                unsigned int i;
                for (i = 4; i < length + 2; i++) {
                    sum += file[i];
                }
                unsigned short checksum = file[i] | (file[i + 1] << 8);
                LOGI("noooooooooooo   sum=%x checksum=%x", sum, checksum);
                //if(sum != checksum){
                //	free(file);
                //	return -2;
                //}
                mud.fd = open("/dev/update", O_RDWR, 0666);
                if (mud.fd < 0) {
                    LOGE("open '/dev/update' error(%d)", errno);
                    return -3;
                }
                ioctl(mud.fd, MUB_UPDATE_START, file);
                jclass clazz = (*env)->GetObjectClass(env, thiz);
                jfieldID mMUDMask = (*env)->GetFieldID(env, clazz, "mMUDMask", "I");
                mud.mask = (*env)->GetIntField(env, thiz, mMUDMask);
                (*env)->GetJavaVM(env, &mud.jvm);
                mud.thiz = (*env)->NewGlobalRef(env, thiz);
                mud.clazz = (*env)->NewGlobalRef(env, clazz);
                mud.texit = 0;
                pthread_create(&mud.tid, NULL, update_thread, &mud);
            }
            break;
        case com_my_hardware_mud_MubUpdate_MUD_RESET:
            if (mud.fd >= 0) {
                mud.texit = 1;
                pthread_join(mud.tid, NULL);
                (*env)->DeleteGlobalRef(env, mud.clazz);
                (*env)->DeleteGlobalRef(env, mud.thiz);
                sync();
                ioctl(mud.fd, MUB_UPDATE_RESET, NULL);
                close(mud.fd);
                mud.fd = -1;
            }
            break;
        default:
            return -1;
    }
    return 0;
}
