package com.minedeath.audiosys

import com.github.mikephil.charting.formatter.ValueFormatter

// Custom ValueFormatter for displaying decibel values on the Y-axis of the chart
class DecibelValueFormatter : ValueFormatter() {

    // Override the getFormattedValue method to format the decibel value as an integer followed by "dB"
    override fun getFormattedValue(value: Float): String {
        // Convert the float value to an integer and append " dB" to it
        return "${value.toInt()} dB"
    }
}
