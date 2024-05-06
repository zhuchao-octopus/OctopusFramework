#include <errno.h>
#include <fcntl.h>
#include <termios.h>
#include <pthread.h>
#include<sys/types.h>
#include <stdlib.h>
#include <poll.h>
#include<sys/time.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close
#include <string.h>
#include <stdio.h>
//#include <cutils/properties.h>
#include <jni.h>

#define LOG_TAG "BT_HW"
#define LOG_TAG1 "BT_HW1"
#define LOG_TAG2 "BT_HW2"
//#undef LOG
//#include <utils/Log.h>
#include<android/log.h>

//#include "ATBluetooth.h"
#include "bluetooth.h"
#include "client.h"


#define BT_RECV_BUF_LEN_MAX        256
typedef unsigned char BYTE;
typedef struct {
    JavaVM *jvm;
    jobject this;
    jclass clazz;

    int fd;
    pthread_t tid;
    unsigned int texit;


    jmethodID btCallBack;
} JniJavaStruct;

static JniJavaStruct g_sJava = {.fd = -1,};
static int update_bt = 0;
//for debug


#define BTNFORE_RELEASE_ON 0

#if BTNFORE_RELEASE_ON
#define BTNFORE_DEBUG_ON				0
#define BTNFORE_DEBUG1_ON				0
#define BTNFORE_DEBUG2_ON				0
#define BTNFORE_DEBUG3_ON				0
#define LOGI(fmt, args...) 
#define LOGD(fmt, args...) 
#define LOGE(fmt, args...)
#else
#define BTNFORE_DEBUG_ON                1
#define BTNFORE_DEBUG1_ON                1
#define BTNFORE_DEBUG2_ON                1
#define BTNFORE_DEBUG3_ON                1

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG2, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG1, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

#endif

#if BTNFORE_DEBUG_ON

#define LOGDEBUG                    LOGE
#define LOGERROR                    LOGE
#else
#define LOGDEBUG
#define LOGERROR					LOGE
#endif

#define BT_TYPE_IVT        0
#define BT_TYPE_PARROT        1
#define BT_TYPE_GOC        2

static int g_bt_type = BT_TYPE_IVT;


#define LOGDEBUG_BUF_LEN            200

static int LogDebugBuf(char *pszTitle, BYTE *pucData, int nLen) {
#if BTNFORE_DEBUG_ON
    char szDebug[LOGDEBUG_BUF_LEN + 4] = {0};
    snprintf(szDebug, sizeof(szDebug) - 1, "%s[%d]", pszTitle, nLen);

    int i;
    int nOffset = strlen(szDebug);
    int nLength = sizeof(szDebug);
    for (i = 0; i < nLen && nOffset < LOGDEBUG_BUF_LEN; i++) {
        snprintf(szDebug + nOffset, nLength - nOffset, "<%02x>", pucData[i]);
        nOffset += 4;
    }
    LOGDEBUG("%s", szDebug);
#endif
    return 0;
}

