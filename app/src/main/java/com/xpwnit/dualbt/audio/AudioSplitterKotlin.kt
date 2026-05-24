package com.xpwnit.dualbt.audio

import android.media.AudioTrack
import com.xpwnit.dualbt.logging.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioSplitterKotlin @Inject constructor() {

    private var frameCount: Long = 0

    fun splitAndWrite(pcm: ByteArray, tracks: List<AudioTrack>) {
        for ((index, track) in tracks.withIndex()) {
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                val written = track.write(pcm, 0, pcm.size)
                if (written < 0) {
                    AppLogger.e("AudioSplitter", "Track $index write error: $written")
                }
            }
        }
        frameCount++
        if (frameCount % 500 == 0L) {
            AppLogger.d("AudioSplitter", "Frames processed: $frameCount")
        }
    }

    fun reset() {
        frameCount = 0
        AppLogger.i("AudioSplitter", "Splitter reset")
    }
}
