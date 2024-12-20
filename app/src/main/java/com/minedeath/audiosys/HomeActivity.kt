package com.minedeath.audiosys

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    // UI components
    private lateinit var startMonitoringButton: Button

    // Flag to track if monitoring is active
    private var isMonitoring: Boolean = false

    // Reference to the background service
    private var backgroundAudioMonitoringService: BackgroundAudioMonitoringService? = null

    // Flag to track if the service is bound
    private var serviceBound = false

    // Service connection to track the state of the service
    private val serviceConnection = object : ServiceConnection {
        // Called when the service is connected successfully
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Get the service instance from the binder
            val binder = service as BackgroundAudioMonitoringService.LocalBinder
            backgroundAudioMonitoringService = binder.getService()
            serviceBound = true
        }

        // Called when the service is disconnected
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize the "Start Monitoring" button
        startMonitoringButton = findViewById(R.id.buttonStartMonitoring)

        // Set the button click listener to toggle monitoring
        startMonitoringButton.setOnClickListener {
            if (isMonitoring) {
                stopBackgroundAudioMonitoring()
            } else {
                startBackgroundAudioMonitoring()
            }
        }
    }

    // Method to start the background audio monitoring service
    private fun startBackgroundAudioMonitoring() {
        // Create an intent to bind to the service
        val intent = Intent(this, BackgroundAudioMonitoringService::class.java)
        // Bind the service to the activity
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Show a toast to inform the user
        Toast.makeText(this, "Background monitoring started!", Toast.LENGTH_SHORT).show()

        // Set the monitoring flag to true
        isMonitoring = true
    }

    // Method to stop the background audio monitoring service
    private fun stopBackgroundAudioMonitoring() {
        // Check if the service is bound before stopping it
        if (serviceBound) {
            val intent = Intent(this, BackgroundAudioMonitoringService::class.java)
            stopService(intent) // Stop the service
            unbindService(serviceConnection) // Unbind from the service
            serviceBound = false
        }

        // Show a toast to inform the user
        Toast.makeText(this, "Background monitoring stopped!", Toast.LENGTH_SHORT).show()

        // Set the monitoring flag to false
        isMonitoring = false
    }

    // Called when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        // Unbind the service if bound
        if (serviceBound) {
            unbindService(serviceConnection)
        }
    }

    // Placeholder method for "Analysis" button click
    fun onAnalysisClick(view: android.view.View) {
       // Toast.makeText(this, "Analysis button clicked!", Toast.LENGTH_SHORT).show()

        // Navigate to SplashActivity, then to AnalysisActivity after a short delay
        val splashIntent = Intent(this, SplashActivity::class.java)
        splashIntent.putExtra("targetActivity", AnalysisActivity::class.java) // Pass the target activity
        startActivity(splashIntent)
    }

    // Placeholder method for "Settings" button click
    fun onSettingsClick(view: android.view.View) {
       // Toast.makeText(this, "Settings button clicked!", Toast.LENGTH_SHORT).show()

        // Navigate to SplashActivity, then to SettingsActivity after a short delay
        val splashIntent = Intent(this, SplashActivity::class.java)
        splashIntent.putExtra("targetActivity", SettingsActivity::class.java) // Pass the target activity
        startActivity(splashIntent)
    }

    // Placeholder method for "Logs" button click
    fun onLogsClick(view: android.view.View) {
        //Toast.makeText(this, "Logs button clicked!", Toast.LENGTH_SHORT).show()

        // Navigate to SplashActivity, then to LogsActivity after a short delay
        val splashIntent = Intent(this, SplashActivity::class.java)
        splashIntent.putExtra("targetActivity", LogsActivity::class.java) // Pass the target activity
        startActivity(splashIntent)
    }
}