static void *RecvThread(void *arg) {
    JniJavaStruct *pstMyBtNfore = (JniJavaStruct *) arg;
    JNIEnv *env;
    JavaVM *jvm = pstMyBtNfore->jvm;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    LOGERROR("@@@@@@@@@@@@ RecvThread[1] error(%d)\n", errno);

    char event[BT_RECV_BUF_LEN_MAX * 2];
    int rc;
    int len;
    int read_buf[BT_RECV_BUF_LEN_MAX] = {0};
    int last_len = 0;

    fd_set fds;
    int maxfdp;
    struct timeval timeout;
    int g_ivt_err = 0;
    while (!pstMyBtNfore->texit) {

        if (pstMyBtNfore->fd > 0) {
            //test
            FD_ZERO(&fds); //每次循环都要清空集合，否则不能检测描述符变化
            FD_SET(pstMyBtNfore->fd, &fds); //添加描述符

            maxfdp = pstMyBtNfore->fd + 1;    //描述符最大值加1
            timeout.tv_sec = 0;
            timeout.tv_usec = 400000;
            int ret = (select(maxfdp, &fds, NULL, NULL, &timeout));  //select使用

            //LOGERROR("@@@@@@@@@@@@ 1111111RecvThread[1] error(%d)\n", ret);
            if (ret <= 0) {
                //	LOGERROR("@@@@@@@@@@@@ 1122211111RecvThread[1] error(%d)\n", ret);
                continue;
            }

            memset(read_buf, 0, sizeof(read_buf));
            rc = read(pstMyBtNfore->fd, read_buf, BT_RECV_BUF_LEN_MAX);

            if (rc == 0) {
                //LOGE("read received  rc:%d %dfail ", rc, g_ivt_err );
                //++g_ivt_err;
                //if (g_ivt_err > 30){

                //	char crash[] = {'C', 'R', 'A', 'S', 'H'};
                //	jbyteArray bytes =(*env)->NewByteArray(env,6);
                //	(*env)->SetByteArrayRegion(env,bytes, 0, 6, (jbyte*)(crash));

                //	(*env)->CallVoidMethod(env, pstMyBtNfore->this, pstMyBtNfore->btCallBack, bytes, len);
                //	(*env)->DeleteLocalRef(env,bytes);
                //	break;
                //}
                continue;
            }
            g_ivt_err = 0;

            if (last_len == 0) {
                memset(event, 0, sizeof(event));
                memcpy(event, read_buf, rc);
            } else {
                rc += last_len;
                memcpy(event + last_len, read_buf, rc);
                last_len = 0;
            }

            //	LOGE("read received !!!!!   rc:%d last_len:%d  %s ", rc,last_len, event );


            if (rc > 2) {
                int i;
                char data_cb[BT_RECV_BUF_LEN_MAX] = {0};
                char data_cb0[BT_RECV_BUF_LEN_MAX] = {0};
                char *p1, *p2;
                p1 = p2 = event;

                for (i = 0; i < rc; i++) {

                    if (*p1++ == '\0') {
                        memset(data_cb, 0, sizeof(char) * BT_RECV_BUF_LEN_MAX);

                        memcpy(data_cb, p2, p1 - p2);
                        len = strlen(data_cb) - 2;

                        if (len < 2 || len >= BT_RECV_BUF_LEN_MAX * 2) {
                            LOGE("err2222 msg     : 0x%x, 0x%x, len:%d, i=%d,l=%d", *p1, *p2, len, (p2 - event), (p1 - event));
                            p2 = p1;
                            continue;
                        }
                        //LOGE("received    : %s, %s, len:%d, i=%d,l=%d", data_cb, data_cb, len, i, (p1-p2));

                        jbyteArray bytes = (*env)->NewByteArray(env, len);
                        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) (data_cb));

                        (*env)->CallVoidMethod(env, pstMyBtNfore->this, pstMyBtNfore->btCallBack, bytes, len);
                        (*env)->DeleteLocalRef(env, bytes);
                        p2 = p1;
                    }
                }

                if ((p2 - event) < rc) {
                    last_len = p1 - p2;
                    memcpy(event, p2, last_len);
                    memset(event + last_len, 0, sizeof(event) - last_len);
                    /*
                    int j=0;
                    for (;j<last_len;++j){
                        LOGE("%02x", event[j] );
                    }*/
                    LOGE("received2222cc: len:%d %d ", (last_len), rc);
                }
            }
        }
    }

    (*jvm)->DetachCurrentThread(jvm);
    LOGE("read received !!!!!  quit!!!!!!!!!!!!!!");
    return NULL;
}


