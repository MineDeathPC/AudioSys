package com.minedeath.audiosys.utils

import java.io.Serializable

data class SoundLog(
    val decibelLevel: Float,
    val date: String,   // Add date property
    val time: String    // Keep the time property
) : Serializable
