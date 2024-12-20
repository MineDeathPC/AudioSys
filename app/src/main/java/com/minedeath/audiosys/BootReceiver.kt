package com.minedeath.audiosys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Check if the broadcast is for the system boot completion
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Retrieve shared preferences to check if volume limiter is enabled
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("AudioSysPrefs", Context.MODE_PRIVATE)
            val isVolumeLimiterEnabled = sharedPreferences.getBoolean("volumeLimiterEnabled", false)

            if (isVolumeLimiterEnabled) {
                // Start the VolumeLimiterService if the volume limiter is enabled
                val serviceIntent = Intent(context, VolumeLimiterService::class.java)
                context.startService(serviceIntent)
                Toast.makeText(context, "Volume Limiter started after boot.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
