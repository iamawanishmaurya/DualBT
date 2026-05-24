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
        mRingBuffer[i].resize(RING_BUFFER_FRAMES * 2, 0);
    }
}

AudioSplitter::~AudioSplitter() {
    stop();
}

bool AudioSplitter::init(int sampleRate, int channelCount, int bufferSize) {
    mSampleRate = sampleRate;
    mChannelCount = channelCount;
    mBufferSize = bufferSize;

    LOGI("Initializing AudioSplitter: rate=%d, channels=%d, bufSize=%d",
         sampleRate, channelCount, bufferSize);

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
    int32_t frames0 = getBufferedFrames(0);
    int32_t frames1 = getBufferedFrames(1);
    int32_t drift = std::abs(frames0 - frames1);

    if (drift > DRIFT_THRESHOLD_FRAMES) {
        LOGI("Drift detected: %d frames — correcting", drift);
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

void AudioSplitter::setDriftCorrectionEnabled(bool enabled) {
    mDriftCorrectionEnabled = enabled;
    LOGI("Drift correction %s", enabled ? "enabled" : "disabled");
}
