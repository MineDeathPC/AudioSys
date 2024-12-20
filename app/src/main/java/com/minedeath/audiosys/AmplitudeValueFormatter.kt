// Package declaration for the application
package com.minedeath.audiosys

// Importing the ValueFormatter class from the MPAndroidChart library to format chart values
import com.github.mikephil.charting.formatter.ValueFormatter

// Custom class to format the amplitude values displayed on the chart
class AmplitudeValueFormatter : ValueFormatter() {

    // Overriding the getFormattedValue method from ValueFormatter
    // This method is called to format the value before displaying it on the chart
    override fun getFormattedValue(value: Float): String {

        // Returning the formatted value as a string with 2 decimal places
        // %.2f formats the float value to 2 decimal places
        return String.format("%.2f", value)
    }
}
