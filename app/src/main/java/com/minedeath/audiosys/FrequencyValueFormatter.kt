package com.minedeath.audiosys

import com.github.mikephil.charting.formatter.ValueFormatter

class FrequencyValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value >= 1000) {
            // Convert to kHz for values above 1 kHz
            String.format("%.1fkHz", value / 1000)
        } else {
            String.format("%.0fHz", value)
        }
    }
}
