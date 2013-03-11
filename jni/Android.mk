LOCAL_PATH := $(call my-dir)

WAYLAND_APP_SRC := \
	src/jni_util.c \
	src/posix.c

include $(CLEAR_VARS)

LOCAL_MODULE 			:= wheatley-lib
LOCAL_SRC_FILES 		:= $(WAYLAND_APP_SRC)
LOCAL_STATIC_LIBRARIES := android_native_app_glue

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/native_app_glue)

