package com.minedeath.audiosys

import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.io.IOException
import java.util.LinkedList

class DecibelAnalysisActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var peakTextView: TextView
    private lateinit var averageTextView: TextView
    private lateinit var closeButton: Button
    private val handler = Handler(Looper.getMainLooper()) // Handler to update UI periodically
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false

    private val decibelValues = LinkedList<Float>() // LinkedList to store the last 30 decibel values
    private var samplingRateMillis: Long = 50 // The frequency of decibel level checks (50ms)
    private var threshold: Float = 90.0f // The threshold above which the decibel level is considered high
    private var vibrationEnabled: Boolean = true // Flag to enable or disable vibration when threshold is exceeded

    private lateinit var vibrator: Vibrator // Vibrator to trigger vibration if enabled

    // Color variables for the chart and UI elements
    private val lineColorStart = "#39FF14" // Color for line when decibel level is below threshold (green)
    private val lineColorEnd = "#FF0000" // Color for line when decibel level exceeds threshold (red)
    private val peakColorNormal = "#00FFFF" // Color for peak text when below threshold (cyan)
    private val peakColorThreshold = "#FF0000" // Color for peak text when threshold is exceeded (red)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decibel_analysis)

        // Initialize UI components
        chart = findViewById(R.id.decibelChart)
        peakTextView = findViewById(R.id.peakValue)
        averageTextView = findViewById(R.id.averageValue)
        closeButton = findViewById(R.id.buttonClose)

        // Initialize vibrator system service
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Get values from Intent (from the previous activity or service)
        threshold = intent.getIntExtra("decibelThreshold", 90).toFloat()
        vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)

        // Setup chart and start recording audio
        setupChart()
        startRecording()
        scheduleDecibelUpdate()

        // Set listener to close the activity when the button is clicked
        closeButton.setOnClickListener {
            onBackPressed()
        }
    }

    // Set up the LineChart to display decibel levels
    private fun setupChart() {
        chart.description.isEnabled = false // Disable chart description
        chart.setTouchEnabled(false) // Disable user interaction with the chart
        chart.setPinchZoom(false) // Disable pinch zooming on the chart

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(false)  // Remove x-axis labels for simplicity
        xAxis.textColor = resources.getColor(android.R.color.white)

        // Left axis settings (vertical axis for decibel levels)
        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f // Set minimum value to 0 dB
        leftAxis.axisMaximum = 150f // Set maximum value to 150 dB
        leftAxis.textColor = resources.getColor(android.R.color.white)
        leftAxis.setLabelCount(31, true) // Show 31 labels for a better granularity
        leftAxis.valueFormatter = DecibelValueFormatter()  // Custom formatter to display decibel values

        // Right axis is not enabled
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // Initialize chart data
        chart.data = LineData()
    }

    // Start recording audio using the device's microphone
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC) // Use the microphone as the audio source
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Set the output format (3GP)
                setOutputFile(File.createTempFile("temp_audio", ".3gp", cacheDir).absolutePath) // Set temporary file for recording
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Use AMR-NB encoding for the audio

                prepare() // Prepare the recorder for use
                start() // Start the recording
                isRecording = true // Set recording flag to true
                Log.d("DecibelAnalysis", "Recording started")
            } catch (e: Exception) {
                e.printStackTrace()
                mediaRecorder?.release() // Release the recorder in case of error
                mediaRecorder = null
                isRecording = false
            }
        }
    }

    // Schedule periodic updates for the decibel level and chart updates
    private fun scheduleDecibelUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    // Get the current decibel level
                    val decibel = getDecibelLevel()
                    // Update chart and stats based on the new decibel value
                    updateChart(decibel)
                    updateStats(decibel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Re-run this update every 'samplingRateMillis' milliseconds
                handler.postDelayed(this, samplingRateMillis)
            }
        })
    }

    // Get the current decibel level from the MediaRecorder
    private fun getDecibelLevel(): Float {
        return if (isRecording) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                if (amplitude > 0) {
                    // Convert the amplitude to decibel level
                    20 * Math.log10(amplitude.toDouble()).toFloat()
                } else {
                    0f // Return 0 if amplitude is 0 (no sound detected)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0f
            }
        } else {
            0f
        }
    }

    // Update the LineChart with the latest decibel level
    private fun updateChart(decibel: Float) {
        // Add the new decibel value to the list
        decibelValues.add(decibel)
        if (decibelValues.size > 30) decibelValues.removeFirst() // Keep only the last 30 values

        // Create chart entries from the decibel values
        val entries = decibelValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val dataSet = LineDataSet(entries, "Decibel Level (dB)").apply {
            setDrawCircles(false) // Do not display individual data points
            setDrawValues(false) // Do not display the values on the chart

            // Transition line color based on decibel level (green for normal, red for high)
            val color = if (decibel > threshold) lineColorEnd else lineColorStart
            this.color = Color.parseColor(color) // Set the line color dynamically
        }

        // Set the updated data and refresh the chart
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.notifyDataSetChanged() // Notify the chart of new data
        chart.invalidate() // Redraw the chart
    }

    // Update the peak and average decibel values, and update UI accordingly
    private fun updateStats(currentPeak: Float) {
        if (decibelValues.isEmpty()) return // No data to update

        // Calculate peak and average decibel values
        val peak = decibelValues.maxOrNull() ?: 0f
        val average = decibelValues.average().toFloat()

        // Update the peak TextView color based on whether it exceeds the threshold
        if (peak > threshold) {
            peakTextView.setTextColor(Color.parseColor(peakColorThreshold)) // Red color when threshold is exceeded
            if (vibrationEnabled) {
                vibrator.vibrate(500) // Vibrate for 500ms if enabled
            }
        } else {
            peakTextView.setTextColor(Color.parseColor(peakColorNormal)) // color when below threshold
        }

        // Update the peak and average values in the UI
        peakTextView.text = getString(R.string.peak_db, peak)
        averageTextView.text = getString(R.string.average_db, average)
    }

    // Release resources when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release() // Release the media recorder
        mediaRecorder = null
        isRecording = false
        handler.removeCallbacksAndMessages(null) // Remove any pending UI updates
    }
}
