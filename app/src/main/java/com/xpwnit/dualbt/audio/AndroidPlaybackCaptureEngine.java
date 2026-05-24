package com.xpwnit.dualbt.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Process;
import android.os.Handler;
import android.os.Looper;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamRoutePlan;

public final class AndroidPlaybackCaptureEngine {
    private final AudioCaptureSpec spec = AudioCaptureSpec.defaultPlaybackSpec();
    private final PcmSplitter splitter = new PcmSplitter();
    private final AndroidAudioOutputRouter outputRouter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AudioRecord audioRecord;
    private MediaProjection mediaProjection;
    private MediaProjection.Callback projectionCallback;
    private Thread captureThread;
    private volatile boolean running;
    private volatile float outputGain = 1.0f;
    private long chunksRead;

    public AndroidPlaybackCaptureEngine(Context context) {
        outputRouter = new AndroidAudioOutputRouter(context);
    }

    public synchronized boolean start(MediaProjection projection, StreamRoutePlan routePlan) {
        if (projection == null) {
            AppLogger.w("CaptureEngine", "Cannot start capture without MediaProjection");
            return false;
        }
        if (routePlan == null || routePlan.targetCount() != 2) {
            AppLogger.w("CaptureEngine", "Cannot start capture without exactly 2 route targets");
            return false;
        }
        stopInternal(true);
        try {
            int minBufferBytes = AudioRecord.getMinBufferSize(
                    spec.sampleRate(),
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            if (minBufferBytes <= 0) {
                AppLogger.w("CaptureEngine", "AudioRecord minimum buffer unavailable: " + minBufferBytes);
                return false;
            }
            int bufferBytes = spec.bufferSizeBytes(minBufferBytes);
            AudioPlaybackCaptureConfiguration captureConfiguration = captureConfiguration(projection);
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(spec.sampleRate())
                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                    .build();
            AudioRecord record = new AudioRecord.Builder()
                    .setAudioPlaybackCaptureConfig(captureConfiguration)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferBytes)
                    .build();
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                record.release();
                AppLogger.w("CaptureEngine", "AudioRecord was not initialized");
                return false;
            }
            if (!outputRouter.start(spec, routePlan)) {
                record.release();
                AppLogger.w("CaptureEngine", "Output router could not start");
                return false;
            }
            audioRecord = record;
            mediaProjection = projection;
            projectionCallback = new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    AppLogger.w("CaptureEngine", "MediaProjection stopped externally");
                    stopInternal(false);
                }
            };
            mediaProjection.registerCallback(projectionCallback, mainHandler);
            audioRecord.startRecording();
            running = true;
            chunksRead = 0L;
            captureThread = new Thread(() -> readLoop(bufferBytes, routePlan), "DualBT-Capture");
            captureThread.start();
            AppLogger.i("CaptureEngine", "Audio playback capture started: buffer=" + bufferBytes + ", routes=" + routePlan.displayNames());
            return true;
        } catch (RuntimeException exception) {
            AppLogger.e("CaptureEngine", "Failed to start audio playback capture", exception);
            stopInternal(true);
            return false;
        }
    }

    public synchronized void stop() {
        stopInternal(true);
    }

    public boolean isRunning() {
        return running;
    }

    public void setOutputGain(float gain) {
        outputGain = clampGain(gain);
        AppLogger.i("CaptureEngine", "Output gain set to " + Math.round(outputGain * 100.0f) + "%");
    }

    private AudioPlaybackCaptureConfiguration captureConfiguration(MediaProjection projection) {
        AudioPlaybackCaptureConfiguration.Builder builder =
                new AudioPlaybackCaptureConfiguration.Builder(projection);
        for (String usage : spec.matchingUsages()) {
            builder.addMatchingUsage(toAudioUsage(usage));
        }
        builder.excludeUid(Process.myUid());
        return builder.build();
    }

    private int toAudioUsage(String usage) {
        if ("game".equals(usage)) {
            return AudioAttributes.USAGE_GAME;
        }
        if ("unknown".equals(usage)) {
            return AudioAttributes.USAGE_UNKNOWN;
        }
        return AudioAttributes.USAGE_MEDIA;
    }

    private void readLoop(int bufferBytes, StreamRoutePlan routePlan) {
        byte[] sharedBuffer = new byte[bufferBytes];
        byte[] firstOutput = new byte[bufferBytes];
        byte[] secondOutput = new byte[bufferBytes];
        while (running) {
            AudioRecord currentRecord = audioRecord;
            if (currentRecord == null) {
                break;
            }
            int bytesRead = currentRecord.read(sharedBuffer, 0, sharedBuffer.length);
            if (bytesRead > 0) {
                splitter.copyToOutputs(sharedBuffer, bytesRead, firstOutput, secondOutput);
                float gain = outputGain;
                PcmGain.applyInPlace(firstOutput, bytesRead, gain);
                PcmGain.applyInPlace(secondOutput, bytesRead, gain);
                outputRouter.write(firstOutput, secondOutput, bytesRead);
                chunksRead++;
                if (chunksRead == 1 || chunksRead % 500 == 0) {
                    AppLogger.d("CaptureEngine", "Captured PCM chunks=" + chunksRead + ", bytes=" + bytesRead + ", routes=" + routePlan.displayNames());
                }
            } else if (bytesRead < 0) {
                AppLogger.w("CaptureEngine", "AudioRecord read returned " + bytesRead);
            }
        }
    }

    private float clampGain(float gain) {
        if (Float.isNaN(gain) || Float.isInfinite(gain)) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(2.0f, gain));
    }

    private synchronized void stopInternal(boolean stopProjection) {
        running = false;
        Thread thread = captureThread;
        captureThread = null;
        AudioRecord record = audioRecord;
        audioRecord = null;
        if (record != null) {
            try {
                record.stop();
            } catch (RuntimeException ignored) {
                // AudioRecord may already be stopped or uninitialized.
            }
            record.release();
        }
        outputRouter.stop();
        if (thread != null && thread != Thread.currentThread()) {
            try {
                thread.join(500L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        MediaProjection projection = mediaProjection;
        MediaProjection.Callback callback = projectionCallback;
        mediaProjection = null;
        projectionCallback = null;
        if (projection != null && callback != null) {
            try {
                projection.unregisterCallback(callback);
            } catch (RuntimeException ignored) {
                // Projection may already be stopped.
            }
        }
        if (projection != null && stopProjection) {
            projection.stop();
        }
        if (record != null || projection != null) {
            AppLogger.i("CaptureEngine", "Audio playback capture stopped");
        }
    }
}
