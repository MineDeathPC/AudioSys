package com.minedeath.audiosys.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object SoundLogManager {
    private const val FILE_NAME = "sound_logs.dat"

    // Save logs to a file
    fun saveLogs(context: Context, logs: List<SoundLog>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(logs)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load logs from a file
    fun loadLogs(context: Context): MutableList<SoundLog> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf() // Return empty list if file doesn't exist

        return try {
            val inputStream = ObjectInputStream(FileInputStream(file))
            val logs = inputStream.readObject() as? MutableList<SoundLog> ?: mutableListOf()
            inputStream.close()
            logs
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    // Clear logs (delete the file)
    fun clearLogs(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
