

#ifndef __mub_keypad_h__
#define __mub_keypad_h__

#include <linux/types.h>
#include <asm/ioctls.h>

#define MUB_KEYPAD_KEY_SOURCE 0x00000001 // SOURCE
#define MUB_KEYPAD_KEY_MUTE 0x00000002 // MUTE
#define MUB_KEYPAD_KEY_EJECT 0x00000004 // EJECT
#define MUB_KEYPAD_KEY_TS 0x00000008 // TS
#define MUB_KEYPAD_KEY_BL_LEVEL 0x00000010 // BACKLIGHT LEVEL
#define MUB_KEYPAD_KEY_AUX 0x00000020 // AUX
#define MUB_KEYPAD_KEY_NEXTSONG 0x00000040 // NEXTSONG
#define MUB_KEYPAD_KEY_PREVIOUSSONG 0x00000080 // PREVIOUSSONG
#define MUB_KEYPAD_KEY_GPS 0x00000100 // GPS
#define MUB_KEYPAD_KEY_FM 0x00000200 // FM
#define MUB_KEYPAD_KEY_DVD 0x00000400 // DVD
#define MUB_KEYPAD_KEY_DTV 0x00000800 // DTV
#define MUB_KEYPAD_KEY_IPOD 0x00001000 // IPOD
#define MUB_KEYPAD_KEY_BT 0x00002000 // BT

#define MUB_KEYPAD_KEY_GPS_SOUND_ON  0x00004000 // 
#define MUB_KEYPAD_KEY_GPS_SOUND_OFF 0x00008000 // 

#define MUB_KEYPAD_KEY_SCW 0x00010000 // ^>
#define MUB_KEYPAD_KEY_SCCW 0x00020000 // <^
#define MUB_KEYPAD_KEY_TV 0x00040000 // TV
#define MUB_KEYPAD_KEY_EQ 0x00080000 // EQ

#define MUB_KEYPAD_KEY_BT_DIAL 0x00100000 // BT dial
#define MUB_KEYPAD_KEY_BT_HANG 0x00200000 // BT hang
#define MUB_KEYPAD_KEY_MODE 0x00400000 // MODE

#define MUB_KEYPAD_BRAKE_CAR_ON 0x01000000 // 
#define MUB_KEYPAD_BRAKE_CAR_OFF 0x02000000 // 

#define MUB_KEYPAD_VK _IOW('m', 0xc0, unsigned int)
#define MUB_KEYPAD_LOCK _IOW('m', 0xc8, unsigned int)
#define MUB_KEYPAD_WAIT_EVENT _IO('m', 0xcf)

#define MUB_KEYPAD_REQUEST_BRAKE_CAR_INFO _IO('m', 0xce)

#endif // __mub_keypad_h__
