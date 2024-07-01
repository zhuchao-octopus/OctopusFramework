#include "com_my_hardware_dvs_DVideoSpec.h"

#define LOG_TAG "Dvd"

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
#include <sys/times.h>
#include <string.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close
#include <linux/types.h>
#include <asm/ioctls.h>

#include "mub_keypad.h"

#define DVD_RELEASE_ON				1

#if 1
#define DVD_PORT 	"/dev/ak-dvd"
#else //vk3224
#define DVD_PORT 	"/dev/ttyvk3x0"
#endif

#ifndef LOGD
#define LOGD ALOGE
#define LOGI ALOGE
#define LOGE ALOGE
#endif

#if DVD_RELEASE_ON
#define LOGDEBUG					
#define LOGERR	
#else
#define LOGDEBUG_BUF_LEN			400
#define LOGDEBUG					LOGE
#define LOGERR						LOGE				
#endif

#ifndef true
#define true 1
#endif

#ifndef false
#define false 0
#endif
typedef unsigned char BYTE;

enum {
	DISK_TYPE_NORMAL = 0, DISK_TYPE_DATA = 1
};

#define SHINWA_CMD_SYNC				0xAA
#define SHINWA_CMD_SOP_55			0x55
#define SHINWA_CMD_SOP_66			0x66

jboolean gbIsDVS = true;
char gSFSpeed = 0;
char gSRSpeed = 0;

static int LogDebugBuf(char *pszTitle, unsigned char *pucData, int nLen) {
#if !DVD_RELEASE_ON
	char szDebug[LOGDEBUG_BUF_LEN + 4] = { 0 };
	snprintf(szDebug, sizeof(szDebug) - 1, "%s[%d]", pszTitle, nLen);

	int i;
	int nOffset = strlen(szDebug);
	int nLength = sizeof(szDebug);
	for (i = 0; i < nLen && nOffset < LOGDEBUG_BUF_LEN; i++) {
		snprintf(szDebug + nOffset, nLength - nOffset, "<%02x>", pucData[i]);
		nOffset += 4;
	}

	LOGDEBUG("%s",szDebug);
#endif
	return 0;
}

JNIEXPORT jint JNICALL Java_com_octopus_android_carapps_hardware_dvs_DVideoSpec_nativeSendMSCCommand(
		JNIEnv *env, jobject thiz, jint value) {
	return 0;
}



#define DVS_READ_BUFF	256

#define DVS_WIDTH		720
#define DVS_HEIGHT		480

#define SCREEN_WIDTH	800
#define SCREEN_HEIGHT	480

#define AK_DVD_IOCTL_READ_DATA _IO('d', 0x40)
#define AK_DVD_IOCTL_WRITE_DATA        _IO('d', 0x41)
#define AK_DVD_IOCTL_CLEAR_RFIFO       _IO('d', 0x42)

struct DVideoSpec {
	unsigned int mask;
	JavaVM *jvm;
	JNIEnv *env;
	jobject thiz;
	jclass clazz;
	int fd;
	pthread_t tid;
	unsigned int texit;
	jmethodID dvsCallback;

	unsigned char info[DVS_READ_BUFF];
	char szFileName[200];
};

static void *dvs_thread(void *arg) {
	struct DVideoSpec *dvs = (struct DVideoSpec *) arg;
	JavaVM *jvm = dvs->jvm;
	int nread;
	unsigned char buf[DVS_READ_BUFF];
	int i;
	int ret;
	unsigned char checksum = 0;

	struct pollfd pfd[1];

	pfd[0].fd = dvs->fd;
	pfd[0].events = POLLIN | POLLERR;

	//clear buf

	memset(dvs->info, 0, DVS_READ_BUFF);
		
	//add shinwa dvd
	int nRevTotalLen = 0;
	int nNeedRecvLen = 0;
	unsigned char b;
	(*jvm)->AttachCurrentThread(jvm, &dvs->env, NULL);
	dvs->dvsCallback = (*dvs->env)->GetMethodID(dvs->env, dvs->clazz,
			"dvsCallback", "([BI)V");
			int nReadLen = 2;
	while (!dvs->texit) {
		memset(dvs->info, 0, sizeof(dvs->info));
		if((ioctl(dvs->fd, AK_DVD_IOCTL_READ_DATA, dvs->info ) == 0 )){
		
	//	int nReadLen = read(dvs->fd, dvs->info, sizeof(dvs->info));
		
	//	usleep(200);
	//	close(dvs->fd);
	//	dvs->fd = -1;
	//		dvs->fd = open(DVD_PORT, O_RDWR, 0666);
			
		//LOGE("Recv[0x%x][0x%x][0x%x][0x%x][0x%x][0x%x][0x%x][%d]\n",dvs->info[0],dvs->info[1],dvs->info[2],dvs->info[3],dvs->info[4],dvs->info[5],dvs->info[6],nReadLen);
	//change for kernel bug.
	b = dvs->info[0];
	dvs->info[0] = dvs->info[1];
	dvs->info[1] = b;

//LOGE("Recv[0x%x][0x%x]\n",dvs->info[0],dvs->info[1]);

					
						
						jbyteArray bytes =(*dvs->env)->NewByteArray(dvs->env,nReadLen);
						(*dvs->env)->SetByteArrayRegion(dvs->env,bytes, 0, nReadLen, (jbyte*)(dvs->info));								
						
						(*dvs->env)->CallVoidMethod(dvs->env, dvs->thiz, dvs->dvsCallback, bytes, nReadLen);
	
					//	(*env)->CallVoidMethod(dvs->env, pstMyBtNfore->this, pstMyBtNfore->btCallBack, bytes, len);						
						(*dvs->env)->DeleteLocalRef(dvs->env,bytes);
						
						}
	}

	(*jvm)->DetachCurrentThread(jvm);
	return NULL;
}

