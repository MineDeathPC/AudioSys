package com.minedeath.audiosys

import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minedeath.audiosys.utils.SoundLog
import com.minedeath.audiosys.utils.SoundLogManager
import kotlin.math.roundToInt

class LogsActivity : AppCompatActivity() {

    private lateinit var logsTextView: TextView  // TextView to display logs
    private lateinit var clearLogsButton: Button // Button to clear the logs
    private lateinit var backButton: ImageButton // Button to go back

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        // Initialize views
        logsTextView = findViewById(R.id.logsTextView)
        clearLogsButton = findViewById(R.id.clearLogsButton)
        backButton = findViewById(R.id.backButton)

        // Load the logs from file using SoundLogManager
        val logs = SoundLogManager.loadLogs(this)

        // Check if logs are empty and update the TextView accordingly
        if (logs.isEmpty()) {
            // Display "No logs to display...." if empty
            logsTextView.text = "No logs to display...."
        } else {
            // Format the logs for display
            val logText = StringBuilder()
            for (log in logs) {
                // Applying colors to decibel level (red), date (green), and time (green)
                val formattedLog = """
                    Decibel level of <font color='red'>${log.decibelLevel.roundToInt()} dB</font> 
                    detected on <font color='green'>${log.date}</font> at <font color='green'>${log.time}</font><br>
                """.trimIndent()

                // Append the formatted log entry
                logText.append(formattedLog)
            }

            // Set the text in the TextView with HTML formatting
            logsTextView.text = Html.fromHtml(logText.toString(), Html.FROM_HTML_MODE_LEGACY)
        }

        // Set up the back button to finish the activity (navigate back)
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set up the clear logs button to clear the logs
        clearLogsButton.setOnClickListener {
            SoundLogManager.clearLogs(this)
            // Clear the displayed logs
            logsTextView.text = "No logs to display...."  // Reset the text to empty state
            Toast.makeText(this, "Logs cleared!", Toast.LENGTH_SHORT).show()
        }
    }
}
