LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
       Update.c

LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) $(LOCAL_PATH)

LOCAL_SHARED_LIBRARIES := libutils libc
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := liblog libcutils

LOCAL_MODULE := libUpdate
LOCAL_PRELINK_MODULE := false
ifeq (1,$(strip $(shell expr $(PLATFORM_SDK_VERSION) \== 29)))
LOCAL_PRODUCT_MODULE := true
endif
ifeq (1,$(strip $(shell expr $(PLATFORM_SDK_VERSION) \< 29)))
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_MODULE)
endif
include $(BUILD_SHARED_LIBRARY)

