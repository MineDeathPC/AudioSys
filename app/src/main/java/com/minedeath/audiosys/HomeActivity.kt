package com.minedeath.audiosys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize the "Start Monitoring" button
        val startMonitoringButton: Button = findViewById(R.id.buttonStartMonitoring)
        startMonitoringButton.setOnClickListener {
            startBackgroundAudioMonitoring()
        }
    }

    // Start the background audio monitoring service
    private fun startBackgroundAudioMonitoring() {
        // Start the BackgroundAudioMonitoringService
        val intent = Intent(this, BackgroundAudioMonitoringService::class.java)
        startService(intent)

        // Show a toast message
        Toast.makeText(this, "Background monitoring started!", Toast.LENGTH_SHORT).show()

        // Close the current activity (simulate app closure)
        finish()
    }

    // Placeholder for "Analysis" button click
    fun onAnalysisClick(view: android.view.View) {
        Toast.makeText(this, "Analysis button clicked!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AnalysisActivity::class.java)
        startActivity(intent)
    }

    // Placeholder for "Settings" button click
    fun onSettingsClick(view: android.view.View) {
        Toast.makeText(this, "Settings button clicked!", Toast.LENGTH_SHORT).show()
    }

    // Placeholder for "Logs" button click
    fun onLogsClick(view: android.view.View) {
        Toast.makeText(this, "Logs button clicked!", Toast.LENGTH_SHORT).show()
    }
}
