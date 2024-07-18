#include <errno.h>
#include <fcntl.h>
#include <termios.h>
#include <pthread.h>
#include <stdlib.h>
#include <poll.h>

#define LOG_TAG "BT_HW"
//#undef LOG
//#include <utils/Log.h>

#include<android/log.h>
#include <string.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

#include "com_my_hardware_setting_HWSetting.h"

#define sudo2_PASSWORD    "ak47ak47"

static int GetAuxIn() {
    int status;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/auxin", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/auxin' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}


static int SetAuxIn(int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/auxin", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/auxin' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}


static int GetLed() {
    int status;
    char buf[16] = {0};

    int fd = open("/sys/class/ak/keypad/mcu", O_RDWR | O_NONBLOCK, 0666);
    if (fd >= 0) {
        unsigned char tx[] = {0x0a, 0x0b, 0x1, 0x0};
        write(fd, tx, sizeof(tx));
        close(fd);
    }
    fd = -1;

    fd = open("/sys/class/ak/source/led_color", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/led_color' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}


static int SetLed(int status) {
    int fd = open("/sys/class/ak/keypad/mcu", O_RDWR | O_NONBLOCK, 0666);
    if (fd >= 0) {
        unsigned char tx[] = {0x0a, 0x0b, 0x0, 0x0};
        tx[3] = status;
        write(fd, tx, sizeof(tx));
        close(fd);
    }
    return 0;

}

static int GetDVD() {
    int status;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/dvd", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/dvd' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}


static int SetDVD(int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/dvd", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/dvd' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}

static int GetReakSW() {
    int status;
    char buf[4] = {0};
    int fd = open("/sys/class/ak/source/reaksw", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/reaksw' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}


static int SetReakSW(int status) {
    char buf[4] = {0};
    int fd = open("/sys/class/ak/source/reaksw", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/reaksw' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}

static int GetIllumin() {
    int status;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/illumintr", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/illumintr' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}


static int SetIllumin(int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/illumintr", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/illumintr' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}


static int GetRadio(int index) {
    int status = 0;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/radio", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/radio' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);

    int param[4] = {0};
    sscanf(buf, "%d,%d,%d,%d", &param[0], &param[1], &param[2], &param[3]);
    if (index > 0 && index <= 4) {
        status = param[index - 1];
    }

    //LOGD("GetRadio=%s param=%d,%d,%d,%d", buf,param[0],param[1],param[2],param[3]);
    return status;
}


static int SetRadio(int index, int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/radio", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/radio' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d,%d", index, status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}

static int GetFan() {
    int status;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/fan", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/fan' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}

static int SetFan(int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/fan", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/fan' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    return 0;
}

static int ResetMcuSecret() {
    char buf[128] = {0};
    char *password = sudo2_PASSWORD;
    sprintf(buf, "sudo %s chmod 222 /sys/class/ak/source/security", password);
    system(buf);

    int fd = open("/sys/class/ak/source/security", O_WRONLY | O_NONBLOCK, 0220);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/security' error(%d)", errno);
        return -1;
    }
    buf[0] = 0x55;
    buf[1] = 0xaa;
    buf[2] = 0x00;
    write(fd, buf, 3);
    close(fd);

    sprintf(buf, "sudo %s chmod 220 /sys/class/ak/source/security", password);
    system(buf);
    return 0;
}

static int GetRadioRegion() {
    int status;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/radioregion", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/radioregion' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);
    status = atoi(buf);
    return status;
}

static int SetRadioRegion(int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/radioregion", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/radioregion' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d", status);
    write(fd, buf, strlen(buf));
    close(fd);
    LOGD("SetRadioRegion=%d", status);
    return 0;
}

static int GetCarSupport(int index) {
    int status = 0;
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/can", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/can' error(%d)", errno);
        return -1;
    }
    read(fd, buf, sizeof(buf));
    close(fd);

    status = atoi(buf);
    if (index >= 0 && index < 8) {
        status = (status >> index) & 0x01;
    } else {
        LOGE("GetCarSupport error,index = %d", index);
    }

    //LOGD("GetCarSupport=%s,0x%x status=0x%x,index = %d", buf,atoi(buf),status,index);
    return status;
}

static int SetCarSupport(int index, int status) {
    char buf[16] = {0};
    int fd = open("/sys/class/ak/source/can", O_RDWR | O_NONBLOCK, 0666);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/can' error(%d)", errno);
        return -1;
    }
    sprintf(buf, "%d,%d", index, status);
    write(fd, buf, strlen(buf));
    close(fd);
    //LOGD("SetCarSupport:%s",buf);
    return 0;
}

static int ResetMcuFactory() {
    char buf[4] = {0};
    int fd = open("/sys/class/ak/source/factory", O_WRONLY | O_NONBLOCK, 0220);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/factory' error(%d)", errno);
        return -1;
    }
    buf[0] = 0x55;
    buf[1] = 0xaa;
    buf[2] = 0x00;
    write(fd, buf, 3);
    close(fd);
    return 0;
}

static int ResetSystemFactory() {
    char buf[128] = {0};
    char *password = sudo2_PASSWORD;
    sprintf(buf, "sudo %s busybox rm -r /data/*", password);
    system(buf);

    sync();
    ResetMcuFactory();

    sprintf(buf, "sudo %s chmod 222 /sys/class/ak/source/reset", password);
    system(buf);

    int fd = open("/sys/class/ak/source/reset", O_WRONLY | O_NONBLOCK, 0220);
    if (fd < 0) {
        LOGE("open '/sys/class/ak/source/reset' error(%d)", errno);
        return -1;
    }
    buf[0] = 0x55;
    buf[1] = 0xaa;
    buf[2] = 0x00;
    write(fd, buf, 3);
    close(fd);
    return 0;
}


JNIEXPORT jint JNICALL Java_com_my_hardware_setting_HWSetting_nativeSendCommand
        (JNIEnv *env, jobject this, jint what, jint nParam1, jint nParam2) {
    int ret;
    switch (what) {
        case com_my_hardware_setting_HWSetting_SETTING_GET_AUXIN:
            ret = GetAuxIn();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_AUXIN:
            ret = SetAuxIn(nParam1);
            break;
        case com_my_hardware_setting_HWSetting_SETTING_GET_LED:
            ret = GetLed();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_LED:
            ret = SetLed(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_DVD:
            ret = GetDVD();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_DVD:
            ret = SetDVD(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_REAKSW:
            ret = GetReakSW();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_REAKSW:
            ret = SetReakSW(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_ILLUMIN:
            ret = GetIllumin();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_ILLUMIN:
            ret = SetIllumin(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_RADIO:
            ret = GetRadio(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_RADIO:
            ret = SetRadio(nParam1, nParam2);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_FAN:
            ret = GetFan();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_FAN:
            ret = SetFan(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SECRET_RESET:
            ret = ResetMcuSecret();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_MCU_RESET:
            ret = ResetMcuFactory();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SYSTEM_RESET:
            ret = ResetSystemFactory();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_RADIO_REGION:
            ret = GetRadioRegion();
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_RADIO_REGION:
            ret = SetRadioRegion(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_GET_CAR_SUPPORT:
            ret = GetCarSupport(nParam1);
            break;

        case com_my_hardware_setting_HWSetting_SETTING_SET_CAR_SUPPORT:
            ret = SetCarSupport(nParam1, nParam2);
            break;
        default:
            LOGE("will do (%d)!!", what);
            break;
    }
    return ret;
}
