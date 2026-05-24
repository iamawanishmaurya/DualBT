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
