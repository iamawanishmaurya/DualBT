# DualBT — Full Implementation Plan
> Dual Bluetooth Audio Splitter for Android
> Give this entire document to Codex / any AI coding agent as context.

---

## Project Summary

Build an Android app **DualBT** that:
1. Captures system audio from any music app (Spotify, YouTube, etc.)
2. Splits the raw PCM stream in real-time
3. Simultaneously outputs identical audio to **two Bluetooth speakers**

**No root required. Pure app-level. Android 10+.**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin + C++ (NDK) |
| Audio engine | Oboe (C++ library via NDK) |
| Audio capture | MediaProjection + AudioPlaybackCapture API |
| BT routing | `AudioTrack.setPreferredDevice()` (public API) |
| UI | Jetpack Compose |
| DI | Hilt |
| State | StateFlow + ViewModel |
| Service | ForegroundService (MediaProjection) |
| Build | Gradle KTS |
| Min SDK | 29 (Android 10) |
| Target SDK | 34 (Android 14) |

---

## Architecture Diagram

```
┌─────────────────────────────────────┐
│        ANY MUSIC APP                │
│    (Spotify / YouTube / Local)      │
└────────────────┬────────────────────┘
                 │ system audio
                 ▼
┌─────────────────────────────────────┐
│     LAYER 1: CAPTURE ENGINE         │
│                                     │
│  MediaProjectionManager             │
│  → AudioPlaybackCaptureConfig       │
│  → AudioRecord (PCM 48kHz 16bit)    │
└────────────────┬────────────────────┘
                 │ raw PCM bytes
                 ▼
┌─────────────────────────────────────┐
│     LAYER 2: PCM SPLITTER (NDK)     │
│                                     │
│  Oboe C++ AudioStream               │
│  read(buffer) → copy → buf1 + buf2  │
│  Ring buffer per output             │
│  Drift correction via timestamps    │
└──────────┬──────────────┬───────────┘
           │ buf1         │ buf2
           ▼              ▼
┌──────────────┐  ┌──────────────────┐
│ AudioTrack 1 │  │  AudioTrack 2    │
│ .setPreferred│  │  .setPreferred   │
│ Device(bt1)  │  │  Device(bt2)     │
└──────┬───────┘  └──────┬───────────┘
       │ A2DP            │ A2DP
       ▼                 ▼
 [BT Speaker 1]    [BT Speaker 2]
```

---

## Folder Structure

```
app/src/main/
├── AndroidManifest.xml
├── cpp/
│   ├── CMakeLists.txt
│   ├── AudioSplitter.h
│   ├── AudioSplitter.cpp
│   └── jni_bridge.cpp
└── kotlin/com/xpwnit/dualbt/
    ├── DualBTApp.kt
    ├── MainActivity.kt
    ├── bt/
    │   ├── BTDevice.kt            ← data class
    │   ├── BTScanner.kt           ← scan + connect
    │   └── BTRepository.kt
    ├── audio/
    │   ├── CaptureEngine.kt       ← MediaProjection capture
    │   ├── AudioRouter.kt         ← AudioTrack per device
    │   └── AudioSplitterBridge.kt ← JNI calls
    ├── service/
    │   └── DualBTService.kt       ← ForegroundService
    ├── ui/
    │   ├── MainScreen.kt
    │   ├── DeviceCard.kt
    │   └── StatusBar.kt
    ├── vm/
    │   └── MainViewModel.kt
    └── di/
        └── AppModule.kt
```

---

# PHASE 1 — Project Scaffold

**Goal:** Runnable empty app, correct dependencies, NDK configured, Hilt working.

