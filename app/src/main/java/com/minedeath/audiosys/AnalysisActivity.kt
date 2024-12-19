package com.minedeath.audiosys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // Set up the Decibel button
        val btnDecibel: Button = findViewById(R.id.btnDecibel)
        btnDecibel.setOnClickListener {
            val intent = Intent(this, DecibelAnalysisActivity::class.java)
            startActivity(intent)
        }

        // Set up the Frequency button
        val btnFrequency: Button = findViewById(R.id.btnFrequency)
        btnFrequency.setOnClickListener {
            val intent = Intent(this, FrequencyAnalysisActivity::class.java)
            startActivity(intent)
        }

        // Back button to return to HomeActivity
        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}
