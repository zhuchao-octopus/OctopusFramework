/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_my_hardware_mud_MubUpdate */

#ifndef _Included_com_my_hardware_mud_MubUpdate
#define _Included_com_my_hardware_mud_MubUpdate
#ifdef __cplusplus
extern "C" {
#endif
#undef com_my_hardware_mud_MubUpdate_MUD_START
#define com_my_hardware_mud_MubUpdate_MUD_START 128L
#undef com_my_hardware_mud_MubUpdate_MUD_RESET
#define com_my_hardware_mud_MubUpdate_MUD_RESET 255L
#undef com_my_hardware_mud_MubUpdate_MUD_POS
#define com_my_hardware_mud_MubUpdate_MUD_POS 1L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG
#define com_my_hardware_mud_MubUpdate_MUD_FLAG 2L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG_START
#define com_my_hardware_mud_MubUpdate_MUD_FLAG_START 0L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG_UNLOCK
#define com_my_hardware_mud_MubUpdate_MUD_FLAG_UNLOCK 1L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG_DATA
#define com_my_hardware_mud_MubUpdate_MUD_FLAG_DATA 2L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG_CHECKSUM
#define com_my_hardware_mud_MubUpdate_MUD_FLAG_CHECKSUM 3L
#undef com_my_hardware_mud_MubUpdate_MUD_FLAG_RESET
#define com_my_hardware_mud_MubUpdate_MUD_FLAG_RESET 4L
/*
 * Class:     com_my_hardware_mud_MubUpdate
 * Method:    nativeSendMUDCommand
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_my_hardware_mud_MubUpdate_nativeSendMUDCommand
        (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_my_hardware_mud_MubUpdate_nativeSendMUDCommand2
        (JNIEnv *, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif
