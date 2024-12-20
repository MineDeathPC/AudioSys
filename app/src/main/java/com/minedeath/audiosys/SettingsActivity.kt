package com.minedeath.audiosys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var decibelSlider: SeekBar
    private lateinit var decibelThresholdText: TextView
    private lateinit var vibrationSwitch: CheckBox
    private lateinit var volumeLimiterSwitch: CheckBox
    private lateinit var volumeSlider: SeekBar
    private lateinit var volumeLimitText: TextView
    private lateinit var backButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind views to their respective IDs
        decibelSlider = findViewById(R.id.decibelSlider)
        decibelThresholdText = findViewById(R.id.decibelThresholdText)
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        volumeLimiterSwitch = findViewById(R.id.volumeLimiterSwitch)
        volumeSlider = findViewById(R.id.volumeSlider)
        volumeLimitText = findViewById(R.id.volumeLimitText)
        backButton = findViewById(R.id.backButton)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AudioSysPrefs", MODE_PRIVATE)

        // Load saved preferences for decibel threshold, vibration, and volume limiter settings
        val savedThreshold = sharedPreferences.getInt("decibelThreshold", 90)
        val isVibrationEnabled = sharedPreferences.getBoolean("vibrationEnabled", true)
        val isVolumeLimiterEnabled = sharedPreferences.getBoolean("volumeLimiterEnabled", false)
        val savedVolumeLimit = sharedPreferences.getInt("maxVolumeLevel", 50)

        // Set initial values in the UI
        decibelSlider.progress = savedThreshold
        decibelThresholdText.text = "Decibel Threshold: $savedThreshold dB"
        vibrationSwitch.isChecked = isVibrationEnabled
        volumeLimiterSwitch.isChecked = isVolumeLimiterEnabled
        volumeSlider.progress = savedVolumeLimit
        volumeLimitText.text = "Volume Limit: $savedVolumeLimit%"

        // Set up SeekBar change listener to update and save the decibel threshold
        decibelSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                decibelThresholdText.text = "Decibel Threshold: $progress dB"
                // Save the updated threshold value as Int
                sharedPreferences.edit()
                    .putInt("decibelThreshold", progress)
                    .apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set up CheckBox listener for vibration preference
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("vibrationEnabled", isChecked)
                .apply()
        }

        // Set up CheckBox listener for volume limiter toggle
        volumeLimiterSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("volumeLimiterEnabled", isChecked)
                .apply()

            // Enable or disable the volume slider based on volume limiter status
            volumeSlider.isEnabled = isChecked
            if (isChecked) {
                // Start the VolumeLimiterService when enabled
                val serviceIntent = Intent(this@SettingsActivity, VolumeLimiterService::class.java)
                startService(serviceIntent)
            } else {
                // Stop the VolumeLimiterService when disabled
                val serviceIntent = Intent(this@SettingsActivity, VolumeLimiterService::class.java)
                stopService(serviceIntent)

                // Reset volume limit to default if volume limiter is disabled
                sharedPreferences.edit()
                    .putInt("maxVolumeLevel", 50)
                    .apply()
                volumeLimitText.text = "Volume Limit: 50%"
                volumeSlider.progress = 50
            }
        }

        // Set up SeekBar for volume limit adjustment
        volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeLimitText.text = "Volume Limit: $progress%"
                sharedPreferences.edit()
                    .putInt("maxVolumeLevel", progress)
                    .apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Handle back button click
        backButton.setOnClickListener {
            finish() // Close settings and return to the previous screen
        }
    }
}
