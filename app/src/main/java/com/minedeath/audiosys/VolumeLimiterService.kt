package com.minedeath.audiosys

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class VolumeLimiterService : Service() {

    private lateinit var audioManager: AudioManager
    private lateinit var sharedPreferences: SharedPreferences
    private var isRunning = true

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        sharedPreferences = getSharedPreferences("AudioSysPrefs", MODE_PRIVATE)

        // Start foreground service
        createNotificationChannel()
        startForeground(1, getNotification())

        // Monitor and enforce volume limits
        Thread {
            while (isRunning) {
                try {
                    enforceVolumeLimit()
                    Thread.sleep(500) // Check volume every 500ms
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun enforceVolumeLimit() {
        val isLimiterEnabled = sharedPreferences.getBoolean("volumeLimiterEnabled", false)
        if (!isLimiterEnabled) return

        val maxVolume = sharedPreferences.getInt("maxVolumeLevel", 50)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val allowedVolume = (maxSystemVolume * maxVolume) / 100

        if (currentVolume > allowedVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, allowedVolume, 0)
            Log.d("VolumeLimiterService", "Volume adjusted to $allowedVolume")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "volume_limiter_channel",
                "Volume Limiter",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, "volume_limiter_channel")
            .setContentTitle("Volume Limiter")
            .setContentText("Volume limiter is active")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .build()
    }
}
