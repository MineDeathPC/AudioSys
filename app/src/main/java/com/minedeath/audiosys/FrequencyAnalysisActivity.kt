package com.minedeath.audiosys

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import java.util.LinkedList

class FrequencyAnalysisActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var peakTextView: TextView
    private lateinit var averageTextView: TextView
    private lateinit var closeButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private var dispatcher: AudioDispatcher? = null

    private val frequencyValues = LinkedList<Float>() // Store frequency values
    private var samplingRateMillis: Long = 200 // Sampling rate in milliseconds
   // private var frequencyThreshold: Float = 1000.0f // Example threshold in Hz

    // Color variables for customization
    private val lineColor = "#39FF14" // Neon green for the line
    private val textColorOnLine = "#F50AF3" // Bright pink for the text on the line
    private val peakColorNormal = "#00FFFF" // Cyan for normal peak text
  //  private val peakColorThreshold = "#FF0000" // Red when threshold is exceeded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frequency_analysis)

        // Initialize UI components
        chart = findViewById(R.id.frequencyChart)
        peakTextView = findViewById(R.id.peakValue)
        averageTextView = findViewById(R.id.averageValue)
        closeButton = findViewById(R.id.buttonClose)

        // Set up the chart
        setupChart()

        // Start frequency analysis
        startFrequencyAnalysis()

        // Close button action
        closeButton.setOnClickListener {
            stopFrequencyAnalysis()
            onBackPressed()
        }
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = android.graphics.Color.parseColor("#FFFFFF") // White for X-axis labels

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 20000f
        leftAxis.setLabelCount(41, true) // Labels every 500 Hz
        leftAxis.textColor = android.graphics.Color.parseColor("#FFFFFF") // White for Y-axis labels

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.data = LineData()
    }

    private fun startFrequencyAnalysis() {
        val sampleRate = 22050 // Try 22.05 kHz or 16 kHz for efficiency
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, 2048, 0)

        val pitchHandler = PitchDetectionHandler { result, _ ->
            val frequency = result.pitch

            if (frequency in 50f..20000f) {
                runOnUiThread {
                    updateChart(frequency)
                    updateStats()
                }
            }
        }

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            2048,
            pitchHandler
        )

        dispatcher?.addAudioProcessor(pitchProcessor)
        Thread(dispatcher).start()
    }

    private fun stopFrequencyAnalysis() {
        dispatcher?.stop()
        dispatcher = null
    }

    private fun updateChart(frequency: Float) {
        frequencyValues.add(frequency)
        if (frequencyValues.size > 30) frequencyValues.removeFirst() // Keep last 30 values

        val entries = frequencyValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val dataSet = LineDataSet(entries, "Frequency (Hz)").apply {
            setDrawCircles(false)
            color = android.graphics.Color.parseColor(lineColor) // Neon green for line
            valueTextColor = android.graphics.Color.parseColor(textColorOnLine) // Bright pink for values
            valueTextSize = 10f // Adjust size of value text
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun updateStats() {
        if (frequencyValues.isEmpty()) return

        val peak = frequencyValues.maxOrNull() ?: 0f
        val average = frequencyValues.average().toFloat()

        // Change peak text color based on threshold
   //     if (peak > frequencyThreshold) {
     //       peakTextView.setTextColor(android.graphics.Color.parseColor(peakColorThreshold)) // Red
     //   } else {
    //        peakTextView.setTextColor(android.graphics.Color.parseColor(peakColorNormal)) // Cyan
     //   }

        peakTextView.text = getString(R.string.peak_frequency, peak)
        averageTextView.text = getString(R.string.average_frequency, average)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFrequencyAnalysis()
    }
}
