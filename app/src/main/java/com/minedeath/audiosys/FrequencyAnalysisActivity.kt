package com.minedeath.audiosys

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.util.fft.FFT
import kotlin.math.sqrt

class FrequencyAnalysisActivity : AppCompatActivity() {

    // Declare UI elements and variables
    private lateinit var chart: LineChart
    private lateinit var bandwidthTextView: TextView
    private lateinit var dominantFreqTextView: TextView
    private var dominantFrequency: Float = 0f
    private var bandwidth: Float = 0f
    private lateinit var closeButton: Button

    private val smoothingFactor = 0.1f  // Smoothing factor for amplitude smoothing
    private var smoothedMaxAmplitude = 0f

    // Declare audio processing variables
    private var dispatcher: AudioDispatcher? = null
    private val handler = Handler(Looper.getMainLooper())
    private val sampleRate = 22050  // Sample rate for audio
    private val fftSize = 2048  // Size of FFT for frequency analysis
    private val fft = FFT(fftSize)  // FFT instance for frequency analysis
    private val fftBuffer = FloatArray(fftSize)  // Buffer to hold FFT data

    // UI customization colors
    private val lineColor = "#39FF14"
    private val axisLabelColor = "#FFFFFF"
    private val gridColor = "#444444"
    private val textColor = "#F50AF3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frequency_analysis)

        // Initialize UI elements
        chart = findViewById(R.id.frequencyChart)
        bandwidthTextView = findViewById(R.id.bandwidthValue)
        dominantFreqTextView = findViewById(R.id.dominantFreqValue)
        closeButton = findViewById(R.id.buttonClose)

        setupChart()  // Setup the chart
        startFrequencyAnalysis()  // Start audio frequency analysis