### 1.1 `build.gradle.kts` (app)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.xpwnit.dualbt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xpwnit.dualbt"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Oboe
    implementation("com.google.oboe:oboe:1.8.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

### 1.2 `build.gradle.kts` (project)

```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
}
```

### 1.3 `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Bluetooth permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

    <!-- Audio capture -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Foreground service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />

    <!-- Keep alive -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".DualBTApp"
        android:allowBackup="true"
        android:label="DualBT"
        android:theme="@style/Theme.DualBT">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.DualBTService"
            android:foregroundServiceType="mediaProjection"
            android:exported="false" />
    </application>
</manifest>
```

### 1.4 `CMakeLists.txt`

```cmake
cmake_minimum_required(VERSION 3.22.1)
project(dualbt)

# Find Oboe via prefab
find_package(oboe REQUIRED CONFIG)

add_library(
    dualbt
    SHARED
    AudioSplitter.cpp
    jni_bridge.cpp
)

target_include_directories(dualbt PRIVATE .)

target_link_libraries(
    dualbt
    oboe::oboe
    android
    log
)
```

### 1.5 `DualBTApp.kt`

```kotlin
package com.xpwnit.dualbt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DualBTApp : Application()
```

### Phase 1 Test
- App launches without crash
- Hilt injection works
- NDK compiles (check Build > Make Project — no CMake errors)

---

# PHASE 2 — Bluetooth Layer

**Goal:** Scan, list, and connect to Bluetooth audio devices. Mock BT for emulator.

### 2.1 `BTDevice.kt`

```kotlin
package com.xpwnit.dualbt.bt

import android.bluetooth.BluetoothDevice
import android.media.AudioDeviceInfo

data class BTDevice(
    val name: String,
    val address: String,
    val bluetoothDevice: BluetoothDevice? = null,  // null = mock
    val audioDeviceInfo: AudioDeviceInfo? = null,   // for routing
    val isMock: Boolean = false
) {
    companion object {
        // Mock devices for emulator testing
        val MOCK_DEVICE_1 = BTDevice(
            name = "Mock Speaker 1",
            address = "AA:BB:CC:DD:EE:01",
            isMock = true
        )
        val MOCK_DEVICE_2 = BTDevice(
            name = "Mock Speaker 2",
            address = "AA:BB:CC:DD:EE:02",
            isMock = true
        )
    }
}
```

### 2.2 `BTScanner.kt`

```kotlin
package com.xpwnit.dualbt.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BTScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val audioManager = context.getSystemService(AudioManager::class.java)

    private val _devices = MutableStateFlow<List<BTDevice>>(emptyList())
    val devices: StateFlow<List<BTDevice>> = _devices

    fun scanConnectedDevices() {
        val found = mutableListOf<BTDevice>()

        // Real BT devices
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val btTypes = setOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER
        )

        for (device in outputDevices) {
            if (device.type in btTypes) {
                val pairedDevice = bluetoothAdapter?.bondedDevices
                    ?.find { it.address == device.address }
                found.add(
                    BTDevice(
                        name = device.productName?.toString() ?: "BT Device",
                        address = device.address ?: "unknown",
                        bluetoothDevice = pairedDevice,
                        audioDeviceInfo = device
                    )
                )
            }
        }

        // Emulator fallback — inject mock devices if none found
        if (found.isEmpty()) {
            found.add(BTDevice.MOCK_DEVICE_1)
            found.add(BTDevice.MOCK_DEVICE_2)
        }

        _devices.value = found
    }
}
```

### 2.3 `BTRepository.kt`

```kotlin
package com.xpwnit.dualbt.bt

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BTRepository @Inject constructor(
    private val scanner: BTScanner
) {
    val devices: StateFlow<List<BTDevice>> = scanner.devices

    private var selectedDevices = mutableListOf<BTDevice>()

    fun refresh() = scanner.scanConnectedDevices()

    fun selectDevice(device: BTDevice) {
        if (selectedDevices.size < 2 && device !in selectedDevices) {
            selectedDevices.add(device)
        }
    }

    fun deselectDevice(device: BTDevice) {
        selectedDevices.remove(device)
    }

    fun getSelectedDevices(): List<BTDevice> = selectedDevices.toList()

    fun isSelected(device: BTDevice) = device in selectedDevices
}
```

### Phase 2 Test
- On real device: shows paired BT speakers
- On emulator: shows "Mock Speaker 1" and "Mock Speaker 2"
- Select/deselect works
- Max 2 devices selectable

---

# PHASE 3 — Audio Capture Engine

**Goal:** Capture system audio as raw PCM using MediaProjection API.

### 3.1 `CaptureEngine.kt`

```kotlin
package com.xpwnit.dualbt.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SAMPLE_RATE = 48000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val BUFFER_SIZE_FACTOR = 4
    }

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private var mediaProjection: MediaProjection? = null

    // Callback — sends PCM buffer to splitter
    var onPcmData: ((ByteArray) -> Unit)? = null

    val bufferSize: Int = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ) * BUFFER_SIZE_FACTOR

    fun startCapture(projection: MediaProjection) {
        mediaProjection = projection

        val captureConfig = AudioPlaybackCaptureConfiguration.Builder(projection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(AUDIO_FORMAT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(CHANNEL_CONFIG)
            .build()

        audioRecord = AudioRecord.Builder()
            .setAudioPlaybackCaptureConfig(captureConfig)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioRecord?.startRecording()

        captureJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)
            while (isActive) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (bytesRead > 0) {
                    onPcmData?.invoke(buffer.copyOf(bytesRead))
                }
            }
        }
    }

    fun stopCapture() {
        captureJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    // EMULATOR MOCK — generates sine wave PCM instead of real capture
    fun startMockCapture() {
        captureJob = CoroutineScope(Dispatchers.IO).launch {
            val frequency = 440.0 // A4 note
            val buffer = ByteArray(bufferSize)
            var phase = 0.0
            val phaseIncrement = 2.0 * Math.PI * frequency / SAMPLE_RATE

            while (isActive) {
                for (i in buffer.indices step 4) {
                    val sample = (Short.MAX_VALUE * 0.5 * Math.sin(phase)).toInt().toShort()
                    // Stereo — left channel
                    buffer[i] = (sample.toInt() and 0xFF).toByte()
                    buffer[i + 1] = (sample.toInt() shr 8).toByte()
                    // Stereo — right channel
                    buffer[i + 2] = buffer[i]
                    buffer[i + 3] = buffer[i + 1]
                    phase += phaseIncrement
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                }
                onPcmData?.invoke(buffer.copyOf())
                Thread.sleep(10) // ~100 chunks/sec
            }
        }
    }
}
```

### Phase 3 Test
- On emulator: `startMockCapture()` generates audible 440Hz sine wave
- `onPcmData` callback fires continuously with non-zero byte arrays
- Unit test: verify buffer size > 0, verify callback frequency

---

# PHASE 4 — NDK Audio Splitter (C++)

**Goal:** High-performance PCM splitter in C++ via Oboe. Zero GC pauses.

### 4.1 `AudioSplitter.h`

```cpp
#pragma once

