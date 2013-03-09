LOCAL_PATH := $(call my-dir)

WAYLAND_APP_SRC := \
	src/jni_util.c \
	src/posix.c

include $(CLEAR_VARS)

LOCAL_MODULE 			:= wheatley-lib
LOCAL_SRC_FILES 		:= $(WAYLAND_APP_SRC)

include $(BUILD_SHARED_LIBRARY)

