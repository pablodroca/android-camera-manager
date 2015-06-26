LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE:= ndkmessage
LOCAL_SRC_FILES:= ndkmessage.c
include $(BUILD_SHARED_LIBRARY)