#include <oboe/Oboe.h>
#include <vector>
#include <mutex>
#include <queue>
#include <atomic>

class AudioSplitter {
public:
    AudioSplitter();
    ~AudioSplitter();

    bool init(int sampleRate, int channelCount, int bufferSize);
    void pushPcm(const int16_t* data, int32_t numFrames);
    void start();
    void stop();
    void setDriftCorrectionEnabled(bool enabled);

private:
    static constexpr int NUM_OUTPUTS = 2;
    static constexpr int RING_BUFFER_FRAMES = 4096 * 4;
    static constexpr int DRIFT_THRESHOLD_FRAMES = 100;

    int mSampleRate = 48000;
    int mChannelCount = 2;
    int mBufferSize = 4096;
    bool mDriftCorrectionEnabled = true;

    // Ring buffer per output
    std::vector<int16_t> mRingBuffer[NUM_OUTPUTS];
    std::atomic<int32_t> mWritePos{0};
    std::atomic<int32_t> mReadPos[NUM_OUTPUTS];

    // Output streams (Oboe)
    std::shared_ptr<oboe::AudioStream> mOutputStream[NUM_OUTPUTS];

    std::mutex mMutex;
    std::atomic<bool> mRunning{false};

    void correctDrift();
    int32_t getBufferedFrames(int outputIndex);
};
```

### 4.2 `AudioSplitter.cpp`

```cpp
#include "AudioSplitter.h"
#include <android/log.h>
#include <cstring>
#include <cmath>

#define LOG_TAG "AudioSplitter"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

AudioSplitter::AudioSplitter() {
    for (int i = 0; i < NUM_OUTPUTS; i++) {
        mReadPos[i] = 0;
        mRingBuffer[i].resize(RING_BUFFER_FRAMES * 2, 0); // *2 for stereo
    }
}

AudioSplitter::~AudioSplitter() {
    stop();
}