static void returnToJava(JniJavaStruct *pstMyBtNfore, JNIEnv *env, char *data_cb, int len) {
    //char data_cb[BT_RECV_BUF_LEN_MAX] = {0};
    //memset(data_cb, 0, sizeof(char)*BT_RECV_BUF_LEN_MAX);
    //len = end-start;
    //memcpy(data_cb, data_cb2, len);

    //LogDebugBuf("ok string:", data_cb, len);
    //LOGD("ok string %s", data_cb);

    jbyteArray bytes = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) (data_cb));

    (*env)->CallVoidMethod(env, pstMyBtNfore->this, pstMyBtNfore->btCallBack, bytes, len);
    (*env)->DeleteLocalRef(env, bytes);
}

static int dropIncrrectPreCmd(char *event, int len) {
    int i;

    for (i = 0; i < len; i++) //find start
    {
        //LOGE("find start:%d %d" , *(event+i), *(event+i+1));
        if (*(event + i) == 0xd && *(event + i + 1) == 0xa) {
            break;
        }
    }
    return i;
}

static char *findCmd(char *event, int len) {
    int i;

    int start = -1;
    for (i = start; i < len; i++) //find end
    {
        //LOGE("find end:%d %d" , *(event+i), *(event+i+1));
        if (*(event + i) == 0xd && *(event + i + 1) == 0xa) {
            return event + i + 2;
        }
    }
    return NULL;
}

static void update_bt_parrot(char *buf, int len) {
    LogDebugBuf("update ret:", buf, len);
}

static void *RecvThreadParrot(void *arg) {
    JniJavaStruct *pstMyBtNfore = (JniJavaStruct *) arg;
    JNIEnv *env;
    JavaVM *jvm = pstMyBtNfore->jvm;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    //LOGERROR("@@@@@@@@@@@@ RecvThread[1] error(%d)\n", errno);
    char event[BT_RECV_BUF_LEN_MAX * 2];
    int rc;
    int len;
    int read_buf[BT_RECV_BUF_LEN_MAX] = {0};
    int last_len = 0;
    while (!pstMyBtNfore->texit) {

        if (pstMyBtNfore->fd > 0) {
            //	LOGE("r1   last_len:%d   ", last_len );
            memset(read_buf, 0, sizeof(read_buf));
            rc = read(pstMyBtNfore->fd, read_buf, BT_RECV_BUF_LEN_MAX);
            if (update_bt == 1) {
                update_bt_parrot(read_buf, rc);
                continue;
            }
            //LOGE("read :%s", read_buf);
            returnToJava(pstMyBtNfore, env, read_buf, rc); //all java do it now
            usleep(20000);//why....is java do slow??
            /*
            continue;
            //test
            LOGE("r2   rc:%d   ", rc);
            LogDebugBuf("read received1:", read_buf, rc);
            //LOGE("read received1: %s ", read_buf);

            if(last_len > 0 && last_len < ((BT_RECV_BUF_LEN_MAX*2)-rc)){
                rc+=last_len;
                memcpy(event+last_len, read_buf, rc);
                last_len = 0;
            }
            else
            {
                last_len = 0;
                memcpy(event, read_buf, rc);
            }

            LogDebugBuf("read received2:", event, rc);

            int drop = dropIncrrectPreCmd(event, rc);
            if(drop >= rc)
            {
                LOGE("uncorrect cmd!!!!!!!!!");
                continue;
            }
            else if(drop > 0)
            {
                LOGE("c 3 need drop :%d", drop);
                memcpy(event, event+drop, rc-drop);
                rc = rc-drop;
                //LogDebugBuf("event received drop:", event, rc);
            }

            char *pStart = event;
            char *pEnd = pStart;
            char cmd[BT_RECV_BUF_LEN_MAX] = {0};
            int cmdLen = 0;
            int done_len = 0;
            do{
                pEnd = findCmd(pStart+2, rc-(pEnd-pStart));

                LOGE("rc 4 findCmd %x:%x", pStart, pEnd);

                if(pEnd != NULL)
                {

                    memset(cmd, 0, sizeof(cmd));
                    done_len = pEnd-pStart;
                    cmdLen = pEnd-pStart-4;

                    LOGE("rc 5 findCmd :%d", cmdLen);
                    if(cmdLen>0 && cmdLen<sizeof(cmd)){
                        memcpy(cmd, pStart+2, cmdLen);
                        returnToJava(pstMyBtNfore, env, cmd, cmdLen);
                    }
                    else if(cmdLen == 0)
                    {
                        pEnd = pEnd - 2;
                    }
                    if((done_len)<rc)
                    {
                        pStart = pEnd;
                    } else {
                        LOGE("do cmd finish!");
                        break;
                    }
                } else {

                    last_len = rc-(done_len);

                    LOGE("rc 6 no cmd :%d", done_len);
                    if(last_len>0)
                    {
                        memset(cmd, 0, sizeof(cmd));

                        //LogDebugBuf("1last buf:", event, rc);
                        memcpy(cmd, event+done_len, last_len);
                        //LogDebugBuf("1cmd:", cmd, last_len);
                        memset(event, 0, sizeof(event));
                        memcpy(event, cmd, last_len);
                    } else {

                        memset(event, 0, sizeof(event));
                    }
                    LogDebugBuf("3last buf:", event, rc);
                    break;
                }
            }while(1);
            */
        }
    }

    (*jvm)->DetachCurrentThread(jvm);
    return NULL;
}

