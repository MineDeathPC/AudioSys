package com.minedeath.audiosys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences


class AnalysisActivity : AppCompatActivity() {
    private lateinit var decibelSlider: SeekBar
    private lateinit var vibrationSwitch: CheckBox

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)  // Keep the original layout for AnalysisActivity

        // Get SharedPreferences to load the saved values
        sharedPreferences = getSharedPreferences("AudioSysPrefs", MODE_PRIVATE)

        // Get saved values for threshold and vibration preference
        val savedThreshold = sharedPreferences.getInt("decibelThreshold", 90)
        val isVibrationEnabled = sharedPreferences.getBoolean("vibrationEnabled", true)


        // Set up the Decibel button
        val btnDecibel: Button = findViewById(R.id.btnDecibel)
        btnDecibel.setOnClickListener {
            val threshold = savedThreshold
            val vibrationEnabled = isVibrationEnabled
            val intent = Intent(this, DecibelAnalysisActivity::class.java)
            intent.putExtra("decibelThreshold", threshold)
            intent.putExtra("vibrationEnabled", vibrationEnabled)
            startActivity(intent)
        }

        // Set up the Frequency button
        val btnFrequency: Button = findViewById(R.id.btnFrequency)
        btnFrequency.setOnClickListener {
            val intent = Intent(this, FrequencyAnalysisActivity::class.java)
            startActivity(intent)
        }

        // Back button to return to HomeActivity
        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}
