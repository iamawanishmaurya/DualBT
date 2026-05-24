package com.xpwnit.dualbt.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioDeviceInfo
import com.xpwnit.dualbt.bt.BTDevice
import com.xpwnit.dualbt.logging.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRouter @Inject constructor(
    private val audioManager: AudioManager
) {
    companion object {
        const val SAMPLE_RATE = 48000
        const val CHANNEL_COUNT = 2
    }

    private val tracks = mutableListOf<AudioTrack>()

    fun setupTracks(devices: List<BTDevice>): Boolean {
        AppLogger.i("AudioRouter", "Setting up ${devices.size} audio track(s)")
        releaseTracks()

        for ((index, device) in devices.take(2).withIndex()) {
            try {
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

                // Route to specific BT device
                device.audioDeviceInfo?.let { info ->
                    val success = track.setPreferredDevice(info)
                    AppLogger.i("AudioRouter", "Track $index routed to ${device.name}: $success")
                    if (!success) {
                        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                        val btDevice = outputDevices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        }
                        btDevice?.let {
                            track.setPreferredDevice(it)
                            AppLogger.i("AudioRouter", "Track $index fallback routed to A2DP")
                        }
                    }
                } ?: run {
                    AppLogger.i("AudioRouter", "Track $index using default output (mock device: ${device.name})")
                }

                track.play()
                tracks.add(track)
                AppLogger.i("AudioRouter", "Track $index started for ${device.name}")
            } catch (e: Exception) {
                AppLogger.e("AudioRouter", "Failed to create track $index for ${device.name}", e)
            }
        }

        AppLogger.i("AudioRouter", "Setup complete: ${tracks.size}/${devices.size} tracks active")
        return tracks.size == devices.take(2).size
    }

    fun getTracks(): List<AudioTrack> = tracks.toList()

    fun writeToAll(pcmData: ByteArray) {
        for (track in tracks) {
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(pcmData, 0, pcmData.size)
            }
        }
    }

    fun releaseTracks() {
        for (track in tracks) {
            try {
                track.stop()
                track.release()
            } catch (e: Exception) {
                AppLogger.e("AudioRouter", "Error releasing track", e)
            }
        }
        tracks.clear()
        AppLogger.i("AudioRouter", "All tracks released")
    }

    fun getRoutedDeviceNames(): List<String> =
        tracks.map { it.routedDevice?.productName?.toString() ?: "Default" }
}