static void *RecvThreadGoc(void *arg) {
    JniJavaStruct *pstMyBtNfore = (JniJavaStruct *) arg;
    JNIEnv *env;
    JavaVM *jvm = pstMyBtNfore->jvm;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    LOGERROR("!!!!!! RecvThreadGoc[1] error(%d)\n", errno);
    char event[BT_RECV_BUF_LEN_MAX * 2];
    int rc;
    int len;
    int read_buf[BT_RECV_BUF_LEN_MAX] = {0};
    int last_len = 0;

    fd_set fds;
    int maxfdp;
    struct timeval timeout;

    while (!pstMyBtNfore->texit) {

        if (pstMyBtNfore->fd > 0) {
            FD_ZERO(&fds); //每次循环都要清空集合，否则不能检测描述符变化
            FD_SET(pstMyBtNfore->fd, &fds); //添加描述符
            maxfdp = pstMyBtNfore->fd + 1;    //描述符最大值加1
            timeout.tv_sec = 0;
            timeout.tv_usec = 400000;
            int ret = (select(maxfdp, &fds, NULL, NULL, &timeout));  //select使用

            //	LOGERROR("@@@@@@@@@@@@ 1111111RecvThread[1] error(%d)\n", ret);
            if (ret <= 0) {
                //	LOGERROR("@@@@@@@@@@@@ 1122211111RecvThread[1] error(%d)\n", ret);
                continue;
            }

            memset(read_buf, 0, sizeof(read_buf));
            rc = read(pstMyBtNfore->fd, read_buf, BT_RECV_BUF_LEN_MAX);

            if (rc > 0) {
                returnToJava(pstMyBtNfore, env, read_buf, rc); //all java do it now
            }
            //usleep(20000);//why....is java do slow??
        }
    }

    (*jvm)->DetachCurrentThread(jvm);

    return NULL;
}