bool AudioSplitter::init(int sampleRate, int channelCount, int bufferSize) {
    mSampleRate = sampleRate;
    mChannelCount = channelCount;
    mBufferSize = bufferSize;

    for (int i = 0; i < NUM_OUTPUTS; i++) {
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Output)
               ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
               ->setSharingMode(oboe::SharingMode::Exclusive)
               ->setFormat(oboe::AudioFormat::I16)
               ->setChannelCount(channelCount)
               ->setSampleRate(sampleRate)
               ->setFramesPerDataCallback(bufferSize / (channelCount * sizeof(int16_t)));

        oboe::Result result = builder.openStream(mOutputStream[i]);
        if (result != oboe::Result::OK) {
            LOGE("Failed to open stream %d: %s", i, oboe::convertToText(result));
            return false;
        }
        LOGI("Opened output stream %d", i);
    }
    return true;
}

void AudioSplitter::pushPcm(const int16_t* data, int32_t numFrames) {
    std::lock_guard<std::mutex> lock(mMutex);

    // Write to both ring buffers
    int32_t framesToWrite = numFrames * mChannelCount;
    int32_t writePos = mWritePos.load() % (RING_BUFFER_FRAMES * mChannelCount);

    for (int i = 0; i < NUM_OUTPUTS; i++) {
        int32_t pos = writePos;
        for (int32_t j = 0; j < framesToWrite; j++) {
            mRingBuffer[i][pos % (RING_BUFFER_FRAMES * mChannelCount)] = data[j];
            pos++;
        }
    }

    mWritePos += framesToWrite;

    // Write to Oboe streams
    for (int i = 0; i < NUM_OUTPUTS; i++) {
        if (mOutputStream[i] && mRunning) {
            auto result = mOutputStream[i]->write(data, numFrames, 0);
            if (result.error() != oboe::Result::OK) {
                LOGE("Stream %d write error: %s", i, oboe::convertToText(result.error()));
            }
        }
    }

    if (mDriftCorrectionEnabled) {
        correctDrift();
    }
}

void AudioSplitter::correctDrift() {
    // Check drift between two outputs via frame counts
    int32_t frames0 = getBufferedFrames(0);
    int32_t frames1 = getBufferedFrames(1);
    int32_t drift = std::abs(frames0 - frames1);

    if (drift > DRIFT_THRESHOLD_FRAMES) {
        LOGI("Drift detected: %d frames — correcting", drift);
        // Fast output: insert silence frames to slow it down
        int fasterIdx = (frames0 > frames1) ? 0 : 1;
        std::vector<int16_t> silence(DRIFT_THRESHOLD_FRAMES * mChannelCount, 0);
        if (mOutputStream[fasterIdx] && mRunning) {
            mOutputStream[fasterIdx]->write(
                silence.data(), DRIFT_THRESHOLD_FRAMES, 0
            );
        }
    }
}

int32_t AudioSplitter::getBufferedFrames(int outputIndex) {
    if (!mOutputStream[outputIndex]) return 0;
    return mOutputStream[outputIndex]->getXRunCount();
}

void AudioSplitter::start() {
    mRunning = true;
    for (int i = 0; i < NUM_OUTPUTS; i++) {
        if (mOutputStream[i]) {
            auto result = mOutputStream[i]->requestStart();
            if (result != oboe::Result::OK) {
                LOGE("Failed to start stream %d", i);
            }
        }
    }
    LOGI("AudioSplitter started");
}

void AudioSplitter::stop() {
    mRunning = false;
    for (int i = 0; i < NUM_OUTPUTS; i++) {
        if (mOutputStream[i]) {
            mOutputStream[i]->requestStop();
            mOutputStream[i]->close();
            mOutputStream[i].reset();
        }
    }
    LOGI("AudioSplitter stopped");
}
```

### 4.3 `jni_bridge.cpp`

```cpp
#include <jni.h>
#include "AudioSplitter.h"

