#include <jni.h>
#include "AudioSplitter.h"
#include <android/log.h>

#define LOG_TAG "JNI_Bridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static AudioSplitter* gSplitter = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeInit(
    JNIEnv* env, jobject obj,
    jint sampleRate, jint channelCount, jint bufferSize
) {
    LOGI("nativeInit: rate=%d, channels=%d, bufSize=%d", sampleRate, channelCount, bufferSize);
    if (gSplitter) delete gSplitter;
    gSplitter = new AudioSplitter();
    return gSplitter->init(sampleRate, channelCount, bufferSize);
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativePushPcm(
    JNIEnv* env, jobject obj,
    jbyteArray pcmData, jint numFrames
) {
    if (!gSplitter) return;
    jbyte* data = env->GetByteArrayElements(pcmData, nullptr);
    gSplitter->pushPcm(reinterpret_cast<int16_t*>(data), numFrames);
    env->ReleaseByteArrayElements(pcmData, data, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeStart(
    JNIEnv* env, jobject obj
) {
    LOGI("nativeStart");
    if (gSplitter) gSplitter->start();
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeStop(
    JNIEnv* env, jobject obj
) {
    LOGI("nativeStop");
    if (gSplitter) gSplitter->stop();
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeDestroy(
    JNIEnv* env, jobject obj
) {
    LOGI("nativeDestroy");
    delete gSplitter;
    gSplitter = nullptr;
}

} // extern "C"
