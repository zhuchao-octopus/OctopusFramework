
#ifndef __mub_update_h__
#define __mub_update_h__

#include <linux/types.h>
#include <asm/ioctls.h>

#define MUB_UPDATE_READ_POS _IOR('m', 0xb1, unsigned short)
#define MUB_UPDATE_READ_FLAG _IOR('m', 0xb2, unsigned char)
#define MUB_UPDATE_START _IO('m', 0xb8)
#define MUB_UPDATE_RESET _IO('m', 0xb9)
#define MUB_UPDATE_WAIT_EVENT _IO('m', 0xbf)

#define MUB_UPDATE_POS 0x01
#define MUB_UPDATE_FLAG 0x02

#define MUB_UPDATE_FLAG_START 0x0
#define MUB_UPDATE_FLAG_UNLOCK 0x1
#define MUB_UPDATE_FLAG_DATA 0x2
#define MUB_UPDATE_FLAG_CHECKSUM 0x3
#define MUB_UPDATE_FLAG_RESET 0x4

#endif // __mub_update_h__