static AudioSplitter* gSplitter = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeInit(
    JNIEnv* env, jobject obj,
    jint sampleRate, jint channelCount, jint bufferSize
) {
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
    if (gSplitter) gSplitter->start();
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeStop(
    JNIEnv* env, jobject obj
) {
    if (gSplitter) gSplitter->stop();
}

JNIEXPORT void JNICALL
Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeDestroy(
    JNIEnv* env, jobject obj
) {
    delete gSplitter;
    gSplitter = nullptr;
}

} // extern "C"
```

### 4.4 `AudioSplitterBridge.kt`

```kotlin
package com.xpwnit.dualbt.audio

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioSplitterBridge @Inject constructor() {

    companion object {
        init { System.loadLibrary("dualbt") }
    }

    fun init(sampleRate: Int, channelCount: Int, bufferSize: Int): Boolean =
        nativeInit(sampleRate, channelCount, bufferSize)

    fun pushPcm(pcmData: ByteArray, numFrames: Int) =
        nativePushPcm(pcmData, numFrames)

    fun start() = nativeStart()
    fun stop() = nativeStop()
    fun destroy() = nativeDestroy()

    private external fun nativeInit(sampleRate: Int, channelCount: Int, bufferSize: Int): Boolean
    private external fun nativePushPcm(pcmData: ByteArray, numFrames: Int)
    private external fun nativeStart()
    private external fun nativeStop()
    private external fun nativeDestroy()
}
```

### Phase 4 Test
- NDK builds without errors
- `nativeInit()` returns true
- `nativePushPcm()` doesn't crash with test buffer
- Logcat shows "AudioSplitter started"

---

# PHASE 5 — Audio Router (BT Device Routing)

**Goal:** Route each AudioTrack to a specific Bluetooth audio device.

### 5.1 `AudioRouter.kt`

```kotlin
package com.xpwnit.dualbt.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioDeviceInfo
import com.xpwnit.dualbt.bt.BTDevice
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