static void test() {

    char event[BT_RECV_BUF_LEN_MAX * 2];
    int rc;
    int len;
    int read_buf[BT_RECV_BUF_LEN_MAX] = {0};
    int last_len = 0;

    char t[] = {0xd, 0x0a, 0x2a, 0x50, 0x49, 0x4e, 0x44, 0x3a, 0x31, 0x2c, 0x31, 0x2c, 0x31, 0x2c, 0x31,
                0x2c, 0x31, 0x2c, 0x31, 0x2c, 0x30, 0x2c, 0x30, 0x0d, 0x0a, 0x0d, 0x0a, 0x4f, 0x4b, 0x0d, 0x0a, 0x0d, 0x0a,
                0x0};
    if (1) {
        memset(read_buf, 0, sizeof(read_buf));

        //rc = read(pstMyBtNfore->fd, read_buf, BT_RECV_BUF_LEN_MAX);

        rc = strlen(t);
        memcpy(read_buf, t, rc);
        LogDebugBuf("read received1:", read_buf, rc);

        if (last_len == 0) {
            memset(event, 0, sizeof(event));
            memcpy(event, read_buf, rc);

            if (rc == 1) {
                if ((*event) == 0xd) {
                    last_len = 1;
                }
                LOGE("only one byte cmd !!!!! :%d ", (*event));
                //continue;
            }
        } else {
            rc += last_len;
            memcpy(event + last_len, read_buf, rc);
            last_len = 0;
        }
        LOGE("read received 2   rc:%d last_len:%d  %s ", rc, last_len, event);
        //LogDebugBuf("event received2:", event, rc);
        int i;
        char data_cb[BT_RECV_BUF_LEN_MAX] = {0};
        char *p1;
        p1 = event;

        int start = -1;
        int end = -1;

        while (rc >= 2) {
            //find a cmd
            for (i = start + 1; i < rc - 1; i++) //find start
            {

                //LOGE("find start:%d %d" , *(event+i), *(event+i+1));
                if (*(event + i) == 0xd && *(event + i + 1) == 0xa) {
                    start = i + 2;
                    break;
                }
            }

            LOGE("1 start:%d %d", start, (p1 - event));

            if ((start != -1) && (start <= (rc - 1)) && (start > (p1 - event))) {
                for (i = start; i < rc - 1; i++) // find end
                {


                    LOGE("find end:%d %d", *(event + i), *(event + i + 1));
                    if (*(event + i) == 0xd && *(event + i + 1) == 0xa) {
                        end = i;
                        break;
                    }
                }
            } else        //no cmd
            {
                last_len = rc - (p1 - event);


                LOGE("last_len:%d %d", last_len, rc);
                if (last_len > 0) {
                    memcpy(event, p1, last_len);

                    memset(event + last_len, 0, sizeof(event) - last_len);
                }
                break;
            }

            LOGE("1 end:%d,%d", end, rc);

            if ((end != -1) && (end <= (rc - 1)) && (end > (p1 - event)))// ok cmd
            {
                memset(data_cb, 0, sizeof(char) * BT_RECV_BUF_LEN_MAX);
                len = end - start;
                memcpy(data_cb, event + start, len);
                LogDebugBuf("ok string:", data_cb, len);

                //rc = rc - len-2;
                if (rc >= (end + 4)) {
                    p1 = event + end + 2;
                    start = end + 1;
                } else {
                    if (rc >= (end + 3)) //end msg
                    {
                        LOGE("msg end:%d %d", *(event + end + 2), end);
                        if (*(event + end + 2) == 0xd) {
                            last_len = 1;
                            memcpy(event, p1, last_len);
                            memset(event + last_len, 0, sizeof(event) - last_len);
                        }
                    }
                    break;
                }
            } else        //no cmd
            {
                last_len = rc - (p1 - event);

                LOGE("2 last_len:%d %d", last_len, rc);
                if (last_len > 0) {
                    memcpy(event, p1, last_len);

                    memset(event + last_len, 0, sizeof(event) - last_len);
                }
                break;
            }
        }
    }
}

/*
init ivt
*/

static int initIVT() {
    g_sJava.fd = connect_to_server();
    if (g_sJava.fd > 0) {
        LOGE("IVT Server connected!\n");
        pthread_create(&g_sJava.tid, NULL, RecvThread, &g_sJava);
        return 0;
    } else {
        LOGE("initIVT Can NOT connect to the bluetooth server.\n");
        return -1;
    }
}

static int isPX6() {
    char vendor[10] = "";
    //property_get("ro.build.product", vendor, "");
    //if (strstr(vendor, "rk3399")) {
    return 1;
    //} else {
    //    return 0;
    //}
}

