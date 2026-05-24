package com.xpwnit.dualbt.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import com.xpwnit.dualbt.logging.AppLogger
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

    var onPcmData: ((ByteArray) -> Unit)? = null

    val bufferSize: Int = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ) * BUFFER_SIZE_FACTOR

    fun startCapture(projection: MediaProjection) {
        AppLogger.i("CaptureEngine", "Starting audio capture with MediaProjection")
        mediaProjection = projection

        try {
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
            AppLogger.i("CaptureEngine", "AudioRecord started — buffer size: $bufferSize")

            captureJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ByteArray(bufferSize)
                while (isActive) {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (bytesRead > 0) {
                        onPcmData?.invoke(buffer.copyOf(bytesRead))
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e("CaptureEngine", "Failed to start capture", e)
        }
    }

    fun stopCapture() {
        AppLogger.i("CaptureEngine", "Stopping audio capture")
        captureJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        mediaProjection?.stop()
        mediaProjection = null
        AppLogger.i("CaptureEngine", "Audio capture stopped")
    }

    fun startMockCapture() {
        AppLogger.i("CaptureEngine", "Starting MOCK capture (440Hz sine wave)")
        captureJob = CoroutineScope(Dispatchers.IO).launch {
            val frequency = 440.0
            val buffer = ByteArray(bufferSize)
            var phase = 0.0
            val phaseIncrement = 2.0 * Math.PI * frequency / SAMPLE_RATE

            while (isActive) {
                for (i in buffer.indices step 4) {
                    if (i + 3 >= buffer.size) break
                    val sample = (Short.MAX_VALUE * 0.3 * Math.sin(phase)).toInt().toShort()
                    buffer[i] = (sample.toInt() and 0xFF).toByte()
                    buffer[i + 1] = (sample.toInt() shr 8).toByte()
                    buffer[i + 2] = buffer[i]
                    buffer[i + 3] = buffer[i + 1]
                    phase += phaseIncrement
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                }
                onPcmData?.invoke(buffer.copyOf())
                Thread.sleep(10)
            }
        }
    }
}
