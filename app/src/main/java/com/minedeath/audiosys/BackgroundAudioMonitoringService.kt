package com.minedeath.audiosys

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import kotlin.math.roundToInt

class BackgroundAudioMonitoringService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private val decibelValues = LinkedList<Float>()
    private val handler = Handler()
    private var threshold: Float = 90.0f
    private lateinit var vibrator: Vibrator

    private val samplingRateMillis: Long = 50 // Frequency of decibel checks (50ms)

    override fun onCreate() {
        super.onCreate()

        // Initialize the vibrator
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Start foreground notification with the stop button
        createNotificationChannel()
        startForeground(1, getNotification("Monitoring audio in background..."))

        // Start recording and monitoring
        startRecording()
        scheduleDecibelUpdate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf() // Stop the service if "STOP_SERVICE" action is triggered
        }
        return START_STICKY // Keep the service running unless explicitly stopped
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(File.createTempFile("temp_audio", ".3gp", cacheDir).absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
                isRecording = true
                Log.d("BackgroundService", "Recording started")
            } catch (e: IOException) {
                e.printStackTrace()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
    }

    private fun getDecibelLevel(): Float {
        return if (isRecording) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                if (amplitude > 0) {
                    20 * Math.log10(amplitude.toDouble()).toFloat()
                } else {
                    0f
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0f
            }
        } else {
            0f
        }
    }

    private fun scheduleDecibelUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    val decibel = getDecibelLevel()
                    Log.d("BackgroundService", "Decibel Level: $decibel dB")

                    // Trigger vibration and alert if threshold is exceeded
                    if (decibel > threshold) {
                        vibrator.vibrate(500)
                        showNotification("High noise detected: ${decibel.toDouble().roundToInt()} dB! at ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(
                            Date()
                        )}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                handler.postDelayed(this, samplingRateMillis)
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_monitoring_channel",
                "Audio Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(content: String): Notification {
        // Create an intent to stop the service
        val stopIntent = Intent(this, BackgroundAudioMonitoringService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "audio_monitoring_channel")
            .setContentTitle("Audio Monitoring")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode) // Use your custom icon
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Monitoring",
                stopPendingIntent
            ) // Add the stop action
            .build()
    }

    private fun showNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "audio_monitoring_channel")
            .setContentTitle("Audio Alert")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .setAutoCancel(true)
            .build()
        manager.notify(2, notification) // Use a unique ID for each notification
    }
}
