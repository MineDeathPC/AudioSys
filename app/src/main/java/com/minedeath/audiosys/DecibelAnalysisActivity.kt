package com.minedeath.audiosys

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
    private val handler = Handler(Looper.getMainLooper())
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false

    private val decibelValues = LinkedList<Float>()
    private var samplingRateMillis: Long = 50
    private var threshold: Float = 90.0f

    private lateinit var vibrator: Vibrator

    // Color variables for customization
    private val lineColor = "#39FF14" // Neon green for the line
    private val textColorOnLine = "#F50AF3" // Bright pink for the text on the line
    private val peakColorNormal = "#00FFFF" // Cyan for normal peak text
    private val peakColorThreshold = "#FF0000" // Red when threshold is exceeded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decibel_analysis)

        chart = findViewById(R.id.decibelChart)
        peakTextView = findViewById(R.id.peakValue)
        averageTextView = findViewById(R.id.averageValue)
        closeButton = findViewById(R.id.buttonClose)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        setupChart()
        startRecording()
        scheduleDecibelUpdate()

        closeButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = resources.getColor(R.color.violet) // Violet for X-axis labels

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 150f
        leftAxis.textColor = resources.getColor(R.color.violet)
        leftAxis.setLabelCount(31, true) // Add labels for every 5 dB (150 / 5 = 30 labels + 1)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.data = LineData()
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
                Log.d("DecibelAnalysis", "Recording started")
            } catch (e: Exception) {
                e.printStackTrace()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
            }
        }
    }

    private fun scheduleDecibelUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    val decibel = getDecibelLevel()
                    updateChart(decibel)
                    updateStats(decibel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                handler.postDelayed(this, samplingRateMillis)
            }
        })
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

    private fun updateChart(decibel: Float) {
        decibelValues.add(decibel)
        if (decibelValues.size > 30) decibelValues.removeFirst()

        val entries = decibelValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val dataSet = LineDataSet(entries, "Decibel Level (dB)").apply {
            setDrawCircles(false)
            setDrawValues(false)
            color = android.graphics.Color.parseColor(lineColor) // Neon green for line
            //valueTextColor = android.graphics.Color.parseColor(textColorOnLine) // Bright pink for values
           // valueTextSize = 10f // size of value text
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun updateStats(currentPeak: Float) {
        if (decibelValues.isEmpty()) return

        val peak = decibelValues.maxOrNull() ?: 0f
        val average = decibelValues.average().toFloat()

        if (peak > threshold) {
            peakTextView.setTextColor(android.graphics.Color.parseColor(peakColorThreshold))
            vibrator.vibrate(500)
        } else {
            peakTextView.setTextColor(android.graphics.Color.parseColor(peakColorNormal))
        }

        peakTextView.text = getString(R.string.peak_db, peak)
        averageTextView.text = getString(R.string.average_db, average)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        handler.removeCallbacksAndMessages(null)
    }
}