static int initParrot() {
    if (g_sJava.fd < 0) {
        struct termios tio;

        if (isPX6()) {
            g_sJava.fd = open("/dev/ttyS0", O_RDWR | O_NONBLOCK | O_NOCTTY, 0666);
            if (g_sJava.fd < 0) {
                LOGERROR("open '%s' error(%d)", "parrot /dev/ttyS0", errno);
                return -1;
            }
        } else {
            g_sJava.fd = open("/dev/ttySAC2", O_RDWR | O_NONBLOCK | O_NOCTTY, 0666);
            if (g_sJava.fd < 0) {
                LOGERROR("open '%s' error(%d)", "parrot /dev/ttySAC2", errno);
                g_sJava.fd = open("/dev/ttyS1", O_RDWR | O_NONBLOCK | O_NOCTTY, 0666);
                if (g_sJava.fd < 0) {
                    LOGERROR("open '%s' error(%d)", "parrot /dev/ttyS1", errno);
                    return -1;
                }
            }
        }
        fcntl(g_sJava.fd, F_SETFL, 0);
        tcgetattr(g_sJava.fd, &tio);
        cfmakeraw(&tio);
        cfsetospeed(&tio, B115200);
        cfsetispeed(&tio, B115200);
        tio.c_cflag &= ~CSTOPB;
        if (tcsetattr(g_sJava.fd, TCSAFLUSH, &tio)) {
            LOGERROR("tcsetattr failed");
            return -1;
        }
        pthread_create(&g_sJava.tid, NULL, RecvThreadParrot, &g_sJava);
    }

    return 0;
}


static int initGoc() {
    if (g_sJava.fd < 0) {
        struct termios tio;
        g_sJava.fd = open("/dev/BT_serial", O_RDWR | O_NONBLOCK | O_NOCTTY, 0666);
        LOGERROR("55553initGoc /dev/BT_serial");
        if (g_sJava.fd < 0) {
            LOGERROR("open '%s' error(%d)", "/dev/BT_serial", errno);
            return -1;
        }

        fcntl(g_sJava.fd, F_SETFL, 0);
        tcgetattr(g_sJava.fd, &tio);
        cfmakeraw(&tio);
        cfsetospeed(&tio, 500000);
        cfsetispeed(&tio, 500000);
        tio.c_cflag &= ~CSTOPB;
        if (tcsetattr(g_sJava.fd, TCSAFLUSH, &tio)) {
            LOGERROR("tcsetattr goc failed");
            return -1;
        }

        pthread_create(&g_sJava.tid, NULL, RecvThreadGoc, &g_sJava);
    }

    return 0;
}

static int initBT(int type) {
    int ret = -1;
    g_bt_type = type;
    if (type == BT_TYPE_IVT) {
        ret = initIVT();
    } else if (type == BT_TYPE_PARROT) {
        ret = initParrot();
    } else if (type == BT_TYPE_GOC) {
        ret = initGoc();
    }
    return ret;
}

static void deinitIVT() {
    disconnect_from_server();
    LOGE("IVT disconnect_from_server!\n");
}

static void deinitBT() {
    if (g_bt_type == BT_TYPE_IVT) {
        deinitIVT();
    } else if (g_bt_type == BT_TYPE_PARROT) {
        //return initParrot();
    }
}

static int WriteSerial(const char *pucData) {
    if (update_bt == 1) {
        return 0;
    }
    int nDataLen = strlen(pucData);

    LOGD("[WriteSerial] %s", pucData);
    LogDebugBuf("WriteSerial", pucData, nDataLen);
    if (g_sJava.fd >= 0) {
        write(g_sJava.fd, pucData, nDataLen);
    } else {
        LOGERROR("WriteSerial error\n");
    }
    return 0;
}

static void sendCmd(const char *command) {
    if (g_bt_type == BT_TYPE_IVT) {
        send_command(command);
    } else if (g_bt_type == BT_TYPE_PARROT
               || g_bt_type == BT_TYPE_GOC) {
        WriteSerial(command);
    }
}