class AudioRouter @Inject constructor(
    private val audioManager: AudioManager
) {
    companion object {
        const val SAMPLE_RATE = 48000
        const val CHANNEL_COUNT = 2
    }

    private val tracks = mutableListOf<AudioTrack>()

    fun setupTracks(devices: List<BTDevice>): Boolean {
        releaseTracks()

        for (device in devices.take(2)) {
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ) * 4

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // KEY: route to specific BT device
            device.audioDeviceInfo?.let { info ->
                val success = track.setPreferredDevice(info)
                if (!success) {
                    // Fallback: find device by type
                    val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    val btDevice = outputDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    }
                    btDevice?.let { track.setPreferredDevice(it) }
                }
            }
            // Mock device: no routing needed (uses default output)

            track.play()
            tracks.add(track)
        }

        return tracks.size == devices.size
    }

    fun writeToAll(pcmData: ByteArray) {
        for (track in tracks) {
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(pcmData, 0, pcmData.size)
            }
        }
    }

    fun releaseTracks() {
        for (track in tracks) {
            track.stop()
            track.release()
        }
        tracks.clear()
    }

    fun getRoutedDeviceNames(): List<String> =
        tracks.map { it.routedDevice?.productName?.toString() ?: "Default" }
}
```

### Phase 5 Test
- Two AudioTrack instances created
- `writeToAll()` with sine wave data produces audible output on emulator speaker
- `getRoutedDeviceNames()` returns non-empty list
- No `AudioTrack.ERROR_INVALID_OPERATION` in logcat

---

# PHASE 6 — Foreground Service

**Goal:** Keep audio running when app is backgrounded. Hold MediaProjection token.

### 6.1 `DualBTService.kt`

```kotlin
package com.xpwnit.dualbt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.xpwnit.dualbt.MainActivity
import com.xpwnit.dualbt.R
import com.xpwnit.dualbt.audio.AudioRouter
import com.xpwnit.dualbt.audio.AudioSplitterBridge
import com.xpwnit.dualbt.audio.CaptureEngine
import com.xpwnit.dualbt.bt.BTDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DualBTService : Service() {

    companion object {
        const val CHANNEL_ID = "dualbt_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.xpwnit.dualbt.STOP"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
    }

    @Inject lateinit var captureEngine: CaptureEngine
    @Inject lateinit var audioRouter: AudioRouter
    @Inject lateinit var splitterBridge: AudioSplitterBridge

    inner class LocalBinder : Binder() {
        fun getService() = this@DualBTService
    }

    private val binder = LocalBinder()
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != -1 && resultData != null) {
            val projectionManager = getSystemService(MediaProjectionManager::class.java)
            val projection = projectionManager.getMediaProjection(resultCode, resultData)
            startAudioCapture(projection)
        } else {
            // Emulator mode — mock capture
            startMockCapture()
        }

        return START_STICKY
    }

    fun startWithDevices(devices: List<BTDevice>, projection: MediaProjection? = null) {
        if (isRunning) return

        splitterBridge.init(
            CaptureEngine.SAMPLE_RATE,
            2,
            captureEngine.bufferSize
        )
        splitterBridge.start()
        audioRouter.setupTracks(devices)

        if (projection != null) {
            captureEngine.onPcmData = { pcm ->
                val frames = pcm.size / (2 * 2) // 16bit stereo
                splitterBridge.pushPcm(pcm, frames)
                audioRouter.writeToAll(pcm)
            }
            captureEngine.startCapture(projection)
        } else {
            captureEngine.onPcmData = { pcm ->
                audioRouter.writeToAll(pcm)
            }
            captureEngine.startMockCapture()
        }

        isRunning = true
    }

    private fun startAudioCapture(projection: MediaProjection) {
        captureEngine.onPcmData = { pcm ->
            audioRouter.writeToAll(pcm)
        }
        captureEngine.startCapture(projection)
    }

    private fun startMockCapture() {
        captureEngine.onPcmData = { pcm ->
            audioRouter.writeToAll(pcm)
        }
        captureEngine.startMockCapture()
    }

    override fun onDestroy() {
        captureEngine.stopCapture()
        audioRouter.releaseTracks()
        splitterBridge.destroy()
        isRunning = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DualBT Audio",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "DualBT audio streaming service" }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, DualBTService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DualBT Active")
            .setContentText("Streaming to 2 speakers")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setOngoing(true)
            .build()
    }
}
```

### Phase 6 Test
- Service starts, notification appears
- Service survives app backgrounding
- Stop button in notification kills service cleanly
- No memory leaks (check Android Studio Profiler)

---

# PHASE 7 — ViewModel + State

**Goal:** Connect all layers. Manage permissions, projection, device selection.

### 7.1 `MainViewModel.kt`

```kotlin
package com.xpwnit.dualbt.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpwnit.dualbt.bt.BTDevice
import com.xpwnit.dualbt.bt.BTRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val devices: List<BTDevice> = emptyList(),
    val selectedDevices: List<BTDevice> = emptyList(),
    val isStreaming: Boolean = false,
    val isEmulatorMode: Boolean = false,
    val statusMessage: String = "Select 2 speakers",
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val btRepository: BTRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _isStreaming = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            btRepository.devices.collect { devices ->
                val isEmulator = devices.any { it.isMock }
                _uiState.value = _uiState.value.copy(
                    devices = devices,
                    isEmulatorMode = isEmulator,
                    statusMessage = if (isEmulator)
                        "Emulator mode — mock devices loaded"
                    else
                        "Select up to 2 speakers"
                )
            }
        }
        refresh()
    }

    fun refresh() = btRepository.refresh()

    fun toggleDevice(device: BTDevice) {
        val selected = btRepository.getSelectedDevices().toMutableList()
        if (btRepository.isSelected(device)) {
            btRepository.deselectDevice(device)
        } else if (selected.size < 2) {
            btRepository.selectDevice(device)
        }
        _uiState.value = _uiState.value.copy(
            selectedDevices = btRepository.getSelectedDevices(),
            statusMessage = when (btRepository.getSelectedDevices().size) {
                0 -> "Select 2 speakers"
                1 -> "Select 1 more speaker"
                2 -> "Ready to stream"
                else -> ""
            }
        )
    }

    fun startStreaming(projectionResult: ActivityResult? = null) {
        val devices = btRepository.getSelectedDevices()
        if (devices.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Select at least 1 device"
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            statusMessage = "Streaming to ${devices.size} speaker(s)"
        )
    }

    fun stopStreaming() {
        _uiState.value = _uiState.value.copy(
            isStreaming = false,
            statusMessage = "Stopped"
        )
    }

    fun getSelectedDevices() = btRepository.getSelectedDevices()
    fun isSelected(device: BTDevice) = btRepository.isSelected(device)
}
```

### 7.2 `AppModule.kt`

```kotlin
package com.xpwnit.dualbt.di

