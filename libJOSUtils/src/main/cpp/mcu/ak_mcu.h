/*
*
* Copyright (c) 201109  allen <allen@seanovo.com.cn>
*
* This program is free software;
*/

#ifndef __ak_canbox_h__
#define __ak_canbox_h__

#include <linux/types.h>
#include <asm/ioctls.h>


#define AK_MCU_COMMON_DATA    (0x01 << 1)

#define FOCUS_MAX_DATA    254
#define AK_MCU_READ_COMMON_DATA    _IO('m', 0x30)
#define AK_MCU_WRITE_COMMON_DATA    _IO('m', 0x31)


#define AK_MCU_WAIT_EVENT        _IO('m', 0x40)

#define AK_READ_KERNEL_APP_PRO_DATA    _IO('m', 0x7e)
#define AK_REVERSE_WAIT_EVENT _IO('m', 0x7f)


#define AK_KEYPAD_VK _IOW('m', 0xc0, unsigned int)

#endif // __ak_canbox_h__

