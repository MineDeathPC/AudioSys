package com.minedeath.audiosys

import com.github.mikephil.charting.formatter.ValueFormatter

class AmplitudeValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format("%.2f", value)
    }
}
