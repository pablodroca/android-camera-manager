#include <jni.h>
#include <string.h>
#include <ar_fi_uba_MainActivity.h>


jstring JNICALL Java_ar_fi_uba_MainActivity_getMessage(
        JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello from JNI");
}