static struct DVideoSpec dvs = { .fd = -1};


int SendShinwaCommand(JNIEnv *env, jobject thiz, jint cmd, jint param1,
		jint param2) {
	unsigned char tx[16] = { 0 };
	
				//LOGE("SendShinwaCommand(%d)", cmd);
				
	switch (cmd) {
		case com_my_hardware_dvs_DVideoSpec_DVS_COMMON_CMD_1_PARAM:
		if(dvs.fd>0){
			ioctl(dvs.fd, AK_DVD_IOCTL_WRITE_DATA, &param1 );
		}
		LOGE("write '0x%x'", param1);
		break;
	case com_my_hardware_dvs_DVideoSpec_DVS_OPEN:
		if (dvs.fd < 0) {
			jclass clazz;
			jfieldID mDVSMask;
			struct termios tio;
			dvs.fd = open(DVD_PORT, O_RDWR, 0666);
			if (dvs.fd < 0) {
				LOGE("open '%s' error(%d)", DVD_PORT, errno);
				return -1;
			}
			
			
				LOGE("open '%s' fd:(0x%x)", DVD_PORT, dvs.fd);
		//	fcntl(dvs.fd, F_SETFL, 0);
		//	tcgetattr(dvs.fd, &tio);
		//	cfmakeraw(&tio);
		//	cfsetospeed(&tio, B19200);
		//	cfsetispeed(&tio, B19200);
		//	if (tcsetattr(dvs.fd, TCSAFLUSH, &tio)) {
		//		LOGE("tcsetattr failed");
		//		close(dvs.fd);
		//		dvs.fd = -1;
		//		return -1;
		//	}
			clazz = (*env)->GetObjectClass(env, thiz);
			mDVSMask = (*env)->GetFieldID(env, clazz, "mDVSMask", "I");
			dvs.mask = (*env)->GetIntField(env, thiz, mDVSMask);
			(*env)->GetJavaVM(env, &dvs.jvm);
			dvs.thiz = (*env)->NewGlobalRef(env, thiz);
			dvs.clazz = (*env)->NewGlobalRef(env, clazz);
			dvs.texit = 0;


		
			pthread_create(&dvs.tid, NULL, dvs_thread, &dvs);
		}
		break;
	case com_my_hardware_dvs_DVideoSpec_DVS_CLOSE:
		if (dvs.fd >= 0) {
			dvs.texit = 1;
			pthread_join(dvs.tid, NULL);
			(*env)->DeleteGlobalRef(env, dvs.clazz);
			(*env)->DeleteGlobalRef(env, dvs.thiz);
			close(dvs.fd);
			dvs.fd = -1;
		}
		break;

	}
	return 0;
}

JNIEXPORT jint JNICALL Java_com_octopus_android_carapps_hardware_dvs_DVideoSpec_nativeSendDVSCommand__I(
		JNIEnv *env, jobject thiz, jint cmd) {

	return SendShinwaCommand(env, thiz, cmd, 0, 0);
}

JNIEXPORT jint JNICALL Java_com_octopus_android_carapps_hardware_dvs_DVideoSpec_nativeSendDVSCommand__III(
		JNIEnv *env, jobject thiz, jint cmd, jint param1, jint param2) {

	return SendShinwaCommand(env, thiz, cmd, param1, param2);

}