import android.content.Context
import android.media.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager =
        context.getSystemService(AudioManager::class.java)
}
```

---

# PHASE 8 — UI (Jetpack Compose)

**Goal:** Clean UI to scan devices, select 2, start/stop streaming. Shows emulator mode clearly.

### 8.1 `MainScreen.kt`

```kotlin
package com.xpwnit.dualbt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xpwnit.dualbt.vm.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestProjection: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "DualBT",
            style = MaterialTheme.typography.headlineLarge
        )

        // Emulator mode badge
        if (state.isEmulatorMode) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "⚡ Emulator Mode — Mock Devices",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status
        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Error
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device list
        Text("Available Devices", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.devices) { device ->
                DeviceCard(
                    device = device,
                    isSelected = viewModel.isSelected(device),
                    onToggle = { viewModel.toggleDevice(device) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Scan")
            }

            Button(
                onClick = {
                    if (state.isStreaming) {
                        viewModel.stopStreaming()
                    } else {
                        if (state.isEmulatorMode) {
                            viewModel.startStreaming()
                        } else {
                            onRequestProjection()
                        }
                    }
                },
                enabled = state.selectedDevices.isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isStreaming)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (state.isStreaming) "Stop" else "Start")
            }
        }
    }
}
```

### 8.2 `DeviceCard.kt`

```kotlin
package com.xpwnit.dualbt.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xpwnit.dualbt.bt.BTDevice