static int WriteSerialUpdate(const char *pucData) {

    int nDataLen = strlen(pucData);

    LogDebugBuf("WriteSerialUpdate", pucData, nDataLen);
    if (g_sJava.fd >= 0) {
        write(g_sJava.fd, pucData, nDataLen);
    } else {
        LOGERROR("WriteSerial error\n");
    }
    return 0;
}

#define MAX_UPDATE_BUFF        16*1024

static void updateBT(JNIEnv *env, char *path) {
    LOGD("updateBT %s", path);
    update_bt = 1;
    //char t[2]={0xa3,0x0};
    //usleep(2000000);
    //WriteSerialUpdate(t);
    FILE *fd = fopen(path, "r");
    if (fd != NULL) {

        int size = 0;
        size_t read_size;
        //char buf[MAX_UPDATE_BUFF];
        char *buf = malloc(MAX_UPDATE_BUFF);
        fseek(fd, SEEK_SET, SEEK_END);
        size = ftell(fd);
        fseek(fd, SEEK_SET, 0);

        jmethodID updateCallback = (*env)->GetMethodID(env, g_sJava.clazz,
                                                       "updateCallback", "(I)V");
        LOGD("updateCallback %d ", updateCallback);

        int per = -1;
        int read_total = 0;
        do {
            memset(buf, 0, MAX_UPDATE_BUFF);
            read_size = fread(buf, 1, MAX_UPDATE_BUFF, fd);
            //LOGD("updateBT end2");
            //	size = size-read_size;
            read_total += read_size;
            //send to bt

            usleep(10000);

            int p = (read_total * 100) / size;
            LOGD("read_total %d, size %d, per %d", read_total, size, p);

            //send to ui
            if (updateCallback != NULL) {
                if (per != p) {
                    per = p;
                    (*env)->CallVoidMethod(env, g_sJava.this, updateCallback, p);
                    LOGD("return %d ", p);
                }
            }
        } while (read_total < size);

        usleep(200000);
        LOGD("updateBT end!!!!!!!!!!!!");
        fclose(fd);
        free(buf);
    }
}


/*
* Class:     com_my_hw_ATBluetooth
* Method:    nativeSendCommand
* Signature: (IIILjava/lang/String;)I
*/
JNIEXPORT jint JNICALL

Java_com_zhuchao_android_bt_hw_ATBluetooth_nativeSendCommand(JNIEnv *env, jobject this, jint what, jint arg1, jint arg2, jbyteArray obj) {
    switch (what) {
        case 0x80:
            if (g_sJava.fd < 0) {
                (*env)->GetJavaVM(env, &g_sJava.jvm);
                g_sJava.this = (*env)->NewGlobalRef(env, this);
                g_sJava.clazz = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this));
                g_sJava.btCallBack = (*env)->GetMethodID(env, g_sJava.clazz, "dataCallback", "([BI)V");
                g_sJava.texit = 0;
                //test();
                return initBT(arg1);
            }
            break;
        case 0xff:
            if (g_sJava.fd >= 0) {
                close(g_sJava.fd);
                g_sJava.fd = -1;

                g_sJava.texit = 1;

                deinitBT();

                (*env)->DeleteGlobalRef(env, g_sJava.clazz);
                (*env)->DeleteGlobalRef(env, g_sJava.this);
            }
            break;
        case com_my_hw_ATBluetooth_WRITE_DATA: {
            jbyte *jparam = (*env)->GetByteArrayElements(env, obj, 0);

            if (jparam != NULL && g_sJava.fd >= 0) {
                sendCmd(jparam);
            }
            break;
        }
        case com_my_hw_ATBluetooth_REQUEST_UPDATE_BT: {

            if (obj != NULL) {
                jbyte *jparam = (*env)->GetByteArrayElements(env, obj, 0);
                updateBT(env, jparam);
            }
            break;
        }
    }
    return 0;
}
