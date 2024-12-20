package com.minedeath.audiosys

import com.github.mikephil.charting.formatter.ValueFormatter

// Custom ValueFormatter for formatting frequency values on the chart
class FrequencyValueFormatter : ValueFormatter() {

    // Override the getFormattedValue method to format frequency values
    override fun getFormattedValue(value: Float): String {
        // Check if the frequency value is greater than or equal to 1000 (1 kHz)
        return if (value >= 1000) {
            // If the value is greater than or equal to 1 kHz, format it in kHz
            // For example: 1500 Hz -> 1.5 kHz
            String.format("%.1fkHz", value / 1000)
        } else {
            // If the value is less than 1 kHz, format it in Hz
            // For example: 500 Hz -> 500 Hz
            String.format("%.0fHz", value)
        }
    }
}