        // Close button listener to stop analysis and finish the activity
        closeButton.setOnClickListener {
            stopFrequencyAnalysis()  // Stop the audio analysis
            finish()  // Finish the activity
        }
    }

    // Setup the chart for displaying frequency analysis
    private fun setupChart() {
        chart.description.isEnabled = false  // Disable description
        chart.setTouchEnabled(false)  // Disable touch interaction
        chart.setPinchZoom(false)  // Disable pinch zoom

        // Configure X-Axis for frequency display
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.parseColor(axisLabelColor)
            axisMinimum = 20f  // Set the minimum frequency to 20 Hz
            axisMaximum = (sampleRate / 2).toFloat()  // Set the max frequency to Nyquist frequency
            setLabelCount(6, true)  // Show 6 labels on X-axis
            valueFormatter = FrequencyValueFormatter()  // Custom formatter for frequency labels
        }

        // Configure Y-Axis for amplitude display
        chart.axisLeft.apply {
            textColor = Color.parseColor(axisLabelColor)
            axisMinimum = 0f  // Set min amplitude to 0
            axisMaximum = 1f  // Set max amplitude to 1 (normalized)
            setLabelCount(6, true)  // Show 6 labels on Y-axis
        }

        chart.axisRight.isEnabled = false  // Disable right axis
        chart.legend.isEnabled = false  // Disable chart legend
        chart.data = LineData()  // Set initial empty data
    }

    // Start frequency analysis using TarsosDSP
    private fun startFrequencyAnalysis() {
        // Create an audio dispatcher from the default microphone
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, fftSize, 0)

        // Create an AudioProcessor to handle audio data and perform FFT analysis
        val fftProcessor = object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                val audioBuffer = audioEvent.floatBuffer  // Get the audio buffer from the event

                // Perform FFT on the audio buffer
                fft.forwardTransform(audioBuffer)
                System.arraycopy(audioBuffer, 0, fftBuffer, 0, fftSize)

                // Calculate amplitudes from the FFT data
                val amplitudes = FloatArray(fftSize / 2)
                for (i in amplitudes.indices) {
                    val real = fftBuffer[2 * i]
                    val imag = fftBuffer[2 * i + 1]
                    amplitudes[i] = sqrt(real * real + imag * imag)  // Calculate magnitude
                }

                handler.post {
                    updateChart(amplitudes)  // Update chart with new amplitude data
                    updateStats(amplitudes)  // Update statistical information (dominant frequency, bandwidth)
                }

                return true
            }

            override fun processingFinished() {}
        }

        dispatcher?.addAudioProcessor(fftProcessor)  // Add the FFT processor to the dispatcher
        Thread(dispatcher).start()  // Start the dispatcher in a separate thread
    }

    // Stop frequency analysis
    private fun stopFrequencyAnalysis() {
        dispatcher?.stop()  // Stop the dispatcher
        dispatcher = null  // Release the dispatcher
    }

    // Update the frequency chart with normalized amplitude data
    private fun updateChart(amplitudes: FloatArray) {
        val frequencyValues = ArrayList<Entry>()
        var currentMaxAmplitude = 0f

        // Process amplitudes and track the maximum amplitude
        for (i in amplitudes.indices) {
            val frequency = (i * sampleRate) / fftSize.toFloat()  // Calculate frequency for this index
            frequencyValues.add(Entry(frequency, amplitudes[i]))  // Create an entry for the chart

            // Track the maximum amplitude
            if (amplitudes[i] > currentMaxAmplitude) {
                currentMaxAmplitude = amplitudes[i]
            }
        }

        // Smooth the maximum amplitude for better visualization
        smoothedMaxAmplitude = smoothingFactor * smoothedMaxAmplitude + (1 - smoothingFactor) * currentMaxAmplitude

        // Normalize amplitudes based on the smoothed max amplitude
        val normalizedAmplitudes = amplitudes.map { it / smoothedMaxAmplitude }.toFloatArray()

        // Fix the Y-Axis range to 0-1
        val yAxis = chart.axisLeft
        yAxis.axisMaximum = 1f
        yAxis.axisMinimum = 0f

        // Prepare normalized frequency values for the chart
        val normalizedFrequencyValues = ArrayList<Entry>()
        for (i in normalizedAmplitudes.indices) {
            val frequency = (i * sampleRate) / fftSize.toFloat()
            normalizedFrequencyValues.add(Entry(frequency, normalizedAmplitudes[i]))
        }

        // Create a dataset for the chart
        val dataSet = LineDataSet(normalizedFrequencyValues, "Amplitude over Frequency").apply {
            setDrawCircles(false)  // Disable circles for each data point
            color = Color.parseColor(lineColor)  // Set line color
            valueTextColor = Color.parseColor(textColor)  // Set text color for values
            lineWidth = 1f  // Set the line width
        }

        // Update the chart data and refresh the view
        chart.data = LineData(dataSet)
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    // Update statistical information: dominant frequency and bandwidth
    private fun updateStats(amplitudes: FloatArray) {
        var maxAmplitude = 0f
        var dominantFreq = 0f

        // Find the dominant frequency (highest amplitude)
        for (i in amplitudes.indices) {
            val frequency = (i * sampleRate) / fftSize.toFloat()
            if (amplitudes[i] > maxAmplitude) {
                maxAmplitude = amplitudes[i]
                dominantFreq = frequency
            }
        }

        // Calculate bandwidth based on a threshold of 10% of the max amplitude
        val threshold = maxAmplitude * 0.1f
        var minFreq = Float.MAX_VALUE
        var maxFreq = Float.MIN_VALUE

        // Find the minimum and maximum frequencies within the threshold
        for (i in amplitudes.indices) {
            val frequency = (i * sampleRate) / fftSize.toFloat()
            if (amplitudes[i] >= threshold) {
                minFreq = minOf(minFreq, frequency)
                maxFreq = maxOf(maxFreq, frequency)
            }
        }

        bandwidth = maxFreq - minFreq  // Calculate bandwidth

        // Update the UI with the calculated statistics
        dominantFreqTextView.text = "Dominant Frequency: %.1f Hz".format(dominantFreq)
        bandwidthTextView.text = "Bandwidth: %.1f Hz".format(bandwidth)
    }

    // Cleanup and stop frequency analysis when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        stopFrequencyAnalysis()  // Stop the frequency analysis when the activity is destroyed
    }
}