@Composable
fun DeviceCard(
    device: BTDevice,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onToggle() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (device.isMock) "Mock Device" else device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

### 8.3 `MainActivity.kt`

```kotlin
package com.xpwnit.dualbt

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.xpwnit.dualbt.service.DualBTService
import com.xpwnit.dualbt.ui.MainScreen
import com.xpwnit.dualbt.ui.theme.DualBTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val projectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent(this, DualBTService::class.java).apply {
                putExtra(DualBTService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(DualBTService.EXTRA_RESULT_DATA, result.data)
            }
            startForegroundService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DualBTTheme {
                MainScreen(
                    onRequestProjection = {
                        projectionLauncher.launch(
                            projectionManager.createScreenCaptureIntent()
                        )
                    }
                )
            }
        }
    }
}
```

---

# PHASE 9 — Emulator Testing Strategy

**Goal:** Full test coverage runnable on Android emulator without real BT hardware.

## 9.1 Emulator Setup

```
AVD: Pixel 7 Pro
API: 34 (Android 14)
RAM: 4GB
ABI: x86_64
```

## 9.2 Mock Strategy

| Real Feature | Emulator Mock |
|---|---|
| BT Speaker 1 | Mock Device (left AudioTrack → default output) |
| BT Speaker 2 | Mock Device (right AudioTrack → default output) |
| Music app audio | 440Hz sine wave from `CaptureEngine.startMockCapture()` |
| MediaProjection prompt | Skipped — mock path taken automatically |

## 9.3 Unit Tests

### `CaptureEngineTest.kt`
```kotlin
@Test
fun `mock capture produces non-zero PCM data`() = runTest {
    val engine = CaptureEngine(mockContext)
    var received = false
    engine.onPcmData = { buf ->
        assert(buf.isNotEmpty())
        assert(buf.any { it != 0.toByte() })
        received = true
    }
    engine.startMockCapture()
    delay(100)
    engine.stopCapture()
    assert(received)
}
```

### `BTRepositoryTest.kt`
```kotlin
@Test
fun `emulator mode returns mock devices`() {
    val scanner = BTScanner(mockContext)
    scanner.scanConnectedDevices()
    val devices = scanner.devices.value
    assert(devices.isNotEmpty())
    assert(devices.any { it.isMock })
}

@Test
fun `max 2 devices selectable`() {
    val repo = BTRepository(scanner)
    repo.selectDevice(BTDevice.MOCK_DEVICE_1)
    repo.selectDevice(BTDevice.MOCK_DEVICE_2)
    repo.selectDevice(BTDevice(name="3rd", address="xx")) // should be ignored
    assert(repo.getSelectedDevices().size == 2)
}
```

### `AudioRouterTest.kt`
```kotlin
@Test
fun `tracks created for mock devices`() {
    val router = AudioRouter(mockAudioManager)
    val result = router.setupTracks(listOf(
        BTDevice.MOCK_DEVICE_1,
        BTDevice.MOCK_DEVICE_2
    ))
    assert(result)
    router.releaseTracks()
}
```

## 9.4 Manual Test Checklist (Emulator)

```
[ ] App launches, shows "Emulator Mode" badge
[ ] Scan shows Mock Speaker 1 + Mock Speaker 2
[ ] Tap Mock Speaker 1 → card highlights + checkmark
[ ] Tap Mock Speaker 2 → both selected, "Ready to stream"
[ ] Tap 3rd device → ignored (max 2)
[ ] Tap Start → service starts, notification appears
[ ] Sine wave audible from emulator speaker
[ ] Tap Stop → audio stops, notification dismissed
[ ] Background app → audio continues
[ ] Notification Stop button → service stops cleanly
[ ] Rotate screen → state preserved
```

---

# PHASE 10 — Real Device Verification

**Goal:** Test on real Android device with actual BT speakers.

## 10.1 Real Device Test Checklist

```
[ ] Pair 2 BT speakers via Android Settings
[ ] Open DualBT → both speakers appear in list
[ ] Select both → Start
[ ] MediaProjection permission prompt appears
[ ] Allow → service starts
[ ] Play Spotify / YouTube → audio from BOTH speakers
[ ] Volume sliders work on both independently
[ ] Background app → audio continues
[ ] BT speaker disconnected → graceful error shown
[ ] Reconnect speaker → auto-reconnect attempted
```

## 10.2 ADB Debugging Commands

```bash
# Watch audio routing logs
adb logcat -s AudioSplitter AudioRouter CaptureEngine

# Check BT A2DP state
adb shell dumpsys bluetooth_manager | grep -i a2dp

# Check AudioTrack routing
adb shell dumpsys audio | grep -i preferred

# Check active AudioTracks
adb shell dumpsys media.audio_flinger

# Force BT A2DP connect
adb shell am broadcast -a android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED
```

---

# KNOWN ISSUES & SOLUTIONS

| Issue | Cause | Fix |
|---|---|---|
| Audio cuts out briefly | GC pause on JVM thread | Move PCM loop to NDK (Oboe) |
| Two speakers out of sync | Different BT buffer sizes | Drift correction in `AudioSplitter.cpp` |
| `setPreferredDevice` ignored | AudioPolicy override | Use `AudioManager.startBluetoothSco()` as fallback |
| MediaProjection crash on emulator | No real projection | Auto-detect emulator → mock path |
| App capture blocked | Target app sets `ALLOW_CAPTURE_BY_NONE` | Cannot bypass — affects only DRM apps |
| Service killed by OS | Battery optimization | Request user to disable battery opt for app |

---

# WHAT CODEX SHOULD IMPLEMENT FIRST

Start in this exact order:

1. `Phase 1` — scaffold only, verify it compiles
2. `Phase 2` — BT layer + mock devices, verify on emulator
3. `Phase 3` — mock capture sine wave, verify audible
4. `Phase 5` — AudioRouter with mock devices, verify audio output
5. `Phase 6` — ForegroundService wiring everything
6. `Phase 8` — UI
7. `Phase 4` — NDK splitter (optional — pure Kotlin version first)
8. `Phase 9` — tests
9. `Phase 10` — real device

> Skip Phase 4 (NDK) initially. Implement a pure Kotlin PCM splitter first,
> then swap in Oboe C++ once Kotlin version is confirmed working.

---

## Kotlin-only PCM Splitter (Phase 4 alternative — start here)

```kotlin
// AudioSplitterKotlin.kt — replace NDK version initially
class AudioSplitterKotlin {
    fun splitAndWrite(pcm: ByteArray, track1: AudioTrack, track2: AudioTrack) {
        track1.write(pcm, 0, pcm.size)
        track2.write(pcm, 0, pcm.size)
    }
}
```

Replace with NDK version once end-to-end works.

---

*Plan version: 1.0 | Target: Android 10-14 | No root required*
