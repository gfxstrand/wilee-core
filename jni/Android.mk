LOCAL_PATH := $(call my-dir)

WAYLAND_APP_SRC := \
	src/jni_util.c \
	src/event_loop_queued_executor.c

include $(CLEAR_VARS)

LOCAL_MODULE 			:= wayland-app
LOCAL_SRC_FILES 		:= $(WAYLAND_APP_SRC)

include $(BUILD_SHARED_LIBRARY)

