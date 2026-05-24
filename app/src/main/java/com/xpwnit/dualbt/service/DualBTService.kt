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
import com.xpwnit.dualbt.audio.AudioRouter
import com.xpwnit.dualbt.audio.AudioSplitterKotlin
import com.xpwnit.dualbt.audio.CaptureEngine
import com.xpwnit.dualbt.bt.BTDevice
import com.xpwnit.dualbt.logging.AppLogger
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
    @Inject lateinit var splitter: AudioSplitterKotlin

    inner class LocalBinder : Binder() {
        fun getService() = this@DualBTService
    }

    private val binder = LocalBinder()
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        AppLogger.i("DualBTService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            AppLogger.i("DualBTService", "Stop action received")
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        @Suppress("DEPRECATION")
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != -1 && resultData != null) {
            AppLogger.i("DualBTService", "Starting with MediaProjection")
            val projectionManager = getSystemService(MediaProjectionManager::class.java)
            val projection = projectionManager.getMediaProjection(resultCode, resultData)
            startAudioCapture(projection)
        } else {
            AppLogger.i("DualBTService", "Starting in mock/emulator mode")
            startMockCapture()
        }

        return START_STICKY
    }

    fun startWithDevices(devices: List<BTDevice>, projection: MediaProjection? = null) {
        if (isRunning) {
            AppLogger.w("DualBTService", "Already running — ignoring startWithDevices")
            return
        }

        AppLogger.i("DualBTService", "Starting audio for ${devices.size} device(s)")
        audioRouter.setupTracks(devices)

        val tracks = audioRouter.getTracks()
        if (projection != null) {
            captureEngine.onPcmData = { pcm ->
                splitter.splitAndWrite(pcm, tracks)
            }
            captureEngine.startCapture(projection)
        } else {
            captureEngine.onPcmData = { pcm ->
                splitter.splitAndWrite(pcm, tracks)
            }
            captureEngine.startMockCapture()
        }

        isRunning = true
        AppLogger.i("DualBTService", "Audio streaming started")
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
        AppLogger.i("DualBTService", "Service destroying")
        captureEngine.stopCapture()
        audioRouter.releaseTracks()
        splitter.reset()
        isRunning = false
        super.onDestroy()
        AppLogger.i("DualBTService", "Service destroyed")
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
