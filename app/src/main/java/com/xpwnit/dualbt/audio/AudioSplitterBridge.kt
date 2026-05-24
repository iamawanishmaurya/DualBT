package com.xpwnit.dualbt.audio

import com.xpwnit.dualbt.logging.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioSplitterBridge @Inject constructor() {

    companion object {
        private var isLoaded = false
        fun loadNative() {
            if (!isLoaded) {
                try {
                    System.loadLibrary("dualbt")
                    isLoaded = true
                    AppLogger.i("AudioSplitterBridge", "Native library loaded")
                } catch (e: UnsatisfiedLinkError) {
                    AppLogger.w("AudioSplitterBridge", "Native library not available — using Kotlin splitter")
                }
            }
        }
    }

    val isNativeAvailable: Boolean get() = isLoaded

    fun init(sampleRate: Int, channelCount: Int, bufferSize: Int): Boolean {
        return if (isLoaded) {
            nativeInit(sampleRate, channelCount, bufferSize)
        } else {
            AppLogger.w("AudioSplitterBridge", "Native init skipped — library not loaded")
            false
        }
    }

    fun pushPcm(pcmData: ByteArray, numFrames: Int) {
        if (isLoaded) nativePushPcm(pcmData, numFrames)
    }

    fun start() {
        if (isLoaded) nativeStart()
    }

    fun stop() {
        if (isLoaded) nativeStop()
    }

    fun destroy() {
        if (isLoaded) nativeDestroy()
    }

    private external fun nativeInit(sampleRate: Int, channelCount: Int, bufferSize: Int): Boolean
    private external fun nativePushPcm(pcmData: ByteArray, numFrames: Int)
    private external fun nativeStart()
    private external fun nativeStop()
    private external fun nativeDestroy()
}
