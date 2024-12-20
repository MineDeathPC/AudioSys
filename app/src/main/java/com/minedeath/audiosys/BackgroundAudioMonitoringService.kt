package com.minedeath.audiosys

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.minedeath.audiosys.utils.SoundLog
import com.minedeath.audiosys.utils.SoundLogManager
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.roundToInt

// Service for monitoring audio in the background
class BackgroundAudioMonitoringService : Service() {

    private var mediaRecorder: MediaRecorder? = null // MediaRecorder for recording audio
    private var isRecording: Boolean = false // Flag to check if recording is active
    private val handler = Handler() // Handler to schedule decibel updates
    private var threshold: Int = 90 // Hardcoded threshold value for decibel level
    private var isVibrationEnabled: Boolean = true // Vibration enabled by default
    private lateinit var vibrator: Vibrator // Vibrator for vibration alerts

    private val samplingRateMillis: Long = 100 // Frequency of decibel checks (100ms)

    // Local Binder to allow activity to communicate with the service
    inner class LocalBinder : Binder() {
        fun getService(): BackgroundAudioMonitoringService = this@BackgroundAudioMonitoringService
    }

    private val binder = LocalBinder()

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onCreate() {
        super.onCreate()

        // Initialize vibrator to provide feedback on high decibel levels
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Start foreground notification to show the service is running
        createNotificationChannel()
        startForeground(1, getNotification("Monitoring audio in background..."))

        // Start recording and begin decibel updates
        startRecording()
        scheduleDecibelUpdate()
    }

    // Service command when started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Keep the service running unless explicitly stopped
    }

    // Cleanup when the service is destroyed
    override fun onDestroy() {
        super.onDestroy()
        stopRecording() // Stop recording audio
        handler.removeCallbacksAndMessages(null) // Remove all scheduled tasks
    }

    // Binds the service to an activity (returns LocalBinder)
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    // Starts recording audio using MediaRecorder
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            try {
                // Set the audio source to microphone
                setAudioSource(MediaRecorder.AudioSource.MIC)

                // Set the output format and audio encoding for the file
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(File.createTempFile("temp_audio", ".3gp", cacheDir).absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                // Prepare and start recording
                prepare()
                start()
                isRecording = true
                Log.d("BackgroundService", "Recording started")
            } catch (e: IOException) {
                e.printStackTrace()
                mediaRecorder?.release() // Release the recorder in case of error
                mediaRecorder = null
                isRecording = false
            }
        }
    }

    // Stops the audio recording
    private fun stopRecording() {
        mediaRecorder?.release() // Release the recorder
        mediaRecorder = null
        isRecording = false
    }

    // Returns the current decibel level by calculating from the media recorder's amplitude
    private fun getDecibelLevel(): Float {
        return if (isRecording) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                if (amplitude > 0) {
                    20 * log10(amplitude.toDouble()).toFloat() // Convert amplitude to decibels
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

    // Schedules periodic updates to check decibel levels
    private fun scheduleDecibelUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    // Get the current decibel level
                    val decibel = getDecibelLevel()
                    Log.d("BackgroundService", "Decibel Level: $decibel dB")

                    // Trigger vibration and notification if threshold is exceeded
                    if (decibel > threshold) {
                        if (isVibrationEnabled) {
                            vibrator.vibrate(500) // Vibrate for 500 milliseconds
                        }

                        // Get current date and time for logging
                        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

                        // Create a new log entry
                        val newLog = SoundLog(decibel, currentDate, currentTime)

                        // Load existing logs, add the new log, and save the updated logs
                        val logs = SoundLogManager.loadLogs(applicationContext)
                        logs.add(newLog)
                        SoundLogManager.saveLogs(applicationContext, logs)

                        // Show a notification for the high decibel level detected
                        showNotification("High noise level detected: ${decibel.roundToInt()} dB at $currentTime")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                handler.postDelayed(this, samplingRateMillis) // Schedule the next decibel update
            }
        })
    }

    // Creates a notification channel for devices running on Android Oreo or higher
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_monitoring_channel", // Channel ID
                "Audio Monitoring Service", // Channel name
                NotificationManager.IMPORTANCE_LOW // Low importance for background service
            )
            channel.description = "Channel for background audio monitoring" // Channel description
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel) // Create the notification channel
        }
    }

    // Returns a notification to show when the service is running
    private fun getNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "audio_monitoring_channel")
            .setContentTitle("Monitoring Background Audio") // Set title of notification
            .setContentText(content) // Set content of notification
            .setSmallIcon(R.drawable.ic_launcher) // Set icon for notification
            .setOngoing(true) // Make the notification ongoing (cannot be swiped away)
            .build()
    }

    // Shows a notification with a specific content when the decibel level is high
    private fun showNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "audio_monitoring_channel")
            .setContentTitle("High Decibel Level Detected!") // Set notification title
            .setContentText(content) // Set notification content
            .setSmallIcon(R.drawable.ic_launcher) // Set icon for notification
            .setAutoCancel(true) // Auto-cancel the notification when clicked
            .build()
        manager.notify(2, notification) // Show the notification with a unique ID
    }
}
