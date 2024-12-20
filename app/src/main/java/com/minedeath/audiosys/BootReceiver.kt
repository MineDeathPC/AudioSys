package com.minedeath.audiosys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("AudioSysPrefs", Context.MODE_PRIVATE)

            val isLimiterEnabled = sharedPreferences.getBoolean("volumeLimiterEnabled", false)
            if (isLimiterEnabled) {
                val serviceIntent = Intent(context, VolumeLimiterService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
